package com.example.ui.formatting

enum class PromptType(val displayName: String) {
  GENERAL("PROMPT"),
  SYSTEM_PROMPT("SYSTEM PROMPT"),
  USER_PROMPT("USER PROMPT"),
  CODING_PROMPT("CODING PROMPT"),
  IMAGE_PROMPT("IMAGE PROMPT")
}

sealed class ResponseSegment {
  data class NormalParagraph(
    val text: String,
    val isHeading: Boolean = false,
    val headingLevel: Int = 1
  ) : ResponseSegment()

  data class PromptBlock(
    val promptText: String,
    val title: String = "PROMPT",
    val promptType: PromptType = PromptType.GENERAL
  ) : ResponseSegment()

  data class CodeBlock(
    val code: String,
    val language: String = "",
    val isCommand: Boolean = false
  ) : ResponseSegment()

  data class ListBlock(
    val items: List<String>,
    val isOrdered: Boolean = false
  ) : ResponseSegment()

  data class TableBlock(
    val headers: List<String>,
    val rows: List<List<String>>
  ) : ResponseSegment()
}
