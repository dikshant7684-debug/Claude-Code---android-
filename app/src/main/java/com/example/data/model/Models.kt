package com.example.data.model

import java.io.File

enum class ProjectType(val displayName: String, val extensionHint: String) {
  KOTLIN_ANDROID("Android / Kotlin", "kt"),
  PYTHON("Python CLI / Script", "py"),
  NODE_JS("Node.js / TypeScript", "ts"),
  RUST("Rust Project", "rs"),
  GO("Go Application", "go"),
  SHELL("Shell / Automation", "sh"),
  PLAIN("General Codebase", "txt")
}

data class Project(
  val id: String,
  val name: String,
  val rootPath: String,
  val type: ProjectType = ProjectType.PLAIN,
  val description: String = "",
  val lastModified: Long = System.currentTimeMillis()
) {
  val rootDirectory: File
    get() = File(rootPath)
}

data class ProjectFile(
  val name: String,
  val path: String,
  val relativePath: String,
  val isDirectory: Boolean,
  val sizeBytes: Long = 0L,
  val lastModified: Long = 0L,
  val extension: String = ""
)

data class FileChange(
  val filePath: String,
  val relativePath: String,
  val originalContent: String,
  val proposedContent: String,
  val diffHunks: List<DiffHunk> = emptyList(),
  val isApproved: Boolean = false,
  val isApplied: Boolean = false
)

data class DiffHunk(
  val oldStart: Int,
  val oldLinesCount: Int,
  val newStart: Int,
  val newLinesCount: Int,
  val lines: List<DiffLine>
)

data class DiffLine(
  val type: DiffLineType,
  val text: String,
  val oldLineNumber: Int? = null,
  val newLineNumber: Int? = null
)

enum class DiffLineType {
  CONTEXT,
  ADDITION,
  DELETION,
  HEADER
}

enum class TaskStatus {
  IDLE,
  PLANNING,
  INSPECTING,
  EDITING,
  VERIFYING,
  COMPLETED,
  FAILED,
  CANCELLED
}

enum class StepStatus {
  PENDING,
  RUNNING,
  COMPLETED,
  FAILED,
  REQUIRES_APPROVAL
}

data class AgentStep(
  val id: String,
  val title: String,
  val description: String,
  val toolName: String? = null,
  val toolInput: String? = null,
  val toolOutput: String? = null,
  val status: StepStatus = StepStatus.PENDING,
  val timestamp: Long = System.currentTimeMillis()
)

data class AgentTask(
  val id: String,
  val projectId: String,
  val userPrompt: String,
  val status: TaskStatus = TaskStatus.IDLE,
  val currentPlan: String = "",
  val steps: List<AgentStep> = emptyList(),
  val pendingChanges: List<FileChange> = emptyList(),
  val createdTimestamp: Long = System.currentTimeMillis(),
  val completedTimestamp: Long? = null,
  val summary: String? = null
)

enum class ProviderType(val displayName: String, val defaultBaseUrl: String, val defaultModel: String) {
  OPENAI("OpenAI Compatible", "https://api.openai.com/v1", "gpt-4o"),
  ANTHROPIC("Anthropic Compatible", "https://api.anthropic.com/v1", "claude-3-7-sonnet-20250219"),
  CUSTOM("Custom / Ollama / Local", "http://10.0.2.2:11434/v1", "llama3.2")
}

data class ProviderConfig(
  val providerType: ProviderType = ProviderType.OPENAI,
  val baseUrl: String = ProviderType.OPENAI.defaultBaseUrl,
  val apiKey: String = "",
  val modelId: String = ProviderType.OPENAI.defaultModel,
  val temperature: Float = 0.2f,
  val maxTokens: Int = 4096,
  val isStreaming: Boolean = true,
  val autoApproveFileWrites: Boolean = false,
  val autoApproveSafeCommands: Boolean = false,
  val maxAgentSteps: Int = 8
)

enum class DiagnosticSeverity {
  ERROR,
  WARNING,
  INFO
}

data class BuildDiagnostic(
  val filePath: String? = null,
  val lineNumber: Int? = null,
  val column: Int? = null,
  val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR,
  val message: String,
  val rawOutput: String = ""
)

enum class MessageRole {
  USER,
  ASSISTANT,
  SYSTEM
}

data class UiChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val role: MessageRole,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val steps: List<AgentStep> = emptyList(),
  val modifiedFiles: List<String> = emptyList(),
  val isRunning: Boolean = false,
  val isError: Boolean = false,
  val summary: String? = null
)

