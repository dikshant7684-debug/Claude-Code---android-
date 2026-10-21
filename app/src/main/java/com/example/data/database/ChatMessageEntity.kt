package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MessageRole
import com.example.data.model.UiChatMessage

/**
 * Room database entity representing a persistent conversational chat message
 * between the user and Claude Code.
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey
  val id: String,
  val role: String,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val summary: String? = null,
  val isError: Boolean = false,
  val isRunning: Boolean = false,
  val modifiedFiles: String = ""
) {
  fun toUiModel(): UiChatMessage {
    val messageRole = try {
      MessageRole.valueOf(role)
    } catch (_: Exception) {
      MessageRole.ASSISTANT
    }
    val files = if (modifiedFiles.isBlank()) {
      emptyList()
    } else {
      modifiedFiles.split("\n").filter { it.isNotBlank() }
    }
    return UiChatMessage(
      id = id,
      role = messageRole,
      content = content,
      timestamp = timestamp,
      steps = emptyList(),
      modifiedFiles = files,
      isRunning = false,
      isError = isError,
      summary = summary
    )
  }

  companion object {
    fun fromUiModel(message: UiChatMessage): ChatMessageEntity {
      return ChatMessageEntity(
        id = message.id,
        role = message.role.name,
        content = message.content,
        timestamp = message.timestamp,
        summary = message.summary,
        isError = message.isError,
        isRunning = message.isRunning,
        modifiedFiles = message.modifiedFiles.joinToString("\n")
      )
    }
  }
}
