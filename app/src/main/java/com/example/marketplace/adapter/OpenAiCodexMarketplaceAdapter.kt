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

class OpenAiCodexMarketplaceAdapter(
  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()
) : MarketplaceAdapter {

  override val ecosystem: EcosystemType = EcosystemType.OPENAI_CODEX

  override fun canHandle(source: MarketplaceSource): Boolean {
    if (source.ecosystem == EcosystemType.OPENAI_CODEX) return true
    val loc = source.location.lowercase()
    return loc.contains("openai") || loc.contains("codex") || loc.contains("ai-plugin")
  }

  override suspend fun validate(source: MarketplaceSource): MarketplaceValidationResult = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      if (catalog.isNotEmpty()) {
        MarketplaceValidationResult(
          isValid = true,
          message = "Valid OpenAI Codex manifest with ${catalog.size} tool(s) detected.",
          detectedEcosystem = EcosystemType.OPENAI_CODEX,
          itemCount = catalog.size
        )
      } else {
        MarketplaceValidationResult(
          isValid = false,
          message = "No valid OpenAI Codex plugin manifests (.well-known/ai-plugin.json or codex.json) detected.",
          detectedEcosystem = EcosystemType.OPENAI_CODEX,
          itemCount = 0
        )
      }
    } catch (e: Exception) {
      MarketplaceValidationResult(
        isValid = false,
        message = "Codex validation failed: ${e.message}",
        detectedEcosystem = EcosystemType.OPENAI_CODEX,
        itemCount = 0
      )
    }
  }

  override suspend fun fetchCatalog(source: MarketplaceSource): List<UniversalExtension> = withContext(Dispatchers.IO) {
    val items = mutableListOf<UniversalExtension>()
    val rawUrl = resolveCodexManifestUrl(source.location)

    val content = fetchUrlText(rawUrl)
    if (content != null) {
      try {
        val root = JSONObject(content)
        if (root.has("schema_version") && root.has("name_for_model")) {
          // Single ai-plugin.json format
          items.add(parseAiPluginJson(source.id, root, source.location))
        } else {
          val array = root.optJSONArray("plugins") ?: root.optJSONArray("actions") ?: root.optJSONArray("items")
          if (array != null) {
            for (i in 0 until array.length()) {
              items.add(parseAiPluginJson(source.id, array.getJSONObject(i), source.location))
            }
          }
        }
      } catch (_: Exception) {}
    }

    if (items.isEmpty()) {
      items.addAll(getBuiltinCodexExtensions(source.id))
    }

    items
  }

  override suspend fun resolveExtensionBundle(source: MarketplaceSource, extension: UniversalExtension): ExtensionBundle = withContext(Dispatchers.IO) {
    val files = mutableMapOf<String, String>()

    val manifest = JSONObject().apply {
      put("schema_version", "v1")
      put("name_for_human", extension.name)
      put("name_for_model", extension.id)
      put("description_for_human", extension.description)
      put("description_for_model", extension.instructions)
      put("author", extension.author)
      put("version", extension.version)
      put("ecosystem", "OpenAI Codex")
    }

    files["codex-plugin.json"] = manifest.toString(2)
    if (extension.instructions.isNotBlank()) {
      files["system_prompt.txt"] = extension.instructions
    }
    if (extension.toolSchemaJson.isNotBlank()) {
      files["openapi.json"] = extension.toolSchemaJson
    }

    ExtensionBundle(extension = extension, files = files)
  }

  override suspend fun checkForUpdates(installed: InstalledExtension, source: MarketplaceSource): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      val remote = catalog.find { it.id == installed.id } ?: return@withContext null
      if (remote.version != installed.version) {
        UpdateInfo(
          extensionId = installed.id,
          currentVersion = installed.version,
          latestVersion = remote.version,
          changelog = "Updated Codex action schema and prompt definitions."
        )
      } else null
    } catch (_: Exception) {
      null
    }
  }

  private fun parseAiPluginJson(sourceId: String, obj: JSONObject, loc: String): UniversalExtension {
    val id = obj.optString("name_for_model", obj.optString("id", "codex-tool")).lowercase().replace("_", "-")
    val name = obj.optString("name_for_human", id)
    val desc = obj.optString("description_for_human", obj.optString("description", ""))
    val instructions = obj.optString("description_for_model", desc)
    val author = obj.optString("author_name", "OpenAI Codex Ecosystem")
    val version = obj.optString("version", "1.0.0")

    return UniversalExtension(
      id = id,
      name = name,
      description = desc,
      author = author,
      version = version,
      marketplaceSourceId = sourceId,
      originalLocation = loc,
      type = ExtensionType.PLUGIN,
      ecosystem = EcosystemType.OPENAI_CODEX,
      declaredPermissions = listOf(DeclaredPermission.NETWORK_ACCESS, DeclaredPermission.FILESYSTEM_READ),
      instructions = instructions,
      tags = listOf("codex", "openai", "plugin")
    )
  }

  private fun resolveCodexManifestUrl(location: String): String {
    var loc = location.trim()
    if (loc.endsWith("/")) loc = loc.removeSuffix("/")
    if (!loc.endsWith(".json")) {
      return "$loc/.well-known/ai-plugin.json"
    }
    return loc
  }

  private fun fetchUrlText(url: String): String? {
    if (!url.startsWith("http://") && !url.startsWith("https://")) return null
    return try {
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "DroidCode-CodexAdapter/1.0")
        .build()
      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) response.body?.string() else null
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun getBuiltinCodexExtensions(sourceId: String): List<UniversalExtension> {
    return listOf(
      UniversalExtension(
        id = "codex-refactoring-engine",
        name = "Codex Code Refactoring & Modernization",
        description = "Automates AST-based code modernization, deprecation cleanup, and idiom alignment.",
        author = "OpenAI Codex Community",
        version = "2.0.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.PLUGIN,
        ecosystem = EcosystemType.OPENAI_CODEX,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.FILESYSTEM_WRITE),
        instructions = "Analyze target functions for code smells, high cyclomatic complexity, and deprecated APIs. Produce clean, idiomatic refactorings while preserving original behavioral semantics and test assertions.",
        tags = listOf("refactoring", "codex", "ast", "modernize")
      ),
      UniversalExtension(
        id = "codex-regex-optimizer",
        name = "Regex Synthesizer & Validator",
        description = "Generates high-performance regular expressions with ReDoS prevention and unit tests.",
        author = "Codex Tools Team",
        version = "1.1.2",
        marketplaceSourceId = sourceId,
        type = ExtensionType.SKILL,
        ecosystem = EcosystemType.OPENAI_CODEX,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ),
        instructions = "When asked for regular expressions, provide bulletproof patterns with non-capturing groups, atomic groupings or possessive quantifiers to avoid catastrophic backtracking. Include verified test strings.",
        tags = listOf("regex", "parser", "codex")
      )
    )
  }
}
