package com.example.data.database

import com.example.data.model.UiChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository abstracting Room Database data access for conversational chat messages.
 */
class ChatRepository(
  private val chatMessageDao: ChatMessageDao
) {

  val messagesFlow: Flow<List<UiChatMessage>> = chatMessageDao.getAllMessagesFlow()
    .map { entities -> entities.map { it.toUiModel() } }

  suspend fun getAllMessages(): List<UiChatMessage> = withContext(Dispatchers.IO) {
    chatMessageDao.getAllMessages().map { it.toUiModel() }
  }

  suspend fun saveMessage(message: UiChatMessage) = withContext(Dispatchers.IO) {
    chatMessageDao.insertMessage(ChatMessageEntity.fromUiModel(message))
  }

  suspend fun saveMessages(messages: List<UiChatMessage>) = withContext(Dispatchers.IO) {
    chatMessageDao.insertMessages(messages.map { ChatMessageEntity.fromUiModel(it) })
  }

  suspend fun deleteMessage(id: String) = withContext(Dispatchers.IO) {
    chatMessageDao.deleteMessageById(id)
  }

  suspend fun clearAllMessages() = withContext(Dispatchers.IO) {
    chatMessageDao.clearAllMessages()
  }
}
