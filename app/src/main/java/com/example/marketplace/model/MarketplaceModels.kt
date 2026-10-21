package com.example.marketplace.model

import org.json.JSONObject
import java.io.Serializable

enum class MarketplaceSourceType(val displayName: String) {
  GITHUB("GitHub Repository"),
  GITLAB("GitLab Repository"),
  GIT_REPO("Generic Git"),
  HTTPS_MANIFEST("HTTPS Manifest URL"),
  DROIDCODE_NATIVE("DroidCode Registry"),
  LOCAL_IMPORT("Local Package")
}

enum class EcosystemType(val displayName: String, val badgeColorHex: Long) {
  CLAUDE_CODE("Claude Code", 0xFFD95B26),       // Warm terracotta
  OPENAI_CODEX("OpenAI Codex", 0xFF10A37F),     // Codex green
  DROIDCODE_NATIVE("DroidCode Native", 0xFF0097A7), // Cyan
  GENERIC("Generic Agent", 0xFF607D8B)          // Slate
}

enum class ExtensionType(val displayName: String) {
  SKILL("Skill"),
  AGENT("Agent Definition"),
  PLUGIN("Executable Plugin"),
  MCP_SERVER("MCP Tool Provider"),
  CONNECTOR("Service Connector"),
  WORKFLOW("Workflow Script"),
  HOOK("Lifecycle Hook")
}

enum class DeclaredPermission(val displayName: String, val description: String, val isDangerous: Boolean) {
  FILESYSTEM_READ("Read Project Files", "Allow reading source files in active workspace", false),
  FILESYSTEM_WRITE("Modify Project Files", "Allow editing and creating files in active workspace", true),
  NETWORK_ACCESS("Network Access", "Allow connecting to remote HTTP/API endpoints", true),
  COMMAND_EXECUTION("Run Build Commands", "Allow running permitted terminal commands or tests", true),
  STORAGE_PERSISTENCE("Storage Access", "Allow saving configuration and cache in app directory", false),
  ENVIRONMENT_READ("Read Environment", "Allow inspecting platform and project metadata", false)
}

enum class ValidationStatus {
  VALID,
  INVALID,
  PENDING,
  ERROR
}

data class MarketplaceSource(
  val id: String,
  val name: String,
  val sourceType: MarketplaceSourceType,
  val location: String,
  val branch: String = "main",
  val authRef: String? = null,
  val ecosystem: EcosystemType = EcosystemType.DROIDCODE_NATIVE,
  val isEnabled: Boolean = true,
  val lastRefreshTimestamp: Long = 0L,
  val validationStatus: ValidationStatus = ValidationStatus.PENDING,
  val errorDetails: String? = null,
  val etag: String? = null
) : Serializable

data class UniversalExtension(
  val id: String,
  val name: String,
  val description: String,
  val author: String = "Community",
  val version: String = "1.0.0",
  val marketplaceSourceId: String,
  val originalLocation: String = "",
  val type: ExtensionType = ExtensionType.SKILL,
  val ecosystem: EcosystemType = EcosystemType.DROIDCODE_NATIVE,
  val compatibilityRequirements: List<String> = emptyList(),
  val declaredPermissions: List<DeclaredPermission> = emptyList(),
  val dependencies: List<String> = emptyList(),
  val instructions: String = "",
  val toolSchemaJson: String = "",
  val license: String = "MIT",
  val downloadUrl: String = "",
  val sha256Hash: String = "",
  val tags: List<String> = emptyList(),
  val examples: List<String> = emptyList()
) : Serializable

data class InstalledExtension(
  val id: String,
  val name: String,
  val type: ExtensionType,
  val version: String,
  val sourceMarketplaceId: String,
  val installPath: String,
  val isEnabled: Boolean = true,
  val grantedPermissions: Set<DeclaredPermission> = emptySet(),
  val manifestJson: String = "",
  val installedTimestamp: Long = System.currentTimeMillis(),
  val hasUpdate: Boolean = false,
  val availableUpdateVersion: String? = null
) : Serializable

data class MarketplaceValidationResult(
  val isValid: Boolean,
  val message: String,
  val detectedEcosystem: EcosystemType = EcosystemType.DROIDCODE_NATIVE,
  val itemCount: Int = 0
)

data class ExtensionBundle(
  val extension: UniversalExtension,
  val files: Map<String, String> = emptyMap()
)

data class DiscoveredCapability(
  val extensionId: String,
  val name: String,
  val type: ExtensionType,
  val relevanceScore: Float,
  val matchingKeywords: List<String>,
  val instructions: String = "",
  val toolSchema: JSONObject? = null
)
