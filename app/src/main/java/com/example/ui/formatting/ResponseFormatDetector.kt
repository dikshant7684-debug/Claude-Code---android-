package com.example.ui.formatting

object ResponseFormatDetector {

  private val PROMPT_INTENT_PATTERNS = listOf(
    Regex("(?i)\\b(write|create|generate|give me|make|craft|improve|rewrite|optimize)\\s+(a|an|the)?\\s*(system\\s+)?prompt\\b"),
    Regex("(?i)\\b(prompt\\s+for\\s+[a-z0-9_-]+)"),
    Regex("(?i)\\b(system\\s+prompt\\s+for\\b)"),
    Regex("(?i)\\b(image\\s+prompt\\s+for\\b)"),
    Regex("(?i)\\b(instructions?\\s+(that\\s+I\\s+can\\s+paste|for\\s+claude|for\\s+chatgpt|for\\s+ai)\\b)")
  )

  private val COMMAND_LANGUAGES = setOf(
    "bash", "sh", "shell", "zsh", "cmd", "terminal", "powershell", "console"
  )

  private val PROMPT_LANGUAGES = setOf(
    "prompt", "system-prompt", "systemprompt", "user-prompt", "prompt-template", "instructions"
  )

  fun detectAndParse(content: String, userPrompt: String = ""): List<ResponseSegment> {
    val clean = content.trim()
    if (clean.isBlank()) return emptyList()

    val isPromptRequested = isPromptRequest(userPrompt)
    val segments = mutableListOf<ResponseSegment>()

    // Parse fenced blocks while preserving streaming state
    val normalized = ensureClosedFences(clean)
    val parts = normalized.split("```")

    for (i in parts.indices) {
      val part = parts[i]
      if (i % 2 == 1) {
        // Inside a fenced block
        val firstNewline = part.indexOf('\n')
        val lang = if (firstNewline != -1) part.substring(0, firstNewline).trim().lowercase() else ""
        val codeBody = if (firstNewline != -1) part.substring(firstNewline + 1) else part
        val trimmedCode = codeBody.trimEnd('\r', '\n')

        val isPromptBlock = PROMPT_LANGUAGES.contains(lang) ||
          (isPromptRequested && (lang.isBlank() || lang == "text" || lang == "markdown" || lang == "prompt"))

        if (isPromptBlock) {
          val promptType = detectPromptType(lang, userPrompt, trimmedCode)
          segments.add(
            ResponseSegment.PromptBlock(
              promptText = trimmedCode,
              title = promptType.displayName,
              promptType = promptType
            )
          )
        } else {
          val isCommand = COMMAND_LANGUAGES.contains(lang) || looksLikeCommand(trimmedCode)
          segments.add(
            ResponseSegment.CodeBlock(
              code = trimmedCode,
              language = if (lang.isNotBlank()) lang else if (isCommand) "sh" else "text",
              isCommand = isCommand
            )
          )
        }
      } else {
        // Outside fenced blocks: parse paragraphs, headings, lists, tables, or prompt sections
        val text = part.trim()
        if (text.isNotEmpty()) {
          parseProportionalText(text, isPromptRequested, segments)
        }
      }
    }

    // Conservative check: If the entire response was requested as a prompt and wasn't wrapped in code fences,
    // check if it starts with an explicit Prompt header or is pure prompt instructions
    if (segments.size == 1 && segments[0] is ResponseSegment.NormalParagraph && isPromptRequested) {
      val text = (segments[0] as ResponseSegment.NormalParagraph).text
      if (looksLikePurePrompt(text)) {
        val pType = detectPromptType("", userPrompt, text)
        return listOf(
          ResponseSegment.PromptBlock(
            promptText = text,
            title = pType.displayName,
            promptType = pType
          )
        )
      }
    }

    return if (segments.isNotEmpty()) segments else listOf(ResponseSegment.NormalParagraph(clean))
  }

  fun isPromptRequest(userPrompt: String): Boolean {
    val clean = userPrompt.trim()
    if (clean.isBlank()) return false
    return PROMPT_INTENT_PATTERNS.any { it.containsMatchIn(clean) }
  }

  private fun detectPromptType(lang: String, userPrompt: String, body: String): PromptType {
    val combined = (lang + " " + userPrompt + " " + body.take(150)).lowercase()
    return when {
      combined.contains("system prompt") || lang == "system-prompt" || lang == "systemprompt" -> PromptType.SYSTEM_PROMPT
      combined.contains("image prompt") || combined.contains("midjourney") || combined.contains("diffusion") || lang == "image-prompt" -> PromptType.IMAGE_PROMPT
      combined.contains("coding prompt") || combined.contains("code prompt") -> PromptType.CODING_PROMPT
      combined.contains("user prompt") || lang == "user-prompt" -> PromptType.USER_PROMPT
      else -> PromptType.GENERAL
    }
  }

