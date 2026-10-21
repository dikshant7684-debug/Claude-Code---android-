package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentToolRegistry
import com.example.agent.CodingAgentEngine
import com.example.build.BuildResult
import com.example.build.BuildWorkflowManager
import com.example.data.ai.AiProviderClient
import com.example.data.database.AppDatabase
import com.example.data.database.ChatRepository
import com.example.data.database.ProjectMemoryRepository
import com.example.data.files.ProjectFileManager
import com.example.data.model.AgentTask
import com.example.data.model.FileChange
import com.example.data.model.MessageRole
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.data.model.ProjectType
import com.example.data.model.ProviderConfig
import com.example.data.model.TaskStatus
import com.example.data.model.UiChatMessage
import com.example.data.preferences.EditorPreferences
import com.example.data.preferences.GeneralPreferences
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.ToolPermissionPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

  val fileManager = ProjectFileManager(application)
  val preferencesManager = PreferencesManager(application)
  val aiClient = AiProviderClient()
  val chatDatabase = AppDatabase.getInstance(application)
  val chatRepository = ChatRepository(chatDatabase.chatMessageDao())
  val memoryRepository = ProjectMemoryRepository(chatDatabase.projectMemoryDao())
  val marketplaceManager = com.example.marketplace.service.MarketplaceManager(chatDatabase.marketplaceDao(), scope = viewModelScope)
  val skillPluginManager = com.example.marketplace.service.SkillAndPluginManager(
    context = application,
    installedDao = chatDatabase.installedExtensionDao(),
    executionDao = chatDatabase.extensionExecutionDao(),
    marketplaceDao = chatDatabase.marketplaceDao(),
    adapterRegistry = marketplaceManager.getAdapterRegistry(),
    fileManager = fileManager
  )
  val buildManager = BuildWorkflowManager(fileManager)
  val toolRegistry = AgentToolRegistry(fileManager, buildManager, skillPluginManager)
  val agentEngine = CodingAgentEngine(viewModelScope, toolRegistry, aiClient, fileManager, memoryRepository, skillPluginManager)

  val marketplaces: StateFlow<List<com.example.marketplace.model.MarketplaceSource>> = marketplaceManager.marketplacesFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val marketplaceCatalog: StateFlow<List<com.example.marketplace.model.UniversalExtension>> = marketplaceManager.catalog

  val isRefreshingMarketplaces: StateFlow<Boolean> = marketplaceManager.isRefreshing

  val installedExtensions: StateFlow<List<com.example.marketplace.model.InstalledExtension>> = skillPluginManager.installedExtensionsFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val providerConfig: StateFlow<ProviderConfig> = preferencesManager.providerConfigFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, ProviderConfig())

  val fetchedModels: StateFlow<List<String>> = preferencesManager.fetchedModelsFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val storageInitialized: StateFlow<Boolean> = preferencesManager.storageInitializedFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

  private val _isFetchingModels = MutableStateFlow(false)
  val isFetchingModels: StateFlow<Boolean> = _isFetchingModels.asStateFlow()

  private val _modelsFetchStatus = MutableStateFlow<String?>(null)
  val modelsFetchStatus: StateFlow<String?> = _modelsFetchStatus.asStateFlow()

  private val _storageSetupMessage = MutableStateFlow<String?>(null)
  val storageSetupMessage: StateFlow<String?> = _storageSetupMessage.asStateFlow()

  val mcpServersJson: StateFlow<String> = preferencesManager.mcpServersJsonFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, "{\n  \"mcpServers\": {}\n}")

  val editorPreferences: StateFlow<EditorPreferences> = preferencesManager.editorPreferencesFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, EditorPreferences())

  val generalPreferences: StateFlow<GeneralPreferences> = preferencesManager.generalPreferencesFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, GeneralPreferences())

  val toolPermissions: StateFlow<ToolPermissionPreferences> = preferencesManager.toolPermissionsFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, ToolPermissionPreferences())

  private val _selectedProject = MutableStateFlow<Project?>(null)
  val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

  private val _openFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
  val openFiles: StateFlow<List<ProjectFile>> = _openFiles.asStateFlow()

  private val _activeFile = MutableStateFlow<ProjectFile?>(null)
  val activeFile: StateFlow<ProjectFile?> = _activeFile.asStateFlow()

  private val _activeFileContent = MutableStateFlow("")
  val activeFileContent: StateFlow<String> = _activeFileContent.asStateFlow()

  private val _isContentDirty = MutableStateFlow(false)
  val isContentDirty: StateFlow<Boolean> = _isContentDirty.asStateFlow()

  private val _buildResult = MutableStateFlow<BuildResult?>(null)
  val buildResult: StateFlow<BuildResult?> = _buildResult.asStateFlow()

  private val _connectionStatus = MutableStateFlow<String?>(null)
  val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

  val currentTask: StateFlow<AgentTask?> = agentEngine.currentTask

  // Conversational AI Chat messages
  private val _chatMessages = MutableStateFlow<List<UiChatMessage>>(emptyList())
  val chatMessages: StateFlow<List<UiChatMessage>> = _chatMessages.asStateFlow()

  init {
    initDefaultWorkspace()
    loadPersistedChatMessages()
  }

  private fun loadPersistedChatMessages() {
    viewModelScope.launch {
      chatRepository.messagesFlow.collect { savedMessages ->
        _chatMessages.value = savedMessages
      }
    }
  }

  private fun initDefaultWorkspace() {
    viewModelScope.launch {
      var list = fileManager.listProjects()
      if (list.isEmpty()) {
        val workspace = fileManager.createProject(
          name = "Workspace",
          type = ProjectType.KOTLIN_ANDROID,
          description = "Active developer workspace."
        )
        list = listOf(workspace)
      }
      val target = list.first()
      _selectedProject.value = target

      // Auto open first file or README
      val files = fileManager.listFiles(target)
      val mainFile = files.firstOrNull { it.name.equals("README.md", ignoreCase = true) }
        ?: files.firstOrNull { !it.isDirectory }
      if (mainFile != null) {
        openFile(mainFile)
      }
    }
  }

  fun openFile(file: ProjectFile) {
    if (file.isDirectory) return
    val currentOpen = _openFiles.value.toMutableList()
    if (currentOpen.none { it.path == file.path }) {
      currentOpen.add(file)
      _openFiles.value = currentOpen
    }
    _activeFile.value = file

    viewModelScope.launch {
      val project = _selectedProject.value ?: return@launch
      try {
        val content = fileManager.readFile(project, file.relativePath)
        _activeFileContent.value = content
        _isContentDirty.value = false
      } catch (e: Exception) {
        _activeFileContent.value = "// Error reading file: ${e.message}"
      }
    }
  }

  fun openFileByRelativePath(relativePath: String) {
    viewModelScope.launch {
      val project = _selectedProject.value ?: return@launch
      val files = fileManager.listFiles(project)
      val target = files.firstOrNull { it.relativePath == relativePath || it.name == relativePath }
      if (target != null) {
        openFile(target)
      }
    }
  }

  fun closeFile(file: ProjectFile) {
    val currentOpen = _openFiles.value.toMutableList()
    currentOpen.removeAll { it.path == file.path }
    _openFiles.value = currentOpen

    if (_activeFile.value?.path == file.path) {
      val next = currentOpen.lastOrNull()
      if (next != null) {
        openFile(next)
      } else {
        _activeFile.value = null
        _activeFileContent.value = ""
        _isContentDirty.value = false
      }
    }
  }

  fun updateActiveContent(content: String) {
    _activeFileContent.value = content
    _isContentDirty.value = true
  }

  fun saveActiveFile() {
    val file = _activeFile.value ?: return
    val project = _selectedProject.value ?: return
    val content = _activeFileContent.value

    viewModelScope.launch {
      try {
        fileManager.writeFile(project, file.relativePath, content)
        _isContentDirty.value = false
      } catch (_: Exception) {
      }
    }
  }

  fun startAgentTask(prompt: String) {
    val project = _selectedProject.value ?: return
    agentEngine.startTask(prompt, project, providerConfig.value)
  }

  fun sendChatMessage(prompt: String) {
    val cleanPrompt = prompt.trim()
    if (cleanPrompt.isBlank()) return

    val project = _selectedProject.value ?: return

    val userMsg = UiChatMessage(
      role = MessageRole.USER,
      content = cleanPrompt,
      timestamp = System.currentTimeMillis()
    )

    val assistantMsgId = UUID.randomUUID().toString()
    val assistantMsg = UiChatMessage(
      id = assistantMsgId,
      role = MessageRole.ASSISTANT,
      content = "",
      isRunning = true,
      timestamp = System.currentTimeMillis()
    )

    _chatMessages.value = _chatMessages.value + userMsg + assistantMsg

    // Persist new messages to Room database
    viewModelScope.launch {
      chatRepository.saveMessage(userMsg)
      chatRepository.saveMessage(assistantMsg)
    }

    // Start task in agent
    agentEngine.startTask(cleanPrompt, project, providerConfig.value)

    // Collect updates and synchronize assistant message
    viewModelScope.launch {
      agentEngine.currentTask.collect { task ->
        if (task == null) return@collect

        val isStillRunning = task.status == TaskStatus.PLANNING ||
          task.status == TaskStatus.INSPECTING ||
          task.status == TaskStatus.EDITING ||
          task.status == TaskStatus.VERIFYING

        val content = when {
          !task.summary.isNullOrBlank() -> task.summary!!
          task.currentPlan.isNotBlank() -> task.currentPlan
          else -> "Analyzing request..."
        }

        val modified = task.pendingChanges.map { it.relativePath }

        var updatedMsg: UiChatMessage? = null
        _chatMessages.value = _chatMessages.value.map { msg ->
          if (msg.id == assistantMsgId) {
            val updated = msg.copy(
              content = content,
              steps = task.steps,
              modifiedFiles = modified,
              isRunning = isStillRunning,
              isError = task.status == TaskStatus.FAILED,
              summary = task.summary
            )
            updatedMsg = updated
            updated
          } else {
            msg
          }
        }

        // Persist streaming/completed assistant message update to Room
        updatedMsg?.let {
          chatRepository.saveMessage(it)
        }

        if (!isStillRunning) {
          // Task completed or stopped: auto-refresh workspace files
          val files = fileManager.listFiles(project)
          if (modified.isNotEmpty()) {
            val touched = files.firstOrNull { it.relativePath == modified.last() }
            if (touched != null) {
              openFile(touched)
            }
          }
        }
      }
    }
  }

  fun clearChat() {
    viewModelScope.launch {
      chatRepository.clearAllMessages()
    }
    _chatMessages.value = emptyList()
    agentEngine.cancelCurrentTask()
  }

  fun cancelAgentTask() {
    agentEngine.cancelCurrentTask()
  }

  fun approveAgentAction() {
    agentEngine.approvePendingAction()
  }

  fun rejectAgentAction() {
    agentEngine.rejectPendingAction()
  }

  fun applyChange(change: FileChange) {
    val project = _selectedProject.value ?: return
    viewModelScope.launch {
      agentEngine.applyFileChange(project, change)
      if (_activeFile.value?.relativePath == change.relativePath) {
        _activeFileContent.value = change.proposedContent
        _isContentDirty.value = false
      }
    }
  }

  fun revertChange(change: FileChange) {
    val project = _selectedProject.value ?: return
    viewModelScope.launch {
      agentEngine.revertFileChange(project, change)
      if (_activeFile.value?.relativePath == change.relativePath) {
        _activeFileContent.value = change.originalContent
        _isContentDirty.value = false
      }
    }
  }

  fun runBuildCheck() {
    val project = _selectedProject.value ?: return
    viewModelScope.launch {
      val result = buildManager.checkAndBuild(project)
      _buildResult.value = result
    }
  }

  fun saveProviderConfig(config: ProviderConfig) {
    viewModelScope.launch {
      preferencesManager.saveProviderConfig(config)
    }
  }

  fun saveMcpServersJson(json: String) {
    viewModelScope.launch {
      preferencesManager.saveMcpServersJson(json)
    }
  }

  fun saveEditorPreferences(prefs: EditorPreferences) {
    viewModelScope.launch {
      preferencesManager.saveEditorPreferences(prefs)
    }
  }

  fun saveGeneralPreferences(prefs: GeneralPreferences) {
    viewModelScope.launch {
      preferencesManager.saveGeneralPreferences(prefs)
    }
  }

  fun saveToolPermissions(prefs: ToolPermissionPreferences) {
    viewModelScope.launch {
      preferencesManager.saveToolPermissions(prefs)
    }
  }

  fun resetAllSettings() {
    viewModelScope.launch {
      preferencesManager.resetAllToDefaults()
    }
  }

  fun clearCache(onComplete: (Long) -> Unit) {
    viewModelScope.launch {
      preferencesManager.clearTemporaryCache()
      val freeMb = getApplication<Application>().filesDir.freeSpace / (1024 * 1024)
      onComplete(freeMb)
    }
  }

  fun testConnection() {
    viewModelScope.launch {
      _connectionStatus.value = "Testing connection..."
      val result = aiClient.testConnection(providerConfig.value)
      _connectionStatus.value = if (result.isSuccess) {
        result.getOrNull()
      } else {
        "Failed: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  fun initializeClaudeStorage() {
    viewModelScope.launch(Dispatchers.IO) {
      val result = fileManager.initializeClaudeStorageDirectories()
      preferencesManager.setStorageInitialized(true)
      _storageSetupMessage.value = result
    }
  }

  fun fetchModelsList() {
    viewModelScope.launch {
      _isFetchingModels.value = true
      _modelsFetchStatus.value = "Fetching models from endpoint..."
      val result = aiClient.fetchModels(providerConfig.value)
      _isFetchingModels.value = false
      if (result.isSuccess) {
        val models = result.getOrNull() ?: emptyList()
        preferencesManager.saveFetchedModels(models)
        _modelsFetchStatus.value = "✓ Successfully fetched ${models.size} models from endpoint!"
      } else {
        _modelsFetchStatus.value = "Failed: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  // =========================================================================
  // UNIVERSAL PLUGIN & MARKETPLACE ACTIONS
  // =========================================================================

  fun installExtension(
    extension: com.example.marketplace.model.UniversalExtension,
    grantedPermissions: Set<com.example.marketplace.model.DeclaredPermission> = extension.declaredPermissions.toSet(),
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      val res = skillPluginManager.installExtension(extension, grantedPermissions)
      if (res.isSuccess) {
        onComplete(true, "Successfully installed '${extension.name}'.")
      } else {
        onComplete(false, "Installation failed: ${res.exceptionOrNull()?.message}")
      }
    }
  }

  fun uninstallExtension(extensionId: String, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val res = skillPluginManager.uninstallExtension(extensionId)
      onComplete(res.isSuccess)
    }
  }

  fun toggleExtension(extensionId: String, isEnabled: Boolean) {
    viewModelScope.launch {
      skillPluginManager.setEnabled(extensionId, isEnabled)
    }
  }

  fun updateExtensionPermissions(extensionId: String, permissions: Set<com.example.marketplace.model.DeclaredPermission>) {
    viewModelScope.launch {
      skillPluginManager.updateGrantedPermissions(extensionId, permissions)
    }
  }

  fun addCustomMarketplace(
    name: String,
    location: String,
    sourceType: com.example.marketplace.model.MarketplaceSourceType,
    ecosystem: com.example.marketplace.model.EcosystemType,
    onComplete: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      val res = marketplaceManager.addMarketplace(name, location, sourceType, ecosystem)
      if (res.isSuccess) {
        onComplete(true, "Marketplace '${res.getOrNull()?.name}' added successfully.")
      } else {
        onComplete(false, "Failed to add marketplace: ${res.exceptionOrNull()?.message}")
      }
    }
  }

  fun refreshAllMarketplaces() {
    viewModelScope.launch {
      marketplaceManager.refreshAllMarketplaces()
    }
  }

  fun toggleMarketplace(marketplaceId: String, isEnabled: Boolean) {
    viewModelScope.launch {
      marketplaceManager.toggleMarketplace(marketplaceId, isEnabled)
    }
  }

  fun deleteMarketplace(marketplaceId: String) {
    viewModelScope.launch {
      marketplaceManager.deleteMarketplace(marketplaceId)
    }
  }

  fun checkForExtensionUpdates() {
    viewModelScope.launch {
      skillPluginManager.checkForUpdates()
    }
  }
}
