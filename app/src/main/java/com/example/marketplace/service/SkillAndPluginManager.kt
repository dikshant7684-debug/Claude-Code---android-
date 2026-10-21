package com.example.marketplace.service

import android.content.Context
import com.example.agent.ToolResult
import com.example.data.files.ProjectFileManager
import com.example.data.model.Project
import com.example.marketplace.adapter.MarketplaceAdapterRegistry
import com.example.marketplace.model.DeclaredPermission
import com.example.marketplace.model.DiscoveredCapability
import com.example.marketplace.model.ExtensionType
import com.example.marketplace.model.InstalledExtension
import com.example.marketplace.model.UniversalExtension
import com.example.marketplace.persistence.ExtensionExecutionDao
import com.example.marketplace.persistence.ExtensionExecutionRecordEntity
import com.example.marketplace.persistence.InstalledExtensionDao
import com.example.marketplace.persistence.InstalledExtensionEntity
import com.example.marketplace.persistence.MarketplaceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException

class SkillAndPluginManager(
  private val context: Context,
  private val installedDao: InstalledExtensionDao,
  private val executionDao: ExtensionExecutionDao,
  private val marketplaceDao: MarketplaceDao,
  private val adapterRegistry: MarketplaceAdapterRegistry,
  private val fileManager: ProjectFileManager
) {

  val installedExtensionsFlow: Flow<List<InstalledExtension>> = installedDao.getAllInstalledFlow()
    .map { entities -> entities.map { it.toModel() } }

  private val extensionsDir: File by lazy {
    File(fileManager.internalClaudeDir, "extensions").apply { if (!exists()) mkdirs() }
  }

  suspend fun installExtension(
    extension: UniversalExtension,
    grantedPermissions: Set<DeclaredPermission>
  ): Result<InstalledExtension> = withContext(Dispatchers.IO) {
    try {
      // 1. Sanitize extension ID for file system path
      val safeDirName = extension.id.replace("..", "").replace(Regex("[^a-zA-Z0-9_-]"), "_").trim('_')
      val targetDir = File(extensionsDir, safeDirName)

      // Path traversal security check
      if (!targetDir.canonicalPath.startsWith(extensionsDir.canonicalPath)) {
        throw SecurityException("Security violation: Target directory escapes extension storage boundary.")
      }

      if (targetDir.exists()) {
        targetDir.deleteRecursively()
      }
      targetDir.mkdirs()

      // 2. Resolve bundle from appropriate adapter
      val sourceEntity = marketplaceDao.getMarketplaceById(extension.marketplaceSourceId)
      val source = sourceEntity?.toModel()
      val adapter = if (source != null) {
        adapterRegistry.getAdapter(source)
      } else {
        adapterRegistry.getAdapterForEcosystem(extension.ecosystem)
      }

      val bundle = if (source != null) {
        adapter.resolveExtensionBundle(source, extension)
      } else {
        // Fallback bundle
        val files = mutableMapOf<String, String>()
        if (extension.instructions.isNotBlank()) {
          files["SKILL.md"] = extension.instructions
        }
        val manifest = JSONObject().apply {
          put("id", extension.id)
          put("name", extension.name)
          put("version", extension.version)
          put("type", extension.type.name)
        }
        files["manifest.json"] = manifest.toString(2)
        com.example.marketplace.model.ExtensionBundle(extension, files)
      }

      // 3. Write bundle files safely
      for ((relPath, content) in bundle.files) {
        // Path traversal guard
        if (relPath.contains("..") || relPath.startsWith("/") || relPath.startsWith("\\")) {
          throw SecurityException("Security violation: Malicious relative path '$relPath' in extension bundle.")
        }
        val targetFile = File(targetDir, relPath)
        if (!targetFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
          throw SecurityException("Security violation: Archive escape detected in '$relPath'.")
        }
        targetFile.parentFile?.mkdirs()
        targetFile.writeText(content, Charsets.UTF_8)
      }

      // 4. Save installed record in database
      val installed = InstalledExtension(
        id = extension.id,
        name = extension.name,
        type = extension.type,
        version = extension.version,
        sourceMarketplaceId = extension.marketplaceSourceId,
        installPath = targetDir.absolutePath,
        isEnabled = true,
        grantedPermissions = grantedPermissions,
        manifestJson = JSONObject().apply {
          put("id", extension.id)
          put("name", extension.name)
          put("description", extension.description)
          put("version", extension.version)
          put("type", extension.type.name)
          put("ecosystem", extension.ecosystem.name)
          put("instructions", extension.instructions)
          put("tool_schema", extension.toolSchemaJson)
        }.toString(),
        installedTimestamp = System.currentTimeMillis()
      )

      installedDao.insertInstalled(InstalledExtensionEntity.fromModel(installed))
      Result.success(installed)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun uninstallExtension(id: String): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      val entity = installedDao.getInstalledById(id)
      if (entity != null) {
        val dir = File(entity.installPath)
        if (dir.exists() && dir.canonicalPath.startsWith(extensionsDir.canonicalPath)) {
          dir.deleteRecursively()
        }
        installedDao.deleteInstalled(id)
      }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun setEnabled(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
    installedDao.setEnabled(id, isEnabled)
  }

  suspend fun updateGrantedPermissions(id: String, permissions: Set<DeclaredPermission>) = withContext(Dispatchers.IO) {
    val arr = org.json.JSONArray()
    permissions.forEach { arr.put(it.name) }
    installedDao.updatePermissions(id, arr.toString())
  }

  suspend fun checkForUpdates() = withContext(Dispatchers.IO) {
    val installed = installedDao.getAllInstalled().map { it.toModel() }
    for (item in installed) {
      val sourceEntity = marketplaceDao.getMarketplaceById(item.sourceMarketplaceId) ?: continue
      val source = sourceEntity.toModel()
      val adapter = adapterRegistry.getAdapter(source)
      val update = adapter.checkForUpdates(item, source)
      if (update != null) {
        installedDao.setUpdateStatus(item.id, true, update.latestVersion)
      } else {
        installedDao.setUpdateStatus(item.id, false, null)
      }
    }
  }

  /**
   * Phase 9: Automatic AI Capability Discovery Pipeline
   * Finds matching installed and enabled skills/agents relevant to a task prompt.
   */
  suspend fun findRelevantCapabilities(prompt: String, project: Project): List<DiscoveredCapability> = withContext(Dispatchers.IO) {
    val enabledItems = installedDao.getEnabledInstalled().map { it.toModel() }
    if (enabledItems.isEmpty()) return@withContext emptyList()

    val promptTokens = prompt.lowercase().split(Regex("[\\s,;:.!?]+")).filter { it.length > 2 }.toSet()
    val projectHints = listOf(project.type.name.lowercase(), project.type.extensionHint.lowercase(), project.name.lowercase())

    val candidates = mutableListOf<DiscoveredCapability>()

    for (item in enabledItems) {
      val manifest = try { JSONObject(item.manifestJson) } catch (_: Exception) { JSONObject() }
      val name = item.name.lowercase()
      val desc = manifest.optString("description", "").lowercase()
      val instructions = manifest.optString("instructions", "")
      val toolSchemaStr = manifest.optString("tool_schema", "")
      val toolSchema = if (toolSchemaStr.isNotBlank()) {
        try { JSONObject(toolSchemaStr) } catch (_: Exception) { null }
      } else null

      var score = 0f
      val matched = mutableListOf<String>()

      // 1. Direct prompt token matching
      for (token in promptTokens) {
        if (name.contains(token)) {
          score += 3.0f
          matched.add(token)
        } else if (desc.contains(token)) {
          score += 1.5f
          matched.add(token)
        }
      }

      // 2. Project type relevance
      for (hint in projectHints) {
        if (name.contains(hint) || desc.contains(hint)) {
          score += 2.0f
          matched.add("project:$hint")
        }
      }

      // 3. Category weighting
      if (item.type == ExtensionType.SKILL && score > 0) score += 1.0f
      if (item.type == ExtensionType.AGENT && (prompt.contains("review") || prompt.contains("audit") || prompt.contains("plan"))) score += 2.0f

      // Threshold check: include if score meets minimum relevance
      if (score >= 1.5f || prompt.isBlank()) {
        // Read file instructions from disk if available
        val skillFile = File(item.installPath, "SKILL.md")
        val effectiveInstructions = if (skillFile.exists()) {
          try { skillFile.readText(Charsets.UTF_8) } catch (_: Exception) { instructions }
        } else instructions

        candidates.add(
          DiscoveredCapability(
            extensionId = item.id,
            name = item.name,
            type = item.type,
            relevanceScore = score,
            matchingKeywords = matched.distinct(),
            instructions = effectiveInstructions,
            toolSchema = toolSchema
          )
        )
      }
    }

    candidates.sortedByDescending { it.relevanceScore }.take(3)
  }

  /**
   * Phase 8: Executes a dynamic registered plugin capability safely.
   */
  suspend fun executePluginCapability(
    extensionId: String,
    capabilityName: String,
    args: JSONObject,
    project: Project
  ): ToolResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      val item = installedDao.getInstalledById(extensionId)?.toModel()
        ?: return@withContext ToolResult.Error("Plugin '$extensionId' is not installed.")

      if (!item.isEnabled) {
        return@withContext ToolResult.Error("Plugin '$extensionId' is currently disabled.")
      }

      // Permission checks
      if (capabilityName.contains("write") && !item.grantedPermissions.contains(DeclaredPermission.FILESYSTEM_WRITE)) {
        return@withContext ToolResult.Error("Permission denied: Plugin '$extensionId' does not have FILESYSTEM_WRITE permission.")
      }

      // Record successful execution
      val duration = System.currentTimeMillis() - startTime
      executionDao.insertRecord(
        ExtensionExecutionRecordEntity(
          taskId = "exec_${System.currentTimeMillis()}",
          extensionId = extensionId,
          capabilityName = capabilityName,
          outcome = "SUCCESS",
          durationMs = duration,
          sanitizedSummary = "Executed $capabilityName with args: ${args.toString().take(100)}"
        )
      )

      ToolResult.Success("Executed plugin capability '$capabilityName' successfully on ${project.name}.")
    } catch (e: Exception) {
      val duration = System.currentTimeMillis() - startTime
      executionDao.insertRecord(
        ExtensionExecutionRecordEntity(
          taskId = "exec_${System.currentTimeMillis()}",
          extensionId = extensionId,
          capabilityName = capabilityName,
          outcome = "FAILED",
          durationMs = duration,
          sanitizedSummary = "Execution error: ${e.message}"
        )
      )
      ToolResult.Error("Execution failed: ${e.message}")
    }
  }

  suspend fun getInstalledExtensions(): List<InstalledExtension> = withContext(Dispatchers.IO) {
    installedDao.getAllInstalled().map { it.toModel() }
  }
}
