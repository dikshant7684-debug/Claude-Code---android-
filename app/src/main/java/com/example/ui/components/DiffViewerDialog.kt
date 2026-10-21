package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiffLineType
import com.example.data.model.FileChange
import com.example.ui.theme.ClaudeBorder
import com.example.ui.theme.ClaudeBorderLight
import com.example.ui.theme.ClaudePureWhite
import com.example.ui.theme.ClaudeSurfaceSubtle
import com.example.ui.theme.ClaudeTerracotta
import com.example.ui.theme.ClaudeTerracottaLight
import com.example.ui.theme.ClaudeTextMuted
import com.example.ui.theme.ClaudeTextPrimary
import com.example.ui.theme.ClaudeTextSecondary
import com.example.ui.theme.ClaudeWarmBg

@Composable
fun DiffViewerDialog(
  change: FileChange,
  onApply: () -> Unit,
  onRevert: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .fillMaxHeight(0.90f)
        .border(1.dp, ClaudeBorder, RoundedCornerShape(18.dp))
        .testTag("diff_viewer_dialog"),
      shape = RoundedCornerShape(18.dp),
      color = ClaudePureWhite,
      shadowElevation = 2.dp
    ) {
      Column(
        modifier = Modifier.padding(18.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Review Modifications",
              style = MaterialTheme.typography.titleLarge,
              color = ClaudeTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = change.relativePath,
              style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
              color = ClaudeTerracotta
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("diff_close_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = ClaudeTextSecondary, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Diff List
        Surface(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .border(1.dp, ClaudeBorderLight, RoundedCornerShape(12.dp)),
          color = ClaudeWarmBg,
          shape = RoundedCornerShape(12.dp)
        ) {
          val horizontalScroll = rememberScrollState()

          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(8.dp)
              .horizontalScroll(horizontalScroll)
          ) {
            val allLines = change.diffHunks.flatMap { it.lines }
            if (allLines.isEmpty()) {
              item {
                Text(
                  text = "No differences found between versions.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = ClaudeTextSecondary,
                  modifier = Modifier.padding(16.dp)
                )
              }
            } else {
              items(allLines) { line ->
                val (bgColor, textColor, prefix) = when (line.type) {
                  DiffLineType.ADDITION -> Triple(
                    Color(0xFFE8F5E9), // Soft calm green
                    Color(0xFF2E7D32),
                    "+ "
                  )
                  DiffLineType.DELETION -> Triple(
                    Color(0xFFFFEBEE), // Soft calm red
                    Color(0xFFC62828),
                    "- "
                  )
                  DiffLineType.CONTEXT -> Triple(
                    Color.Transparent,
                    ClaudeTextPrimary,
                    "  "
                  )
                  DiffLineType.HEADER -> Triple(
                    ClaudeTerracottaLight,
                    ClaudeTerracotta,
                    "@@ "
                  )
                }

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor, RoundedCornerShape(2.dp))
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${line.oldLineNumber?.toString() ?: " "} | ${line.newLineNumber?.toString() ?: " "}",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp
                    ),
                    color = ClaudeTextMuted,
                    modifier = Modifier.width(60.dp)
                  )
                  Text(
                    text = "$prefix${line.text}",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = FontFamily.Monospace,
                      fontSize = 12.sp
                    ),
                    color = textColor
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              onRevert()
              onDismiss()
            },
            modifier = Modifier.testTag("diff_revert_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Revert", color = ClaudeTextSecondary)
          }

          Spacer(modifier = Modifier.width(10.dp))

          Button(
            onClick = {
              onApply()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta),
            modifier = Modifier.testTag("diff_apply_button")
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Apply Changes")
          }
        }
      }
    }
  }
}
