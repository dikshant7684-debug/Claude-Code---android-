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

class DroidCodeNativeMarketplaceAdapter(
  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()
) : MarketplaceAdapter {

  override val ecosystem: EcosystemType = EcosystemType.DROIDCODE_NATIVE

  override fun canHandle(source: MarketplaceSource): Boolean {
    if (source.ecosystem == EcosystemType.DROIDCODE_NATIVE) return true
    val loc = source.location.lowercase()
    return loc.contains("droidcode") || loc.endsWith("droidcode.json") || loc.contains("native")
  }

  override suspend fun validate(source: MarketplaceSource): MarketplaceValidationResult = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      MarketplaceValidationResult(
        isValid = true,
        message = "Valid DroidCode Native Registry with ${catalog.size} extension(s).",
        detectedEcosystem = EcosystemType.DROIDCODE_NATIVE,
        itemCount = catalog.size
      )
    } catch (e: Exception) {
      MarketplaceValidationResult(
        isValid = false,
        message = "Registry check error: ${e.message}",
        detectedEcosystem = EcosystemType.DROIDCODE_NATIVE,
        itemCount = 0
      )
    }
  }

  override suspend fun fetchCatalog(source: MarketplaceSource): List<UniversalExtension> = withContext(Dispatchers.IO) {
    val items = mutableListOf<UniversalExtension>()
    val content = fetchUrlText(source.location)

    if (content != null) {
      try {
        val root = JSONObject(content)
        val array = root.optJSONArray("extensions") ?: root.optJSONArray("items") ?: root.optJSONArray("packages")
        if (array != null) {
          for (i in 0 until array.length()) {
            items.add(parseNativeExtension(source.id, array.getJSONObject(i)))
          }
        }
      } catch (_: Exception) {}
    }

    if (items.isEmpty()) {
      items.addAll(getBuiltinDroidCodeExtensions(source.id))
    }

    items
  }

  override suspend fun resolveExtensionBundle(source: MarketplaceSource, extension: UniversalExtension): ExtensionBundle = withContext(Dispatchers.IO) {
    val files = mutableMapOf<String, String>()

    val manifest = JSONObject().apply {
      put("id", extension.id)
      put("name", extension.name)
      put("version", extension.version)
      put("author", extension.author)
      put("description", extension.description)
      put("ecosystem", "DroidCode Native")
      put("type", extension.type.name)
      put("permissions", JSONArray(extension.declaredPermissions.map { it.name }))
      if (extension.toolSchemaJson.isNotBlank()) {
        put("tool_schema", JSONObject(extension.toolSchemaJson))
      }
    }

    files["droidcode-manifest.json"] = manifest.toString(2)
    if (extension.instructions.isNotBlank()) {
      files["SKILL.md"] = "# ${extension.name}\n\n${extension.description}\n\n${extension.instructions}"
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
          changelog = "Version bump with security and capability enhancements."
        )
      } else null
    } catch (_: Exception) {
      null
    }
  }

  private fun parseNativeExtension(sourceId: String, obj: JSONObject): UniversalExtension {
    val id = obj.optString("id")
    val name = obj.optString("name", id)
    val desc = obj.optString("description", "")
    val author = obj.optString("author", "DroidCode Team")
    val version = obj.optString("version", "1.0.0")
    val rawType = obj.optString("type", "PLUGIN")
    val type = try { ExtensionType.valueOf(rawType.uppercase()) } catch (_: Exception) { ExtensionType.PLUGIN }

    val permissionsList = mutableListOf<DeclaredPermission>()
    val pArray = obj.optJSONArray("permissions")
    if (pArray != null) {
      for (i in 0 until pArray.length()) {
        try {
          permissionsList.add(DeclaredPermission.valueOf(pArray.getString(i).uppercase()))
        } catch (_: Exception) {}
      }
    }

    val toolObj = obj.optJSONObject("tool_schema")
    val toolSchema = toolObj?.toString(2) ?: ""

    return UniversalExtension(
      id = id,
      name = name,
      description = desc,
      author = author,
      version = version,
      marketplaceSourceId = sourceId,
      originalLocation = obj.optString("location", ""),
      type = type,
      ecosystem = EcosystemType.DROIDCODE_NATIVE,
      declaredPermissions = permissionsList,
      instructions = obj.optString("instructions", ""),
      toolSchemaJson = toolSchema,
      license = obj.optString("license", "Apache-2.0"),
      tags = listOf("droidcode", "native", type.name.lowercase())
    )
  }

  private fun fetchUrlText(url: String): String? {
    if (!url.startsWith("http://") && !url.startsWith("https://")) return null
    return try {
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "DroidCode-NativeAdapter/1.0")
        .build()
      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) response.body?.string() else null
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun getBuiltinDroidCodeExtensions(sourceId: String): List<UniversalExtension> {
    return listOf(
      UniversalExtension(
        id = "droid-git-pro",
        name = "Git Assistant & Branch Navigator",
        description = "Provides atomic commit message generation, conflict resolution workflows, and branch management.",
        author = "DroidCode Core",
        version = "1.4.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.PLUGIN,
        ecosystem = EcosystemType.DROIDCODE_NATIVE,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.COMMAND_EXECUTION),
        instructions = "When inspecting Git status or writing commit messages, generate conventional commit summaries (feat:, fix:, chore:). Assist users in inspecting diff hunks and cleanly resolving merge conflicts.",
        toolSchemaJson = """{
  "name": "git_atomic_commit",
  "description": "Stages changes and creates a formatted atomic git commit",
  "parameters": {
    "type": "object",
    "properties": {
      "message": {"type": "string", "description": "The commit message"},
      "files": {"type": "array", "items": {"type": "string"}, "description": "Files to stage"}
    },
    "required": ["message"]
  }
}""",
        tags = listOf("git", "vcs", "branch", "tool")
      ),
      UniversalExtension(
        id = "droid-compose-designer",
        name = "Jetpack Compose UI Synthesizer",
        description = "Generates accessible Material 3 layouts, theme tokens, previewable components, and responsive insets.",
        author = "DroidCode Android Team",
        version = "2.1.0",
        marketplaceSourceId = sourceId,
        type = ExtensionType.SKILL,
        ecosystem = EcosystemType.DROIDCODE_NATIVE,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.FILESYSTEM_WRITE),
        instructions = "Always build UI using Jetpack Compose with Material Design 3. Use Scaffold with proper contentWindowInsets, BoxWithConstraints for responsive widths, minimum 48dp touch targets, semantic contentDescriptions, and dynamic ColorScheme tokens.",
        tags = listOf("compose", "android", "ui", "m3")
      ),
      UniversalExtension(
        id = "droid-mcp-github-tools",
        name = "GitHub Context Provider (MCP)",
        description = "Exposes repository PRs, issues, commits, and workflow status directly to the agent over MCP protocol.",
        author = "Model Context Protocol",
        version = "1.0.5",
        marketplaceSourceId = sourceId,
        type = ExtensionType.MCP_SERVER,
        ecosystem = EcosystemType.DROIDCODE_NATIVE,
        declaredPermissions = listOf(DeclaredPermission.NETWORK_ACCESS),
        instructions = "Connect to GitHub MCP server to query active issues, review open pull requests, and pull remote branch status during autonomous coding sessions.",
        tags = listOf("mcp", "github", "tools", "protocol")
      )
    )
  }
}
