package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.formatting.ClipboardHelper
import com.example.ui.formatting.PromptType
import com.example.ui.formatting.ResponseFormatDetector
import com.example.ui.formatting.ResponseSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SmartResponseFormattingTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
  }

  @Test
  fun testNormalConversationalResponse() {
    val conversationalText = """
      Hello! I can help you with Android architecture and Jetpack Compose.
      
      Feel free to ask questions about state management or Room databases.
    """.trimIndent()

    val segments = ResponseFormatDetector.detectAndParse(conversationalText, userPrompt = "Hello, what can you do?")

    assertTrue("Should produce segments", segments.isNotEmpty())
    assertTrue("Should be normal paragraph", segments.all { it is ResponseSegment.NormalParagraph })
    val first = segments[0] as ResponseSegment.NormalParagraph
    assertFalse("Should not be heading", first.isHeading)
    assertTrue(first.text.contains("Hello!"))
  }

  @Test
  fun testPromptBlockDetectionWithFencedTag() {
    val rawResponse = """
      Here is the system prompt:
      
      ```prompt
      You are an expert autonomous Android software engineer.
        - Follow clean architecture
        - Adhere strictly to Material 3
      ```
      
      You can use this in your coding environment.
    """.trimIndent()

    val segments = ResponseFormatDetector.detectAndParse(rawResponse, userPrompt = "Write a prompt for Claude Code")

    val promptSegment = segments.find { it is ResponseSegment.PromptBlock } as? ResponseSegment.PromptBlock
    assertNotNull("Should detect prompt block", promptSegment)
    assertTrue("Should preserve prompt text", promptSegment!!.promptText.contains("You are an expert autonomous Android software engineer."))
    assertTrue("Should preserve indentation", promptSegment.promptText.contains("  - Follow clean architecture"))
    assertEquals(PromptType.GENERAL, promptSegment.promptType)

    // Verify explanation remains outside the prompt block
    val normalParagraphs = segments.filterIsInstance<ResponseSegment.NormalParagraph>()
    assertTrue("Should contain explanation before or after", normalParagraphs.isNotEmpty())
  }

  @Test
  fun testPromptIntentDetectionWithoutExplicitFence() {
    val userPrompt = "Write a system prompt for an Android developer agent"
    val aiResponse = """
      ```
      You are a specialized Android Engineer AI.
      Constraints:
      1. Write Kotlin only.
      2. Use Jetpack Compose for UI.
      ```
    """.trimIndent()

    val segments = ResponseFormatDetector.detectAndParse(aiResponse, userPrompt = userPrompt)

    val promptSegment = segments.find { it is ResponseSegment.PromptBlock } as? ResponseSegment.PromptBlock
    assertNotNull("Should detect prompt block based on user intent", promptSegment)
    assertEquals(PromptType.SYSTEM_PROMPT, promptSegment!!.promptType)
  }

  @Test
  fun testCodeBlockAndCommandDetection() {
    val codeSnippet = """
      Check this Kotlin snippet:
      
      ```kotlin
      fun greet(name: String): String {
          return "Hello, ${'$'}name"
      }
      ```
      
      And run this build command:
      
      ```bash
      gradle :app:testDebugUnitTest
      ```
    """.trimIndent()

    val segments = ResponseFormatDetector.detectAndParse(codeSnippet, userPrompt = "How do I test?")

    val codeBlocks = segments.filterIsInstance<ResponseSegment.CodeBlock>()
    assertEquals("Should detect 2 code blocks", 2, codeBlocks.size)

    val kotlinBlock = codeBlocks.find { it.language == "kotlin" }
    assertNotNull("Should find kotlin block", kotlinBlock)
    assertFalse("Kotlin block is not terminal command", kotlinBlock!!.isCommand)
    assertTrue("Kotlin code preserved", kotlinBlock.code.contains("fun greet"))

    val bashBlock = codeBlocks.find { it.language == "bash" }
    assertNotNull("Should find bash block", bashBlock)
    assertTrue("Bash block is identified as terminal command", bashBlock!!.isCommand)
    assertTrue("Command preserved", bashBlock.code.contains("gradle :app:testDebugUnitTest"))
  }

  @Test
  fun testMixedResponseWithHeadingsAndList() {
    val mixedContent = """
      # Android Best Practices
      
      Here are the primary recommendations:
      - Use ViewModel for state
      - Keep Composables stateless
      - Use Room for local persistence
      
      ```kotlin
      class UserViewModel : ViewModel()
      ```
    """.trimIndent()

    val segments = ResponseFormatDetector.detectAndParse(mixedContent, userPrompt = "Explain best practices")

    val heading = segments.find { it is ResponseSegment.NormalParagraph && it.isHeading } as? ResponseSegment.NormalParagraph
    assertNotNull("Should detect heading", heading)
    assertEquals("Android Best Practices", heading!!.text)
    assertEquals(1, heading.headingLevel)

    val list = segments.find { it is ResponseSegment.ListBlock } as? ResponseSegment.ListBlock
    assertNotNull("Should detect list block", list)
    assertEquals(3, list!!.items.size)
    assertEquals("Use ViewModel for state", list.items[0])

    val code = segments.find { it is ResponseSegment.CodeBlock } as? ResponseSegment.CodeBlock
    assertNotNull("Should detect code block", code)
    assertTrue(code!!.code.contains("class UserViewModel"))
  }

  @Test
  fun testMarkdownTableDetection() {
    val tableContent = """
      | Component | Purpose |
      | --- | --- |
      | ViewModel | UI State Management |
      | Room | Persistence |
      | Coroutines | Asynchronous Flow |
    """.trimIndent()

    val segments = ResponseFormatDetector.detectAndParse(tableContent, userPrompt = "Compare components")

    val table = segments.find { it is ResponseSegment.TableBlock } as? ResponseSegment.TableBlock
    assertNotNull("Should detect markdown table", table)
    assertEquals(2, table!!.headers.size)
    assertEquals("Component", table.headers[0])
    assertEquals("Purpose", table.headers[1])
    assertEquals(3, table.rows.size)
    assertEquals("ViewModel", table.rows[0][0])
  }

  @Test
  fun testStreamingIncompleteCodeFenceResilience() {
    // Streaming response with unclosed code fence
    val streamingChunk = """
      Here is the progress:
      
      ```kotlin
      val appName = "DroidCode"
      val version = 1.0
    """.trimIndent()

    // Must not crash and must gracefully close fence
    val segments = ResponseFormatDetector.detectAndParse(streamingChunk, userPrompt = "Show config")

    val code = segments.find { it is ResponseSegment.CodeBlock } as? ResponseSegment.CodeBlock
    assertNotNull("Should safely parse in-flight code block", code)
    assertEquals("kotlin", code!!.language)
    assertTrue(code.code.contains("appName = \"DroidCode\""))
  }

  @Test
  fun testClipboardHelperExactContentPreservation() {
    val promptContent = """
      You are an expert prompt engineer.
      1. Write precise steps.
          - Nested indentation level
      2. No placeholders.
    """.trimIndent()

    val success = ClipboardHelper.copyToClipboard(context, "test_prompt", promptContent)
    assertTrue("Copy operation should succeed", success)

    val retrieved = ClipboardHelper.getPrimaryClipText(context)
    assertEquals("Copied content must be identical to source", promptContent, retrieved)
  }

  @Test
  fun testTerminalCommandExtractionExcludesUiPrefixes() {
    val commandText = "$ git status"
    val cleanCommand = commandText.lines().joinToString("\n") { it.removePrefix("$ ").removePrefix("> ") }

    val success = ClipboardHelper.copyToClipboard(context, "test_cmd", cleanCommand)
    assertTrue(success)

    val copied = ClipboardHelper.getPrimaryClipText(context)
    assertEquals("git status", copied)
  }
}
