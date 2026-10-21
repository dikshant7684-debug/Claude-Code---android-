package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProviderConfig
import com.example.ui.theme.ClaudeBorder
import com.example.ui.theme.ClaudePureWhite
import com.example.ui.theme.ClaudeSurfaceSubtle
import com.example.ui.theme.ClaudeTerracotta
import com.example.ui.theme.ClaudeTextMuted
import com.example.ui.theme.ClaudeTextPrimary
import com.example.ui.theme.ClaudeTextSecondary

@Composable
fun ClaudeComposer(
  text: String,
  onTextChange: (String) -> Unit,
  onSend: (String) -> Unit,
  config: ProviderConfig,
  onModelSelected: (String) -> Unit,
  onAttachContext: () -> Unit,
  modifier: Modifier = Modifier,
  placeholderText: String = "What would you like to build today?",
  isRunning: Boolean = false,
  onCancel: (() -> Unit)? = null
) {
  var modelMenuExpanded by remember { mutableStateOf(false) }

  val popularModels = remember(config.providerType) {
    listOf(
      "claude-3-7-sonnet-20250219" to "Claude 3.7 Sonnet",
      "claude-3-5-sonnet-20241022" to "Claude 3.5 Sonnet",
      "gpt-4o" to "GPT-4o",
      "gpt-4o-mini" to "GPT-4o Mini",
      "llama3.2" to "Llama 3.2 (Local)",
      "deepseek-chat" to "DeepSeek Chat"
    )
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, ClaudeBorder, RoundedCornerShape(18.dp))
      .clip(RoundedCornerShape(18.dp))
      .testTag("claude_composer_card"),
    color = ClaudePureWhite,
    shadowElevation = 0.5.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Multiline Input Field
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      ) {
        if (text.isEmpty()) {
          Text(
            text = placeholderText,
            style = MaterialTheme.typography.bodyLarge.copy(
              color = ClaudeTextMuted,
              lineHeight = 22.sp
            )
          )
        }

        BasicTextField(
          value = text,
          onValueChange = onTextChange,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("composer_text_input"),
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = ClaudeTextPrimary,
            lineHeight = 22.sp
          ),
          cursorBrush = SolidColor(ClaudeTerracotta),
          keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
          ),
          minLines = 2,
          maxLines = 6
        )
      }

      // Bottom Accessory Bar inside the composer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Plus / Attachment Button
        Surface(
          shape = CircleShape,
          color = ClaudeSurfaceSubtle,
          modifier = Modifier
            .size(32.dp)
            .clickable { onAttachContext() }
            .testTag("composer_attach_btn")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              Icons.Default.Add,
              contentDescription = "Add context or file",
              tint = ClaudeTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Right side: Model selector chip & Send button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Model Selector Pill
          Box {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = ClaudeSurfaceSubtle,
              modifier = Modifier
                .clickable { modelMenuExpanded = true }
                .testTag("composer_model_selector_btn")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val displayModelName = popularModels.firstOrNull { it.first == config.modelId }?.second
                  ?: config.modelId.ifBlank { "Select Model" }

                Text(
                  text = displayModelName,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                  ),
                  color = ClaudeTextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  Icons.Default.KeyboardArrowDown,
                  contentDescription = "Select model",
                  tint = ClaudeTextSecondary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            DropdownMenu(
              expanded = modelMenuExpanded,
              onDismissRequest = { modelMenuExpanded = false }
            ) {
              popularModels.forEach { (id, name) ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = name,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (config.modelId == id) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (config.modelId == id) ClaudeTerracotta else ClaudeTextPrimary
                      )
                    )
                  },
                  onClick = {
                    onModelSelected(id)
                    modelMenuExpanded = false
                  }
                )
              }
            }
          }

          // Send or Stop Button
          if (isRunning) {
            Surface(
              shape = CircleShape,
              color = ClaudeTerracotta,
              modifier = Modifier
                .size(34.dp)
                .clickable { onCancel?.invoke() }
                .testTag("composer_cancel_btn")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Default.Stop,
                  contentDescription = "Stop generation",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          } else {
            val canSend = text.isNotBlank()
            Surface(
              shape = CircleShape,
              color = if (canSend) ClaudeTerracotta else ClaudeSurfaceSubtle,
              modifier = Modifier
                .size(34.dp)
                .clickable(enabled = canSend) {
                  onSend(text.trim())
                }
                .testTag("composer_send_btn")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Default.ArrowUpward,
                  contentDescription = "Send message",
                  tint = if (canSend) Color.White else ClaudeTextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
