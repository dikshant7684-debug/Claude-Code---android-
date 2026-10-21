package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room database operations on persisted chat messages.
 */
@Dao
interface ChatMessageDao {

  @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
  fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

  @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
  suspend fun getAllMessages(): List<ChatMessageEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessages(messages: List<ChatMessageEntity>)

  @Update
  suspend fun updateMessage(message: ChatMessageEntity)

  @Query("DELETE FROM chat_messages WHERE id = :id")
  suspend fun deleteMessageById(id: String)

  @Query("DELETE FROM chat_messages")
  suspend fun clearAllMessages()
}
