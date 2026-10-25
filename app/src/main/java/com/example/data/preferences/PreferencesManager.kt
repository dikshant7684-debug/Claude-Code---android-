package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ProviderConfig
import com.example.data.model.ProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "droidcode_preferences")

data class EditorPreferences(
  val fontSizeSp: Int = 13,
  val showLineNumbers: Boolean = true,
  val wordWrap: Boolean = false,
  val tabSize: Int = 4
)

data class GeneralPreferences(
  val theme: String = "LIGHT", // "LIGHT", "DARK", "SYSTEM"
  val autoOpenWorkspace: Boolean = true,
  val codeCompletionEnabled: Boolean = true,
  val editorToolbarEnabled: Boolean = true
)

data class ToolPermissionPreferences(
  val readFileAllowed: Boolean = true,
  val writeFileRequireReview: Boolean = true,
  val applyPatchRequireReview: Boolean = true,
  val inspectWorkspaceAllowed: Boolean = true
)

class PreferencesManager(private val context: Context) {

  private object Keys {
    val PROVIDER_TYPE = stringPreferencesKey("provider_type")
    val BASE_URL = stringPreferencesKey("base_url")
    val API_KEY = stringPreferencesKey("api_key")
    val MODEL_ID = stringPreferencesKey("model_id")
    val TEMPERATURE = floatPreferencesKey("temperature")
    val MAX_TOKENS = intPreferencesKey("max_tokens")
    val IS_STREAMING = booleanPreferencesKey("is_streaming")
    val AUTO_APPROVE_WRITES = booleanPreferencesKey("auto_approve_writes")
    val AUTO_APPROVE_COMMANDS = booleanPreferencesKey("auto_approve_commands")
    val MAX_AGENT_STEPS = intPreferencesKey("max_agent_steps")
    val LAST_PROJECT_ID = stringPreferencesKey("last_project_id")

    // MCP Config
    val MCP_SERVERS_JSON = stringPreferencesKey("mcp_servers_json")

    // Editor Settings
    val EDITOR_FONT_SIZE = intPreferencesKey("editor_font_size")
    val EDITOR_LINE_NUMBERS = booleanPreferencesKey("editor_line_numbers")
    val EDITOR_WORD_WRAP = booleanPreferencesKey("editor_word_wrap")
    val EDITOR_TAB_SIZE = intPreferencesKey("editor_tab_size")

    // General Settings
    val APP_THEME = stringPreferencesKey("app_theme")
    val AUTO_OPEN_WORKSPACE = booleanPreferencesKey("auto_open_workspace")
    val CODE_COMPLETION = booleanPreferencesKey("code_completion")
    val EDITOR_TOOLBAR = booleanPreferencesKey("editor_toolbar")

    // Tool Permissions
    val TOOL_READ_FILE = booleanPreferencesKey("tool_read_file")
    val TOOL_WRITE_FILE_REVIEW = booleanPreferencesKey("tool_write_file_review")
    val TOOL_APPLY_PATCH_REVIEW = booleanPreferencesKey("tool_apply_patch_review")
    val TOOL_INSPECT_WORKSPACE = booleanPreferencesKey("tool_inspect_workspace")

    // Live Models & Storage flags
    val FETCHED_MODELS = stringPreferencesKey("fetched_models")
    val STORAGE_INITIALIZED = booleanPreferencesKey("storage_initialized")
  }

  val providerConfigFlow: Flow<ProviderConfig> = context.dataStore.data.map { prefs ->
    val typeName = prefs[Keys.PROVIDER_TYPE] ?: ProviderType.NVIDIA_NIM.name
    val providerType = try {
      ProviderType.valueOf(typeName)
    } catch (_: Exception) {
      ProviderType.NVIDIA_NIM
    }

    ProviderConfig(
      providerType = providerType,
      baseUrl = prefs[Keys.BASE_URL] ?: providerType.defaultBaseUrl,
      apiKey = prefs[Keys.API_KEY] ?: "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",
      modelId = prefs[Keys.MODEL_ID] ?: providerType.defaultModel,
      temperature = prefs[Keys.TEMPERATURE] ?: 0.2f,
      maxTokens = prefs[Keys.MAX_TOKENS] ?: 4096,
      isStreaming = prefs[Keys.IS_STREAMING] ?: true,
      autoApproveFileWrites = prefs[Keys.AUTO_APPROVE_WRITES] ?: false,
      autoApproveSafeCommands = prefs[Keys.AUTO_APPROVE_COMMANDS] ?: false,
      maxAgentSteps = prefs[Keys.MAX_AGENT_STEPS] ?: 8
    )
  }

