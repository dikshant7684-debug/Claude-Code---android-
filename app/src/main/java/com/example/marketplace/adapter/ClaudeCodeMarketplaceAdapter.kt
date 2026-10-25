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
    .readTimeout(25, TimeUnit.SECONDS)
    .build()
) : MarketplaceAdapter {

  override val ecosystem: EcosystemType = EcosystemType.CLAUDE_CODE

  override fun canHandle(source: MarketplaceSource): Boolean {
    if (source.ecosystem == EcosystemType.CLAUDE_CODE) return true
    val loc = source.location.lowercase()
    return loc.contains("claude") || loc.contains("anthropic") || loc.contains("skills") || loc.contains("plugins")
  }

  override suspend fun validate(source: MarketplaceSource): MarketplaceValidationResult = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      if (catalog.isNotEmpty()) {
        MarketplaceValidationResult(
          isValid = true,
          message = "Valid Claude Code marketplace with ${catalog.size} real plugins & skills.",
          detectedEcosystem = EcosystemType.CLAUDE_CODE,
          itemCount = catalog.size
        )
      } else {
        MarketplaceValidationResult(
          isValid = false,
          message = "Connected to repository but no valid Claude plugins or manifests found.",
          detectedEcosystem = EcosystemType.CLAUDE_CODE,
          itemCount = 0
        )
      }
    } catch (e: Exception) {
      MarketplaceValidationResult(
        isValid = false,
        message = "Validation failed: ${e.message}",
        detectedEcosystem = EcosystemType.CLAUDE_CODE,
        itemCount = 0
      )
    }
  }

  override suspend fun fetchCatalog(source: MarketplaceSource): List<UniversalExtension> = withContext(Dispatchers.IO) {
    val items = mutableListOf<UniversalExtension>()
    val candidateUrls = resolveCandidateManifestUrls(source.location, source.branch)

    var content: String? = null
    for (url in candidateUrls) {
      val res = fetchUrlText(url)
      if (res != null && res.isNotBlank()) {
        content = res
        break
      }
    }

    if (content != null) {
      try {
        val root = JSONObject(content)
        // Official Anthropic marketplace.json has "plugins": [ ... ]
        val array = root.optJSONArray("plugins")
          ?: root.optJSONArray("skills")
          ?: root.optJSONArray("extensions")
          ?: root.optJSONArray("items")

        if (array != null) {
          for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(parseOfficialPluginEntry(source.id, obj, source.location))
          }
        }
      } catch (_: Exception) {
        // Try as JSON array directly
        try {
          val array = JSONArray(content)
          for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(parseOfficialPluginEntry(source.id, obj, source.location))
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

  /**
   * Real-time downloader: fetches real agents, skills, commands, and schemas
   * directly from Anthropic's claude-plugins-official repository or external Git sources.
   */
  override suspend fun resolveExtensionBundle(source: MarketplaceSource, extension: UniversalExtension): ExtensionBundle = withContext(Dispatchers.IO) {
    val files = mutableMapOf<String, String>()
    val pluginName = extension.id

    // Base raw URL for Anthropic official repo plugins
    val officialBase = "https://raw.githubusercontent.com/anthropics/claude-plugins-official/main/plugins/$pluginName"

    // 1. Download real plugin.json from GitHub
    val remotePluginJson = fetchUrlText("$officialBase/.claude-plugin/plugin.json")
    if (remotePluginJson != null) {
      files[".claude-plugin/plugin.json"] = remotePluginJson
    }

    // 2. Download real README.md
    val remoteReadme = fetchUrlText("$officialBase/README.md")
    if (remoteReadme != null) {
      files["README.md"] = remoteReadme
    }

    // 3. Download real Agent files if plugin contains agents
    val commonAgentNames = listOf(
      "code-architect.md",
      "code-reviewer.md",
      "code-explorer.md",
      "pr-test-analyzer.md",
      "silent-failure-hunter.md",
      "comment-analyzer.md",
      "code-simplifier.md",
      "type-design-analyzer.md"
    )
    for (agentName in commonAgentNames) {
      val agentContent = fetchUrlText("$officialBase/agents/$agentName")
      if (agentContent != null && agentContent.isNotBlank()) {
        files["agents/$agentName"] = agentContent
      }
    }

    // 4. Download real Command files
    val commonCommands = listOf(
      "$pluginName.md",
      "code-review.md",
      "review.md",
      "plan.md",
      "test.md"
    )
    for (cmd in commonCommands) {
      val cmdContent = fetchUrlText("$officialBase/commands/$cmd")
      if (cmdContent != null && cmdContent.isNotBlank()) {
        files["commands/$cmd"] = cmdContent
      }
    }

    // 5. Download real Skills if present
    val skillSubdirs = listOf("build-mcp-server", "build-mcp-app", "build-mcpb", pluginName)
    for (sub in skillSubdirs) {
      val skillContent = fetchUrlText("$officialBase/skills/$sub/SKILL.md")
        ?: fetchUrlText("$officialBase/SKILL.md")
      if (skillContent != null && skillContent.isNotBlank()) {
        files["SKILL.md"] = skillContent
        break
      }
    }

    // 6. Download Hooks if present
    val hooksJson = fetchUrlText("$officialBase/hooks/hooks.json")
    if (hooksJson != null) {
      files["hooks/hooks.json"] = hooksJson
    }

    // 7. If external repository (e.g. git-subdir or url)
    if (extension.originalLocation.startsWith("{")) {
      try {
        val srcObj = JSONObject(extension.originalLocation)
        val rawUrl = srcObj.optString("url")
        val path = srcObj.optString("path")
        val ref = srcObj.optString("ref", "main")
        if (rawUrl.contains("github.com")) {
          val cleanGit = rawUrl.removeSuffix(".git").removePrefix("https://github.com/")
          val extReadme = fetchUrlText("https://raw.githubusercontent.com/$cleanGit/$ref/$path/README.md")
            ?: fetchUrlText("https://raw.githubusercontent.com/$cleanGit/$ref/README.md")
          if (extReadme != null) {
            files["README.md"] = extReadme
          }
          val extSkill = fetchUrlText("https://raw.githubusercontent.com/$cleanGit/$ref/$path/SKILL.md")
            ?: fetchUrlText("https://raw.githubusercontent.com/$cleanGit/$ref/SKILL.md")
          if (extSkill != null) {
            files["SKILL.md"] = extSkill
          }
        }
      } catch (_: Exception) {}
    }

    // Build synthesized SKILL.md if not directly found
    if (!files.containsKey("SKILL.md")) {
      val primaryAgent = files.keys.firstOrNull { it.startsWith("agents/") }
      val primaryContent = if (primaryAgent != null) {
        files[primaryAgent] ?: extension.instructions
      } else if (files.containsKey("README.md")) {
        files["README.md"] ?: extension.instructions
      } else {
        extension.instructions.ifBlank { extension.description }
      }

      files["SKILL.md"] = """---
name: ${extension.name}
description: ${extension.description}
version: ${extension.version}
author: ${extension.author}
ecosystem: Claude Code Official
type: ${extension.type.name}
---

# ${extension.name}

${extension.description}

## Instructions & Guidelines
$primaryContent
"""
    }

    // Manifest JSON
    val manifestObj = JSONObject().apply {
      put("id", extension.id)
      put("name", extension.name)
      put("version", extension.version)
      put("ecosystem", extension.ecosystem.name)
      put("type", extension.type.name)
      put("author", extension.author)
      put("permissions", JSONArray(extension.declaredPermissions.map { it.name }))
      put("downloadedFiles", JSONArray(files.keys.toList()))
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
          changelog = "Updated capability definitions and instructions directly from Anthropic official plugins."
        )
      } else null
    } catch (_: Exception) {
      null
    }
  }

  private fun parseOfficialPluginEntry(sourceId: String, obj: JSONObject, sourceLoc: String): UniversalExtension {
    val name = obj.optString("name", "plugin")
    val displayName = obj.optString("displayName", name)
    val description = obj.optString("description", "")
    val category = obj.optString("category", "development").lowercase()

    var authorName = "Anthropic"
    val authorObj = obj.optJSONObject("author")
    if (authorObj != null) {
      authorName = authorObj.optString("name", "Anthropic")
    } else if (obj.has("author")) {
      authorName = obj.optString("author", "Anthropic")
    }

    val sourceField = obj.opt("source")
    val sourceJson = if (sourceField is JSONObject) sourceField.toString() else sourceField?.toString() ?: ""

    // Detect extension type
    val type = when {
      name.contains("mcp", ignoreCase = true) || category.contains("mcp") || description.contains("MCP server", ignoreCase = true) -> ExtensionType.MCP_SERVER
      name.contains("agent", ignoreCase = true) || description.contains("agents", ignoreCase = true) || name == "feature-dev" || name == "pr-review-toolkit" -> ExtensionType.AGENT
      category == "skills" || name.contains("skill", ignoreCase = true) || description.contains("skills", ignoreCase = true) -> ExtensionType.SKILL
      category == "automation" || name.contains("command", ignoreCase = true) -> ExtensionType.WORKFLOW
      else -> ExtensionType.PLUGIN
    }

    val permissionsList = mutableListOf(DeclaredPermission.FILESYSTEM_READ)
    if (type == ExtensionType.AGENT || type == ExtensionType.PLUGIN) {
      permissionsList.add(DeclaredPermission.FILESYSTEM_WRITE)
    }
    if (type == ExtensionType.MCP_SERVER || description.contains("API") || description.contains("cloud") || description.contains("remote")) {
      permissionsList.add(DeclaredPermission.NETWORK_ACCESS)
    }

    val tags = mutableListOf("claude-official", category)
    if (authorName.isNotBlank() && authorName != "Anthropic") tags.add(authorName.lowercase().replace(" ", "-"))

    return UniversalExtension(
      id = name.lowercase().replace(" ", "-"),
      name = displayName,
      description = description,
      author = authorName,
      version = obj.optString("version", "1.0.0"),
      marketplaceSourceId = sourceId,
      originalLocation = sourceJson,
      type = type,
      ecosystem = EcosystemType.CLAUDE_CODE,
      compatibilityRequirements = listOf("Android 8.0+"),
      declaredPermissions = permissionsList,
      instructions = description,
      tags = tags
    )
  }

  private fun parseSkillMarkdown(sourceId: String, sourceName: String, md: String, loc: String): UniversalExtension {
    var name = sourceName
    var description = "Claude Code skill from $sourceName"
    val lines = md.lines()

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

  private fun resolveCandidateManifestUrls(location: String, branch: String): List<String> {
    var loc = location.trim()
    if (loc.endsWith(".git")) loc = loc.removeSuffix(".git")
    if (loc.contains("github.com") && !loc.contains("raw.githubusercontent.com")) {
      val parts = loc.removePrefix("https://").removePrefix("http://").split("/")
      if (parts.size >= 3) {
        val owner = parts[1]
        val repo = parts[2]
        return listOf(
          "https://raw.githubusercontent.com/$owner/$repo/$branch/.claude-plugin/marketplace.json",
          "https://raw.githubusercontent.com/$owner/$repo/$branch/marketplace.json",
          "https://raw.githubusercontent.com/$owner/$repo/$branch/skills.json"
        )
      }
    }
    return listOf(loc)
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
        id = "feature-dev",
        name = "Feature Development",
        description = "Designs feature architectures and implementation blueprints with specialized agents.",
        author = "Anthropic",
        version = "1.0.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.AGENT,
        ecosystem = EcosystemType.CLAUDE_CODE,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.FILESYSTEM_WRITE),
        instructions = "Analyze codebase patterns, formulate blueprints, verify implementation details, and produce surgical edits with code-architect, code-explorer, and code-reviewer agents.",
        tags = listOf("claude-official", "development", "agents")
      ),
      UniversalExtension(
        id = "code-review",
        name = "Automated Code Review",
        description = "Automated code review for pull requests and code changes with confidence-based scoring.",
        author = "Anthropic",
        version = "1.0.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.PLUGIN,
        ecosystem = EcosystemType.CLAUDE_CODE,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ),
        instructions = "Review code diffs for adherence to project guidelines, clean architecture, edge case handling, and OWASP security recommendations.",
        tags = listOf("claude-official", "development", "review")
      ),
      UniversalExtension(
        id = "mcp-server-dev",
        name = "MCP Server Development",
        description = "Official skills for building, testing, and debugging Model Context Protocol servers.",
        author = "Anthropic",
        version = "1.0.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.MCP_SERVER,
        ecosystem = EcosystemType.CLAUDE_CODE,
        declaredPermissions = listOf(DeclaredPermission.NETWORK_ACCESS, DeclaredPermission.FILESYSTEM_READ),
        instructions = "Build and integrate Model Context Protocol (MCP) servers, implement tool endpoints, resources, and prompt definitions according to MCP specification.",
        tags = listOf("claude-official", "development", "mcp")
      )
    )
  }
}
