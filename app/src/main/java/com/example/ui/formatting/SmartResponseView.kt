package com.example.ui.formatting

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SmartMessageBody(
  content: String,
  userPrompt: String = "",
  onOpenFile: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val segments = remember(content, userPrompt) {
    ResponseFormatDetector.detectAndParse(content, userPrompt)
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    segments.forEach { segment ->
      when (segment) {
        is ResponseSegment.PromptBlock -> {
          PromptBlockCard(block = segment)
        }
        is ResponseSegment.CodeBlock -> {
          if (segment.isCommand) {
            TerminalCommandCard(block = segment)
          } else {
            FormattedCodeBlockCard(block = segment)
          }
        }
        is ResponseSegment.NormalParagraph -> {
          FormattedParagraph(paragraph = segment, onOpenFile = onOpenFile)
        }
        is ResponseSegment.ListBlock -> {
          FormattedList(list = segment)
        }
        is ResponseSegment.TableBlock -> {
          MarkdownTableView(table = segment)
        }
      }
    }
  }
}

/**
 * Dedicated Monospace Prompt Block Card with one-tap Copy functionality
 */
@Composable
fun PromptBlockCard(
  block: ResponseSegment.PromptBlock,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isCopied by remember { mutableStateOf(false) }

  LaunchedEffect(isCopied) {
    if (isCopied) {
      delay(2000)
      isCopied = false
    }
  }

  val badgeColor = when (block.promptType) {
    PromptType.SYSTEM_PROMPT -> MaterialTheme.colorScheme.tertiary
    PromptType.IMAGE_PROMPT -> MaterialTheme.colorScheme.secondary
    PromptType.CODING_PROMPT -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.primary
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .testTag("prompt_block_card")
  ) {
    Column {
      // Header: Badge + One-Tap Copy button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = block.title,
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = badgeColor
          )
        }

        // Copy Button with animated feedback
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          AnimatedVisibility(
            visible = isCopied,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
          ) {
            Text(
              text = "Copied",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              ),
              color = MaterialTheme.colorScheme.primary
            )
          }

          IconButton(
            onClick = {
              if (!isCopied) {
                val success = ClipboardHelper.copyToClipboard(context, "ai_prompt", block.promptText)
                if (success) {
                  isCopied = true
                } else {
                  Toast.makeText(context, "Could not copy prompt", Toast.LENGTH_SHORT).show()
                }
              }
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("copy_prompt_button")
              .semantics {
                contentDescription = if (isCopied) "Copied to clipboard" else "Copy prompt"
              }
          ) {
            Icon(
              imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = null,
              tint = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Preserved Monospace Content with exact line breaks & indentation
      SelectionContainer {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(14.dp)
        ) {
          Text(
            text = block.promptText,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              lineHeight = 19.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

/**
 * Monospace Code Block Card with Syntax Styling and One-Tap Copy
 */
@Composable
fun FormattedCodeBlockCard(
  block: ResponseSegment.CodeBlock,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isCopied by remember { mutableStateOf(false) }

  LaunchedEffect(isCopied) {
    if (isCopied) {
      delay(2000)
      isCopied = false
    }
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .testTag("code_block_card")
  ) {
    Column {
      // Header: Language Label + Copy Button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = block.language.ifBlank { "code" }.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.primary
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          AnimatedVisibility(
            visible = isCopied,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
          ) {
            Text(
              text = "Copied",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              ),
              color = MaterialTheme.colorScheme.primary
            )
          }

          IconButton(
            onClick = {
              if (!isCopied) {
                val success = ClipboardHelper.copyToClipboard(context, "code_snippet", block.code)
                if (success) {
                  isCopied = true
                } else {
                  Toast.makeText(context, "Could not copy code", Toast.LENGTH_SHORT).show()
                }
              }
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("copy_code_button")
              .semantics {
                contentDescription = if (isCopied) "Copied to clipboard" else "Copy code"
              }
          ) {
            Icon(
              imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = null,
              tint = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Horizontally scrollable code area
      SelectionContainer {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(14.dp)
        ) {
          Text(
            text = block.code,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              lineHeight = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

/**
 * Terminal Command Card with '$' prompt indicator and Copy Button
 */
@Composable
fun TerminalCommandCard(
  block: ResponseSegment.CodeBlock,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isCopied by remember { mutableStateOf(false) }

  LaunchedEffect(isCopied) {
    if (isCopied) {
      delay(2000)
      isCopied = false
    }
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .testTag("terminal_command_card")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Terminal,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp)
      )

      Spacer(modifier = Modifier.width(8.dp))

      SelectionContainer(
        modifier = Modifier
          .weight(1f)
          .horizontalScroll(rememberScrollState())
      ) {
        Text(
          text = block.code,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 18.sp
          ),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      IconButton(
        onClick = {
          if (!isCopied) {
            val cleanCommand = block.code.lines().joinToString("\n") { it.removePrefix("$ ").removePrefix("> ") }
            val success = ClipboardHelper.copyToClipboard(context, "terminal_command", cleanCommand)
            if (success) {
              isCopied = true
            }
          }
        },
        modifier = Modifier
          .size(36.dp)
          .testTag("copy_command_button")
          .semantics {
            contentDescription = if (isCopied) "Copied to clipboard" else "Copy command"
          }
      ) {
        Icon(
          imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
          contentDescription = null,
          tint = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

/**
 * Normal Conversational Paragraphs with Inline Code pills and Heading rendering
 */
@Composable
fun FormattedParagraph(
  paragraph: ResponseSegment.NormalParagraph,
  onOpenFile: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  if (paragraph.isHeading) {
    val style = when (paragraph.headingLevel) {
      1 -> MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
      2 -> MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
      else -> MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
    Text(
      text = paragraph.text,
      style = style,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = modifier.padding(top = 4.dp, bottom = 2.dp)
    )
  } else {
    val annotated = remember(paragraph.text) {
      formatInlineMarkdown(paragraph.text)
    }
    SelectionContainer {
      Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = 15.sp,
          lineHeight = 23.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
      )
    }
  }
}

/**
 * Markdown Bullet or Numbered List
 */
@Composable
fun FormattedList(
  list: ResponseSegment.ListBlock,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(start = 4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    list.items.forEachIndexed { index, item ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
      ) {
        Text(
          text = if (list.isOrdered) "${index + 1}." else "•",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          ),
          color = MaterialTheme.colorScheme.primary
        )

        val annotated = remember(item) { formatInlineMarkdown(item) }
        SelectionContainer {
          Text(
            text = annotated,
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = 15.sp,
              lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

/**
 * Markdown Table View
 */
@Composable
fun MarkdownTableView(
  table: ResponseSegment.TableBlock,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      // Table Header Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        table.headers.forEach { header ->
          Text(
            text = header,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(110.dp)
          )
        }
      }

      // Rows
      table.rows.forEachIndexed { idx, row ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              if (idx % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
              else Color.Transparent
            )
            .padding(vertical = 6.dp, horizontal = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          row.forEach { cell ->
            Text(
              text = cell,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.width(110.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Helper to parse inline Markdown (bold, italic, and inline code)
 */
fun formatInlineMarkdown(raw: String): AnnotatedString {
  return buildAnnotatedString {
    var cursor = 0
    val length = raw.length

    while (cursor < length) {
      val nextBacktick = raw.indexOf('`', cursor)
      val nextBold = raw.indexOf("**", cursor)

      val nextSpecial = when {
        nextBacktick != -1 && nextBold != -1 -> minOf(nextBacktick, nextBold)
        nextBacktick != -1 -> nextBacktick
        nextBold != -1 -> nextBold
        else -> -1
      }

      if (nextSpecial == -1) {
        append(raw.substring(cursor))
        break
      }

      if (nextSpecial > cursor) {
        append(raw.substring(cursor, nextSpecial))
        cursor = nextSpecial
      }

      if (raw.startsWith("`", cursor)) {
        val closing = raw.indexOf('`', cursor + 1)
        if (closing != -1) {
          val codeText = raw.substring(cursor + 1, closing)
          withStyle(
            SpanStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              background = Color(0x18888888)
            )
          ) {
            append(" $codeText ")
          }
          cursor = closing + 1
        } else {
          append(raw.substring(cursor))
          break
        }
      } else if (raw.startsWith("**", cursor)) {
        val closing = raw.indexOf("**", cursor + 2)
        if (closing != -1) {
          val boldText = raw.substring(cursor + 2, closing)
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(boldText)
          }
          cursor = closing + 2
        } else {
          append(raw.substring(cursor))
          break
        }
      } else {
        append(raw[cursor])
        cursor++
      }
    }
  }
}
