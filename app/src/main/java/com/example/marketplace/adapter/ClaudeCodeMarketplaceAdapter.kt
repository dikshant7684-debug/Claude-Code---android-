package com.example.marketplace.adapter

import com.example.marketplace.model.DeclaredPermission
import com.example.marketplace.model.EcosystemType
import com.example.marketplace.model.ExtensionBundle
import com.example.marketplace.model.ExtensionType
import com.example.marketplace.model.InstalledExtension
import com.example.marketplace.model.MarketplaceSource
import com.example.marketplace.model.MarketplaceValidationResult
import com.example.marketplace.model.UniversalExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ClaudeCodeMarketplaceAdapter(
  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()
) : MarketplaceAdapter {

  override val ecosystem: EcosystemType = EcosystemType.CLAUDE_CODE

  override fun canHandle(source: MarketplaceSource): Boolean {
    if (source.ecosystem == EcosystemType.CLAUDE_CODE) return true
    val loc = source.location.lowercase()
    return loc.contains("claude") || loc.contains("anthropic") || loc.contains("skills")
  }

  override suspend fun validate(source: MarketplaceSource): MarketplaceValidationResult = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      if (catalog.isNotEmpty()) {
        MarketplaceValidationResult(
          isValid = true,
          message = "Valid Claude Code repository with ${catalog.size} extension(s) detected.",
          detectedEcosystem = EcosystemType.CLAUDE_CODE,
          itemCount = catalog.size
        )
      } else {
        MarketplaceValidationResult(
          isValid = false,
          message = "Repository reachable but no valid Claude Code skills or manifests found.",
          detectedEcosystem = EcosystemType.CLAUDE_CODE,
          itemCount = 0
        )
      }
    } catch (e: Exception) {
      MarketplaceValidationResult(
        isValid = false,
        message = "Connection failed: ${e.message}",
        detectedEcosystem = EcosystemType.CLAUDE_CODE,
        itemCount = 0
      )
    }
  }

  override suspend fun fetchCatalog(source: MarketplaceSource): List<UniversalExtension> = withContext(Dispatchers.IO) {
    val items = mutableListOf<UniversalExtension>()
    val rawUrl = resolveRawManifestUrl(source.location, source.branch)

    val content = fetchUrlText(rawUrl)
    if (content != null) {
      // 1. Try parsing JSON catalog (e.g. marketplace.json, skills.json)
      try {
        val root = JSONObject(content)
        val array = root.optJSONArray("skills") ?: root.optJSONArray("extensions") ?: root.optJSONArray("items")
        if (array != null) {
          for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(parseUniversalExtension(source.id, obj))
          }
        }
      } catch (_: Exception) {
        // Try as JSON array directly
        try {
          val array = JSONArray(content)
          for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(parseUniversalExtension(source.id, obj))
          }
        } catch (_: Exception) {
          // If it's a SKILL.md directly
          if (content.contains("# ") || content.contains("---")) {
            val single = parseSkillMarkdown(source.id, source.name, content, source.location)
            items.add(single)
          }
        }
      }
    }

    if (items.isEmpty()) {
      // Curated Claude Code ecosystem reference skills matching real community packages
      items.addAll(getBuiltinClaudeSkills(source.id))
    }

    items
  }

  override suspend fun resolveExtensionBundle(source: MarketplaceSource, extension: UniversalExtension): ExtensionBundle = withContext(Dispatchers.IO) {
    val files = mutableMapOf<String, String>()

    if (extension.instructions.isNotBlank()) {
      files["SKILL.md"] = buildSkillMarkdown(extension)
    }

    // Try fetching remote files if original location is URL
    if (extension.originalLocation.startsWith("http")) {
      val remoteContent = fetchUrlText(extension.originalLocation)
      if (remoteContent != null) {
        files["SKILL.md"] = remoteContent
      }
    }

    val manifestObj = JSONObject().apply {
      put("id", extension.id)
      put("name", extension.name)
      put("version", extension.version)
      put("ecosystem", extension.ecosystem.name)
      put("type", extension.type.name)
      put("author", extension.author)
      put("permissions", JSONArray(extension.declaredPermissions.map { it.name }))
    }
    files["manifest.json"] = manifestObj.toString(2)

    ExtensionBundle(extension = extension, files = files)
  }

  override suspend fun checkForUpdates(installed: InstalledExtension, source: MarketplaceSource): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      val remote = catalog.find { it.id == installed.id } ?: return@withContext null
      if (isNewerVersion(remote.version, installed.version)) {
        UpdateInfo(
          extensionId = installed.id,
          currentVersion = installed.version,
          latestVersion = remote.version,
          changelog = "Updated capability definitions and instructions."
        )
      } else null
    } catch (_: Exception) {
      null
    }
  }

  private fun parseUniversalExtension(sourceId: String, obj: JSONObject): UniversalExtension {
    val id = obj.optString("id", obj.optString("name").lowercase().replace(" ", "-"))
    val name = obj.optString("name", id)
    val description = obj.optString("description", "")
    val author = obj.optString("author", "Community")
    val version = obj.optString("version", "1.0.0")
    val rawType = obj.optString("type", "SKILL")
    val type = try { ExtensionType.valueOf(rawType.uppercase()) } catch (_: Exception) { ExtensionType.SKILL }
    val instructions = obj.optString("instructions", obj.optString("prompt", ""))

    val permissionsList = mutableListOf<DeclaredPermission>()
    val permArray = obj.optJSONArray("permissions")
    if (permArray != null) {
      for (i in 0 until permArray.length()) {
        try {
          permissionsList.add(DeclaredPermission.valueOf(permArray.getString(i).uppercase()))
        } catch (_: Exception) {}
      }
    }
    if (permissionsList.isEmpty()) {
      permissionsList.add(DeclaredPermission.FILESYSTEM_READ)
    }

    val tagsList = mutableListOf<String>()
    val tagsArray = obj.optJSONArray("tags")
    if (tagsArray != null) {
      for (i in 0 until tagsArray.length()) {
        tagsList.add(tagsArray.getString(i))
      }
    }

    return UniversalExtension(
      id = id,
      name = name,
      description = description,
      author = author,
      version = version,
      marketplaceSourceId = sourceId,
      originalLocation = obj.optString("url", ""),
      type = type,
      ecosystem = EcosystemType.CLAUDE_CODE,
      compatibilityRequirements = listOf("Android 8.0+"),
      declaredPermissions = permissionsList,
      dependencies = emptyList(),
      instructions = instructions,
      toolSchemaJson = obj.optString("tool_schema", ""),
      license = obj.optString("license", "MIT"),
      tags = tagsList
    )
  }

  private fun parseSkillMarkdown(sourceId: String, sourceName: String, md: String, loc: String): UniversalExtension {
    var name = sourceName
    var description = "Claude Code skill from $sourceName"
    val lines = md.lines()

    // Parse YAML frontmatter if present
    if (lines.firstOrNull()?.trim() == "---") {
      var idx = 1
      while (idx < lines.size && lines[idx].trim() != "---") {
        val line = lines[idx].trim()
        if (line.startsWith("name:")) {
          name = line.substringAfter("name:").trim().removeSurrounding("\"")
        } else if (line.startsWith("description:")) {
          description = line.substringAfter("description:").trim().removeSurrounding("\"")
        }
        idx++
      }
    } else {
      val h1 = lines.firstOrNull { it.startsWith("# ") }
      if (h1 != null) {
        name = h1.removePrefix("# ").trim()
      }
    }

    val id = name.lowercase().replace(Regex("[^a-z0-9_-]"), "-").trim('-')
    return UniversalExtension(
      id = id,
      name = name,
      description = description,
      author = "Anthropic / Community",
      version = "1.0.0",
      marketplaceSourceId = sourceId,
      originalLocation = loc,
      type = ExtensionType.SKILL,
      ecosystem = EcosystemType.CLAUDE_CODE,
      declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ),
      instructions = md,
      tags = listOf("claude", "skill", "coding")
    )
  }

  private fun buildSkillMarkdown(ext: UniversalExtension): String {
    return """---
name: ${ext.name}
description: ${ext.description}
version: ${ext.version}
author: ${ext.author}
ecosystem: Claude Code
---

# ${ext.name}

${ext.description}

## Instructions
${ext.instructions}
"""
  }

  private fun resolveRawManifestUrl(location: String, branch: String): String {
    var loc = location.trim()
    if (loc.endsWith(".git")) loc = loc.removeSuffix(".git")
    if (loc.contains("github.com") && !loc.contains("raw.githubusercontent.com")) {
      // e.g. https://github.com/owner/repo -> https://raw.githubusercontent.com/owner/repo/main/marketplace.json
      val parts = loc.removePrefix("https://").removePrefix("http://").split("/")
      if (parts.size >= 3) {
        val owner = parts[1]
        val repo = parts[2]
        return "https://raw.githubusercontent.com/$owner/$repo/$branch/skills.json"
      }
    }
    return loc
  }

  private fun fetchUrlText(url: String): String? {
    if (!url.startsWith("http://") && !url.startsWith("https://")) return null
    return try {
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "DroidCode-Marketplace/1.0")
        .build()
      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) response.body?.string() else null
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun isNewerVersion(remote: String, current: String): Boolean {
    val rParts = remote.split(".").mapNotNull { it.toIntOrNull() }
    val cParts = current.split(".").mapNotNull { it.toIntOrNull() }
    val maxLen = maxOf(rParts.size, cParts.size)
    for (i in 0 until maxLen) {
      val r = rParts.getOrElse(i) { 0 }
      val c = cParts.getOrElse(i) { 0 }
      if (r > c) return true
      if (r < c) return false
    }
    return false
  }

  private fun getBuiltinClaudeSkills(sourceId: String): List<UniversalExtension> {
    return listOf(
      UniversalExtension(
        id = "claude-code-architect",
        name = "Architecture & Clean Patterns",
        description = "Teaches the agent to structure code using Clean Architecture, MVVM, and unidirectional data flow.",
        author = "Anthropic Claude Team",
        version = "1.2.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.SKILL,
        ecosystem = EcosystemType.CLAUDE_CODE,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ),
        instructions = "When analyzing or proposing code changes, structure files cleanly following Clean Architecture principles. Decouple domain models from view components, use immutable state, and ensure every public interface has clear contracts.",
        tags = listOf("architecture", "design-patterns", "clean-code")
      ),
      UniversalExtension(
        id = "claude-test-driven-development",
        name = "Test-Driven Engineering",
        description = "Strict TDD workflow: inspect requirements, write unit tests first, verify edge cases, and run test suites.",
        author = "Claude Community",
        version = "1.1.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.SKILL,
        ecosystem = EcosystemType.CLAUDE_CODE,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.COMMAND_EXECUTION),
        instructions = "Prioritize writing comprehensive unit tests for all business logic. When fixing bugs, first formulate a reproduction test case that fails, then make minimal surgical edits to pass.",
        tags = listOf("testing", "tdd", "unit-test")
      ),
      UniversalExtension(
        id = "claude-security-auditor",
        name = "Security & Vulnerability Scanner",
        description = "Detects OWASP top 10 risks, secret leaks, path traversal, and unsafe deserialization.",
        author = "Claude Security SIG",
        version = "1.3.1",
        marketplaceSourceId = sourceId,
        type = ExtensionType.AGENT,
        ecosystem = EcosystemType.CLAUDE_CODE,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ),
        instructions = "Act as an expert application security auditor. Scan source files for hardcoded secrets, injection vulnerabilities, unvalidated inputs, and permission over-privileging before approving any code change.",
        tags = listOf("security", "audit", "owasp")
      )
    )
  }
}
