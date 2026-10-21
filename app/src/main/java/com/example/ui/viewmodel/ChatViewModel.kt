package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgentTask
import com.example.data.model.MessageRole
import com.example.data.model.TaskStatus
import com.example.data.model.UiChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
  val messages: List<UiChatMessage> = emptyList(),
  val inputText: String = "",
  val selectedModel: String = "GPT-4o",
  val isLoading: Boolean = false,
  val currentTask: AgentTask? = null,
  val availableModels: List<String> = listOf(
    "GPT-4o",
    "Claude 3.7 Sonnet",
    "Claude 3.5 Sonnet",
    "Gemini 2.0 Flash",
    "Llama 3.2"
  )
)

class ChatViewModel(
  private val mainViewModel: MainViewModel? = null
) : ViewModel() {

  private val _uiState = MutableStateFlow(ChatUiState())
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  init {
    mainViewModel?.let { vm ->
      // Sync messages
      viewModelScope.launch {
        vm.chatMessages.collect { list ->
          _uiState.update { it.copy(messages = list) }
        }
      }
      // Sync running state
      viewModelScope.launch {
        vm.currentTask.collect { task ->
          val running = task != null && (
            task.status == TaskStatus.PLANNING ||
            task.status == TaskStatus.INSPECTING ||
            task.status == TaskStatus.EDITING ||
            task.status == TaskStatus.VERIFYING
          )
          _uiState.update { it.copy(isLoading = running, currentTask = task) }
        }
      }
      // Sync model configuration
      viewModelScope.launch {
        vm.providerConfig.collect { config ->
          if (config.modelId.isNotBlank()) {
            val displayName = when {
              config.modelId.contains("gpt-4o", ignoreCase = true) -> "GPT-4o"
              config.modelId.contains("3-7-sonnet", ignoreCase = true) -> "Claude 3.7 Sonnet"
              config.modelId.contains("3-5-sonnet", ignoreCase = true) -> "Claude 3.5 Sonnet"
              config.modelId.contains("gemini", ignoreCase = true) -> "Gemini 2.0 Flash"
              config.modelId.contains("llama", ignoreCase = true) -> "Llama 3.2"
              else -> config.modelId
            }
            _uiState.update { it.copy(selectedModel = displayName) }
          }
        }
      }
      // Sync fetched live models from endpoint
      viewModelScope.launch {
        vm.fetchedModels.collect { fetched ->
          if (fetched.isNotEmpty()) {
            val combined = (fetched + listOf("GPT-4o", "Claude 3.7 Sonnet", "Claude 3.5 Sonnet", "Gemini 2.0 Flash", "Llama 3.2")).distinct()
            _uiState.update { it.copy(availableModels = combined) }
          }
        }
      }
    }
  }

  fun onInputTextChanged(newText: String) {
    _uiState.update { it.copy(inputText = newText) }
  }

  fun onModelSelected(model: String) {
    _uiState.update { it.copy(selectedModel = model) }
    mainViewModel?.let { vm ->
      val modelId = when (model) {
        "GPT-4o" -> "gpt-4o"
        "Claude 3.7 Sonnet" -> "claude-3-7-sonnet-20250219"
        "Claude 3.5 Sonnet" -> "claude-3-5-sonnet-20241022"
        "Gemini 2.0 Flash" -> "gemini-2.0-flash"
        "Llama 3.2" -> "llama3.2"
        else -> model
      }
      val current = vm.providerConfig.value
      vm.saveProviderConfig(current.copy(modelId = modelId))
    }
  }

  fun onSuggestionClick(suggestion: String) {
    _uiState.update { it.copy(inputText = suggestion) }
  }

  fun onSendMessage() {
    val prompt = _uiState.value.inputText.trim()
    if (prompt.isBlank()) return
    _uiState.update { it.copy(inputText = "") }

    if (mainViewModel != null) {
      mainViewModel.sendChatMessage(prompt)
    } else {
      // Standalone / Preview fallback
      val userMsg = UiChatMessage(
        id = UUID.randomUUID().toString(),
        role = MessageRole.USER,
        content = prompt,
        timestamp = System.currentTimeMillis()
      )
      val assistantMsg = UiChatMessage(
        id = UUID.randomUUID().toString(),
        role = MessageRole.ASSISTANT,
        content = "I have analyzed your request: \"$prompt\".\n\n```kotlin\n// Starter code\nfun handleRequest() {\n    println(\"Processing prompt: $prompt\")\n}\n```",
        timestamp = System.currentTimeMillis()
      )
      _uiState.update { it.copy(messages = it.messages + userMsg + assistantMsg) }
    }
  }

  fun onRegeneratePrompt(prompt: String) {
    if (prompt.isBlank()) return
    if (mainViewModel != null) {
      mainViewModel.sendChatMessage(prompt)
    }
  }

  fun onCancelTask() {
    mainViewModel?.cancelAgentTask()
    _uiState.update { it.copy(isLoading = false) }
  }

  fun onClearChat() {
    mainViewModel?.clearChat()
    _uiState.update { it.copy(messages = emptyList(), inputText = "") }
  }
}
