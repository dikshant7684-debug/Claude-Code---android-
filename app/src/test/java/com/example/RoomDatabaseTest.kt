package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMessageDao
import com.example.data.database.ChatMessageEntity
import com.example.data.database.ChatRepository
import com.example.data.model.MessageRole
import com.example.data.model.UiChatMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseTest {

  private lateinit var db: AppDatabase
  private lateinit var dao: ChatMessageDao
  private lateinit var repository: ChatRepository

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.chatMessageDao()
    repository = ChatRepository(dao)
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun testInsertAndRetrieveMessages() = runBlocking {
    val userMsg = UiChatMessage(
      id = "msg-1",
      role = MessageRole.USER,
      content = "Hello Claude Code",
      timestamp = 1000L
    )

    val assistantMsg = UiChatMessage(
      id = "msg-2",
      role = MessageRole.ASSISTANT,
      content = "Hello! How can I help you today?",
      timestamp = 2000L,
      summary = "Greeting response"
    )

    repository.saveMessage(userMsg)
    repository.saveMessage(assistantMsg)

    val messages = repository.getAllMessages()
    assertEquals(2, messages.size)
    assertEquals("Hello Claude Code", messages[0].content)
    assertEquals(MessageRole.USER, messages[0].role)
    assertEquals("Hello! How can I help you today?", messages[1].content)
    assertEquals(MessageRole.ASSISTANT, messages[1].role)
    assertEquals("Greeting response", messages[1].summary)
    assertFalse(messages[1].isRunning)
  }

  @Test
  fun testFlowEmitsPersistedMessages() = runBlocking {
    val msg = UiChatMessage(
      id = "msg-flow",
      role = MessageRole.USER,
      content = "Flow test",
      timestamp = 5000L
    )

    repository.saveMessage(msg)

    val flowList = repository.messagesFlow.first()
    assertEquals(1, flowList.size)
    assertEquals("Flow test", flowList[0].content)
  }

  @Test
  fun testClearAllMessages() = runBlocking {
    repository.saveMessage(
      UiChatMessage(id = "1", role = MessageRole.USER, content = "Msg 1", timestamp = 100L)
    )
    repository.saveMessage(
      UiChatMessage(id = "2", role = MessageRole.ASSISTANT, content = "Msg 2", timestamp = 200L)
    )

    assertEquals(2, repository.getAllMessages().size)

    repository.clearAllMessages()

    assertTrue(repository.getAllMessages().isEmpty())
  }

  @Test
  fun testProjectMemoryPersistenceAndIsolation() = runBlocking {
    val memoryRepo = com.example.data.database.ProjectMemoryRepository(db.projectMemoryDao())
    val projA = "project-alpha"
    val projB = "project-beta"

    memoryRepo.recordMemory(
      projectId = projA,
      category = "ARCHITECTURE",
      key = "ArchitecturePattern",
      content = "Uses MVVM with clean state flows and Room persistence.",
      importance = 0.9f,
      tags = "architecture,mvvm,room"
    )

    memoryRepo.recordMemory(
      projectId = projB,
      category = "CONVENTION",
      key = "NamingStyle",
      content = "Snake case for python scripts.",
      importance = 0.5f,
      tags = "python,style"
    )

    val memoriesA = memoryRepo.getMemories(projA)
    val memoriesB = memoryRepo.getMemories(projB)

    assertEquals(1, memoriesA.size)
    assertEquals(1, memoriesB.size)
    assertEquals("ArchitecturePattern", memoriesA[0].key)
    assertEquals("NamingStyle", memoriesB[0].key)

    // Verify search and relevance retrieval
    val relevant = memoryRepo.getRelevantMemories(projA, "How does the MVVM architecture work?")
    assertEquals(1, relevant.size)
    assertEquals("ArchitecturePattern", relevant[0].key)

    // Verify secrets redaction
    memoryRepo.recordMemory(
      projectId = projA,
      category = "CONFIG",
      key = "TestConfig",
      content = "Api key is AIzaSyD12345678901234567890123456789012",
      importance = 0.5f
    )
    val configMemory = memoryRepo.getMemories(projA).find { it.key == "TestConfig" }
    assertTrue(configMemory?.content?.contains("[REDACTED_SECRET]") == true)
    assertFalse(configMemory?.content?.contains("AIzaSyD") == true)
  }
}