  val lastProjectIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
    prefs[Keys.LAST_PROJECT_ID]
  }

  val mcpServersJsonFlow: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[Keys.MCP_SERVERS_JSON] ?: "{\n  \"mcpServers\": {}\n}"
  }

  val editorPreferencesFlow: Flow<EditorPreferences> = context.dataStore.data.map { prefs ->
    EditorPreferences(
      fontSizeSp = prefs[Keys.EDITOR_FONT_SIZE] ?: 13,
      showLineNumbers = prefs[Keys.EDITOR_LINE_NUMBERS] ?: true,
      wordWrap = prefs[Keys.EDITOR_WORD_WRAP] ?: false,
      tabSize = prefs[Keys.EDITOR_TAB_SIZE] ?: 4
    )
  }

  val generalPreferencesFlow: Flow<GeneralPreferences> = context.dataStore.data.map { prefs ->
    GeneralPreferences(
      theme = prefs[Keys.APP_THEME] ?: "LIGHT",
      autoOpenWorkspace = prefs[Keys.AUTO_OPEN_WORKSPACE] ?: true,
      codeCompletionEnabled = prefs[Keys.CODE_COMPLETION] ?: true,
      editorToolbarEnabled = prefs[Keys.EDITOR_TOOLBAR] ?: true
    )
  }

  val toolPermissionsFlow: Flow<ToolPermissionPreferences> = context.dataStore.data.map { prefs ->
    ToolPermissionPreferences(
      readFileAllowed = prefs[Keys.TOOL_READ_FILE] ?: true,
      writeFileRequireReview = prefs[Keys.TOOL_WRITE_FILE_REVIEW] ?: true,
      applyPatchRequireReview = prefs[Keys.TOOL_APPLY_PATCH_REVIEW] ?: true,
      inspectWorkspaceAllowed = prefs[Keys.TOOL_INSPECT_WORKSPACE] ?: true
    )
  }

  suspend fun saveProviderConfig(config: ProviderConfig) {
    context.dataStore.edit { prefs ->
      prefs[Keys.PROVIDER_TYPE] = config.providerType.name
      prefs[Keys.BASE_URL] = config.baseUrl
      prefs[Keys.API_KEY] = config.apiKey
      prefs[Keys.MODEL_ID] = config.modelId
      prefs[Keys.TEMPERATURE] = config.temperature
      prefs[Keys.MAX_TOKENS] = config.maxTokens
      prefs[Keys.IS_STREAMING] = config.isStreaming
      prefs[Keys.AUTO_APPROVE_WRITES] = config.autoApproveFileWrites
      prefs[Keys.AUTO_APPROVE_COMMANDS] = config.autoApproveSafeCommands
      prefs[Keys.MAX_AGENT_STEPS] = config.maxAgentSteps
    }
  }

  suspend fun saveLastProjectId(projectId: String?) {
    context.dataStore.edit { prefs ->
      if (projectId != null) {
        prefs[Keys.LAST_PROJECT_ID] = projectId
      } else {
        prefs.remove(Keys.LAST_PROJECT_ID)
      }
    }
  }

  suspend fun saveMcpServersJson(json: String) {
    context.dataStore.edit { prefs ->
      prefs[Keys.MCP_SERVERS_JSON] = json
    }
  }

  suspend fun saveEditorPreferences(prefsData: EditorPreferences) {
    context.dataStore.edit { prefs ->
      prefs[Keys.EDITOR_FONT_SIZE] = prefsData.fontSizeSp
      prefs[Keys.EDITOR_LINE_NUMBERS] = prefsData.showLineNumbers
      prefs[Keys.EDITOR_WORD_WRAP] = prefsData.wordWrap
      prefs[Keys.EDITOR_TAB_SIZE] = prefsData.tabSize
    }
  }

  suspend fun saveGeneralPreferences(prefsData: GeneralPreferences) {
    context.dataStore.edit { prefs ->
      prefs[Keys.APP_THEME] = prefsData.theme
      prefs[Keys.AUTO_OPEN_WORKSPACE] = prefsData.autoOpenWorkspace
      prefs[Keys.CODE_COMPLETION] = prefsData.codeCompletionEnabled
      prefs[Keys.EDITOR_TOOLBAR] = prefsData.editorToolbarEnabled
    }
  }

  suspend fun saveToolPermissions(prefsData: ToolPermissionPreferences) {
    context.dataStore.edit { prefs ->
      prefs[Keys.TOOL_READ_FILE] = prefsData.readFileAllowed
      prefs[Keys.TOOL_WRITE_FILE_REVIEW] = prefsData.writeFileRequireReview
      prefs[Keys.TOOL_APPLY_PATCH_REVIEW] = prefsData.applyPatchRequireReview
      prefs[Keys.TOOL_INSPECT_WORKSPACE] = prefsData.inspectWorkspaceAllowed
    }
  }

  val fetchedModelsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
    val raw = prefs[Keys.FETCHED_MODELS] ?: ""
    if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
  }

  suspend fun saveFetchedModels(models: List<String>) {
    context.dataStore.edit { prefs ->
      prefs[Keys.FETCHED_MODELS] = models.joinToString(",")
    }
  }

  val storageInitializedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[Keys.STORAGE_INITIALIZED] ?: false
  }

  suspend fun setStorageInitialized(initialized: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.STORAGE_INITIALIZED] = initialized
    }
  }

  suspend fun resetAllToDefaults() {
    context.dataStore.edit { prefs ->
      prefs.clear()
    }
  }

  suspend fun clearTemporaryCache() {
    try {
      context.cacheDir.deleteRecursively()
    } catch (_: Exception) {
    }
  }

  companion object {
    /**
     * Redacts sensitive secrets for safe logging and telemetry.
     */
    fun redactApiKey(key: String): String {
      if (key.isBlank()) return "(Not set)"
      if (key.length <= 8) return "********"
      return "${key.take(4)}...${key.takeLast(4)}"
    }
  }
}