  private fun looksLikeCommand(code: String): Boolean {
    val lines = code.lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) return false
    val first = lines.first()
    return first.startsWith("$ ") || first.startsWith("> ") ||
      first.startsWith("gradle ") || first.startsWith("./gradlew ") ||
      first.startsWith("npm ") || first.startsWith("git ") ||
      first.startsWith("curl ") || first.startsWith("docker ") ||
      first.startsWith("apt-get ") || first.startsWith("cd ") || first.startsWith("mkdir ")
  }

  private fun looksLikePurePrompt(text: String): Boolean {
    val lower = text.lowercase()
    return lower.startsWith("you are a") || lower.startsWith("act as a") ||
      lower.startsWith("your role is") || lower.startsWith("prompt:") ||
      lower.startsWith("# system prompt") || lower.startsWith("## prompt") ||
      text.contains("\nInstructions:") || text.contains("\nGoal:") || text.contains("\nConstraints:")
  }

  private fun ensureClosedFences(raw: String): String {
    val count = raw.split("```").size - 1
    return if (count % 2 == 1) {
      // Incomplete trailing code fence in streaming response
      "$raw\n```"
    } else {
      raw
    }
  }

  private fun parseProportionalText(
    text: String,
    isPromptRequested: Boolean,
    out: MutableList<ResponseSegment>
  ) {
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
      val line = lines[i]
      val trimmed = line.trim()

      if (trimmed.isBlank()) {
        i++
        continue
      }

      // 1. Markdown Table detection (| Col 1 | Col 2 |)
      if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size && lines[i + 1].trim().contains("---")) {
        val tableLines = mutableListOf<String>()
        while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
          tableLines.add(lines[i].trim())
          i++
        }
        val tableBlock = parseTable(tableLines)
        if (tableBlock != null) {
          out.add(tableBlock)
          continue
        }
      }

      // 2. Markdown Headings (#, ##, ###)
      if (trimmed.startsWith("#")) {
        val level = trimmed.takeWhile { it == '#' }.length
        val title = trimmed.drop(level).trim()
        if (title.isNotEmpty()) {
          out.add(ResponseSegment.NormalParagraph(title, isHeading = true, headingLevel = level))
          i++
          continue
        }
      }

      // 3. Bullet or Numbered Lists
      if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || (trimmed.firstOrNull()?.isDigit() == true && trimmed.contains(". "))) {
        val listItems = mutableListOf<String>()
        val isOrdered = trimmed.firstOrNull()?.isDigit() == true
        while (i < lines.size) {
          val l = lines[i].trim()
          if (l.isBlank()) break
          if (l.startsWith("- ") || l.startsWith("* ")) {
            listItems.add(l.drop(2).trim())
          } else if (l.firstOrNull()?.isDigit() == true && l.indexOf(". ") != -1) {
            listItems.add(l.substringAfter(". ").trim())
          } else {
            // Continuation line for previous item or break
            if (listItems.isNotEmpty()) {
              val last = listItems.removeAt(listItems.lastIndex)
              listItems.add("$last $l")
            } else {
              break
            }
          }
          i++
        }
        if (listItems.isNotEmpty()) {
          out.add(ResponseSegment.ListBlock(listItems, isOrdered = isOrdered))
          continue
        }
      }

      // 4. Regular Paragraph (accumulate lines until blank line or special marker)
      val pBuilder = StringBuilder()
      while (i < lines.size) {
        val cur = lines[i]
        val curTrimmed = cur.trim()
        if (curTrimmed.isBlank() || curTrimmed.startsWith("#") || curTrimmed.startsWith("- ") ||
          curTrimmed.startsWith("* ") || (curTrimmed.firstOrNull()?.isDigit() == true && curTrimmed.contains(". ")) ||
          (curTrimmed.startsWith("|") && curTrimmed.endsWith("|"))
        ) {
          break
        }
        if (pBuilder.isNotEmpty()) pBuilder.append(" ")
        pBuilder.append(curTrimmed)
        i++
      }

      val paragraphText = pBuilder.toString().trim()
      if (paragraphText.isNotEmpty()) {
        out.add(ResponseSegment.NormalParagraph(paragraphText))
      }
    }
  }

  private fun parseTable(lines: List<String>): ResponseSegment.TableBlock? {
    if (lines.size < 2) return null
    fun splitRow(row: String): List<String> {
      return row.trim('|').split('|').map { it.trim() }
    }

    val headers = splitRow(lines[0])
    // Line 1 should be delimiter (| --- | --- |)
    val rows = mutableListOf<List<String>>()
    for (idx in 2 until lines.size) {
      rows.add(splitRow(lines[idx]))
    }
    return ResponseSegment.TableBlock(headers, rows)
  }
}
