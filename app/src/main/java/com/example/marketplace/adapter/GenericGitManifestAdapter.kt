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

class GenericGitManifestAdapter(
  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()
) : MarketplaceAdapter {

  override val ecosystem: EcosystemType = EcosystemType.GENERIC

  override fun canHandle(source: MarketplaceSource): Boolean {
    // Fallback adapter for any Git / HTTPS manifest
    return true
  }

  override suspend fun validate(source: MarketplaceSource): MarketplaceValidationResult = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      if (catalog.isNotEmpty()) {
        MarketplaceValidationResult(
          isValid = true,
          message = "Valid manifest with ${catalog.size} item(s) found.",
          detectedEcosystem = EcosystemType.GENERIC,
          itemCount = catalog.size
        )
      } else {
        MarketplaceValidationResult(
          isValid = false,
          message = "Could not locate a valid manifest.json or skills repository at this location.",
          detectedEcosystem = EcosystemType.GENERIC,
          itemCount = 0
        )
      }
    } catch (e: Exception) {
      MarketplaceValidationResult(
        isValid = false,
        message = "Connection error: ${e.message}",
        detectedEcosystem = EcosystemType.GENERIC,
        itemCount = 0
      )
    }
  }

  override suspend fun fetchCatalog(source: MarketplaceSource): List<UniversalExtension> = withContext(Dispatchers.IO) {
    val items = mutableListOf<UniversalExtension>()
    val loc = source.location.trim()

    val content = if (loc.startsWith("http://") || loc.startsWith("https://")) {
      fetchUrlText(loc)
    } else null

    if (content != null) {
      try {
        val root = JSONObject(content)
        val array = root.optJSONArray("extensions") ?: root.optJSONArray("items") ?: root.optJSONArray("skills")
        if (array != null) {
          for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(parseGenericExtension(source.id, obj))
          }
        }
      } catch (_: Exception) {
        try {
          val array = JSONArray(content)
          for (i in 0 until array.length()) {
            items.add(parseGenericExtension(source.id, array.getJSONObject(i)))
          }
        } catch (_: Exception) {}
      }
    }

    if (items.isEmpty()) {
      items.addAll(getBuiltinGenericExtensions(source.id))
    }

    items
  }

  override suspend fun resolveExtensionBundle(source: MarketplaceSource, extension: UniversalExtension): ExtensionBundle = withContext(Dispatchers.IO) {
    val files = mutableMapOf<String, String>()
    val manifest = JSONObject().apply {
      put("id", extension.id)
      put("name", extension.name)
      put("version", extension.version)
      put("description", extension.description)
      put("author", extension.author)
      put("ecosystem", "Generic")
    }
    files["manifest.json"] = manifest.toString(2)
    if (extension.instructions.isNotBlank()) {
      files["README.md"] = "# ${extension.name}\n\n${extension.instructions}"
    }
    ExtensionBundle(extension, files)
  }

  override suspend fun checkForUpdates(installed: InstalledExtension, source: MarketplaceSource): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
      val catalog = fetchCatalog(source)
      val remote = catalog.find { it.id == installed.id } ?: return@withContext null
      if (remote.version != installed.version) {
        UpdateInfo(installed.id, installed.version, remote.version, "Generic manifest update.")
      } else null
    } catch (_: Exception) {
      null
    }
  }

  private fun parseGenericExtension(sourceId: String, obj: JSONObject): UniversalExtension {
    val id = obj.optString("id", obj.optString("name").lowercase().replace(" ", "-"))
    val name = obj.optString("name", id)
    val desc = obj.optString("description", "")
    val author = obj.optString("author", "Community")
    val version = obj.optString("version", "1.0.0")

    return UniversalExtension(
      id = id,
      name = name,
      description = desc,
      author = author,
      version = version,
      marketplaceSourceId = sourceId,
      type = ExtensionType.SKILL,
      ecosystem = EcosystemType.GENERIC,
      declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ),
      instructions = obj.optString("instructions", desc)
    )
  }

  private fun fetchUrlText(url: String): String? {
    return try {
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "DroidCode-GenericAdapter/1.0")
        .build()
      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) response.body?.string() else null
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun getBuiltinGenericExtensions(sourceId: String): List<UniversalExtension> {
    return listOf(
      UniversalExtension(
        id = "generic-documentation-generator",
        name = "Automated Markdown Docs & Changelog",
        description = "Generates technical documentation, API tables, and Keep-A-Changelog entries.",
        author = "Open Source Community",
        version = "1.0.1",
        marketplaceSourceId = sourceId,
        type = ExtensionType.SKILL,
        ecosystem = EcosystemType.GENERIC,
        declaredPermissions = listOf(DeclaredPermission.FILESYSTEM_READ, DeclaredPermission.FILESYSTEM_WRITE),
        instructions = "When asked for documentation or changelogs, follow Keep-a-Changelog standard (Added, Changed, Deprecated, Removed, Fixed, Security). Generate clean Markdown with clear code signatures.",
        tags = listOf("docs", "markdown", "changelog")
      )
    )
  }
}
