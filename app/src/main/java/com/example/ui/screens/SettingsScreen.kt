package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProviderType
import com.example.ui.theme.ClaudeBorder
import com.example.ui.theme.ClaudeBorderLight
import com.example.ui.theme.ClaudePureWhite
import com.example.ui.theme.ClaudeSurfaceSubtle
import com.example.ui.theme.ClaudeTerracotta
import com.example.ui.theme.ClaudeTerracottaContainer
import com.example.ui.theme.ClaudeTerracottaDark
import com.example.ui.theme.ClaudeTerracottaLight
import com.example.ui.theme.ClaudeTextMuted
import com.example.ui.theme.ClaudeTextPrimary
import com.example.ui.theme.ClaudeTextSecondary
import com.example.ui.theme.ClaudeWarmBg
import com.example.ui.viewmodel.MainViewModel

/**
 * Streamlined Settings Categories:
 * Focuses purely on essential, genuinely functional developer configurations.
 * Eliminates mock desktop plugin marketplaces, unworkable npx MCP servers,
 * low-level tool permission toggles, and duplicate aliases.
 */
enum class SettingsCategory(val title: String, val icon: ImageVector) {
  AI_PROVIDERS("AI & Models", Icons.Default.Psychology),
  AGENT_BEHAVIOR("Agent Behavior", Icons.Default.Tune),
  APPEARANCE_EDITOR("Appearance & Editor", Icons.Default.Code),
  ABOUT("Storage & About", Icons.Default.Info)
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
  val configuration = LocalConfiguration.current
  val isWideScreen = configuration.screenWidthDp >= 640

  var selectedCategory by remember { mutableStateOf(SettingsCategory.AI_PROVIDERS) }

  Scaffold(
    containerColor = ClaudeWarmBg,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(ClaudeWarmBg)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Settings",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
              ),
              color = ClaudeTextPrimary
            )
            Text(
              text = "Configure AI models, agent autonomy, and editor preferences",
              style = MaterialTheme.typography.bodySmall,
              color = ClaudeTextSecondary
            )
          }

          // Badge indicator
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ClaudeTerracottaLight,
            modifier = Modifier.border(1.dp, ClaudeTerracotta.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
          ) {
            Text(
              text = selectedCategory.title,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              ),
              color = ClaudeTerracottaDark,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        // Horizontal pill selector for phones / compact devices
        if (!isWideScreen) {
          LazyRow(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(SettingsCategory.values()) { category ->
              val isSelected = category == selectedCategory
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) ClaudeTerracottaContainer else ClaudePureWhite,
                modifier = Modifier
                  .border(
                    1.dp,
                    if (isSelected) ClaudeTerracotta else ClaudeBorder,
                    RoundedCornerShape(16.dp)
                  )
                  .clickable { selectedCategory = category }
                  .testTag("settings_tab_${category.name.lowercase()}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    category.icon,
                    contentDescription = null,
                    tint = if (isSelected) ClaudeTerracotta else ClaudeTextSecondary,
                    modifier = Modifier.size(15.dp)
                  )
                  Text(
                    text = category.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) ClaudeTerracottaDark else ClaudeTextPrimary
                  )
                }
              }
            }
          }
        }

        Divider(color = ClaudeBorderLight, thickness = 1.dp)
      }
    }
  ) { padding ->
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(ClaudeWarmBg)
    ) {
      if (isWideScreen) {
        // Persistent Left Sidebar for tablet / wide screens
        Column(
          modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(ClaudeSurfaceSubtle)
            .border(width = 1.dp, color = ClaudeBorderLight)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          SettingsCategory.values().forEach { category ->
            val isSelected = category == selectedCategory
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) ClaudePureWhite else Color.Transparent,
              modifier = Modifier
                .fillMaxWidth()
                .border(
                  width = 1.dp,
                  color = if (isSelected) ClaudeBorder else Color.Transparent,
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable { selectedCategory = category }
                .testTag("settings_cat_${category.name.lowercase()}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  category.icon,
                  contentDescription = null,
                  tint = if (isSelected) ClaudeTerracotta else ClaudeTextSecondary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = category.title,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                  ),
                  color = if (isSelected) ClaudeTextPrimary else ClaudeTextSecondary
                )
              }
            }
          }
        }
      }

      // Main Settings Content Region
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .background(ClaudeWarmBg)
      ) {
        when (selectedCategory) {
          SettingsCategory.AI_PROVIDERS -> AiProvidersSettingsPage(viewModel = viewModel)
          SettingsCategory.AGENT_BEHAVIOR -> AgentBehaviorSettingsPage(viewModel = viewModel)
          SettingsCategory.APPEARANCE_EDITOR -> EditorAndAppearanceSettingsPage(viewModel = viewModel)
          SettingsCategory.ABOUT -> StorageAndAboutPage(viewModel = viewModel)
        }
      }
    }
  }
}

// =========================================================================
// 1. AI PROVIDERS & CREDENTIALS PAGE (ESSENTIAL & FULLY FUNCTIONAL)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiProvidersSettingsPage(viewModel: MainViewModel) {
  val currentConfig by viewModel.providerConfig.collectAsState()
  val connectionStatus by viewModel.connectionStatus.collectAsState()
  val fetchedModels by viewModel.fetchedModels.collectAsState()
  val isFetchingModels by viewModel.isFetchingModels.collectAsState()
  val modelsFetchStatus by viewModel.modelsFetchStatus.collectAsState()
  val context = LocalContext.current

  var providerType by remember(currentConfig) { mutableStateOf(currentConfig.providerType) }
  var baseUrl by remember(currentConfig) { mutableStateOf(currentConfig.baseUrl) }
  var apiKey by remember(currentConfig) { mutableStateOf(currentConfig.apiKey) }
  var modelId by remember(currentConfig) { mutableStateOf(currentConfig.modelId) }
  var showApiKey by remember { mutableStateOf(false) }
  var providerExpanded by remember { mutableStateOf(false) }
  var savedFeedback by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 18.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "AI Providers & Models",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Configure API endpoints, model identifiers, and credentials for autonomous AI responses.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    // Provider Protocol Dropdown
    ExposedDropdownMenuBox(
      expanded = providerExpanded,
      onExpandedChange = { providerExpanded = !providerExpanded }
    ) {
      OutlinedTextField(
        value = providerType.displayName,
        onValueChange = {},
        readOnly = true,
        label = { Text("Provider Protocol", color = ClaudeTextSecondary) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
        modifier = Modifier.menuAnchor().fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ClaudeTerracotta,
          unfocusedBorderColor = ClaudeBorder,
          focusedTextColor = ClaudeTextPrimary,
          unfocusedTextColor = ClaudeTextPrimary,
          focusedContainerColor = ClaudePureWhite,
          unfocusedContainerColor = ClaudePureWhite
        )
      )
      ExposedDropdownMenu(
        expanded = providerExpanded,
        onDismissRequest = { providerExpanded = false },
        modifier = Modifier.background(ClaudePureWhite)
      ) {
        ProviderType.values().forEach { type ->
          DropdownMenuItem(
            text = { Text(type.displayName, color = ClaudeTextPrimary) },
            onClick = {
              providerType = type
              baseUrl = type.defaultBaseUrl
              modelId = type.defaultModel
              providerExpanded = false
            }
          )
        }
      }
    }

    // Base URL / API Endpoint
    OutlinedTextField(
      value = baseUrl,
      onValueChange = { baseUrl = it },
      label = { Text("API Endpoint URL (Base URL)", color = ClaudeTextSecondary) },
      placeholder = { Text("e.g. https://api.anthropic.com/v1 or https://api.openai.com/v1") },
      modifier = Modifier.fillMaxWidth().testTag("settings_base_url_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ClaudeTerracotta,
        unfocusedBorderColor = ClaudeBorder,
        focusedTextColor = ClaudeTextPrimary,
        unfocusedTextColor = ClaudeTextPrimary,
        focusedContainerColor = ClaudePureWhite,
        unfocusedContainerColor = ClaudePureWhite
      )
    )

    // Endpoint Quick Presets
    Text("Quick Endpoint Presets:", style = MaterialTheme.typography.labelSmall, color = ClaudeTextSecondary)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      item {
        ClaudeLightModelPill("Anthropic") {
          baseUrl = "https://api.anthropic.com/v1"
          providerType = ProviderType.ANTHROPIC
          modelId = "claude-3-7-sonnet-20250219"
        }
      }
      item {
        ClaudeLightModelPill("OpenAI (/v1)") {
          baseUrl = "https://api.openai.com/v1"
          providerType = ProviderType.OPENAI
          modelId = "gpt-4o"
        }
      }
      item {
        ClaudeLightModelPill("OpenRouter") {
          baseUrl = "https://openrouter.ai/api/v1"
          providerType = ProviderType.OPENAI
          modelId = "anthropic/claude-3.7-sonnet"
        }
      }
      item {
        ClaudeLightModelPill("Groq") {
          baseUrl = "https://api.groq.com/openai/v1"
          providerType = ProviderType.OPENAI
          modelId = "llama-3.3-70b-versatile"
        }
      }
      item {
        ClaudeLightModelPill("Ollama (Local)") {
          baseUrl = "http://10.0.2.2:11434/v1"
          providerType = ProviderType.CUSTOM
          modelId = "llama3.2"
        }
      }
    }

    // API Key
    OutlinedTextField(
      value = apiKey,
      onValueChange = { apiKey = it },
      label = { Text("API Key (Stored locally in DataStore)", color = ClaudeTextSecondary) },
      visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
      trailingIcon = {
        IconButton(onClick = { showApiKey = !showApiKey }) {
          Icon(
            if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = null,
            tint = ClaudeTextMuted
          )
        }
      },
      modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ClaudeTerracotta,
        unfocusedBorderColor = ClaudeBorder,
        focusedTextColor = ClaudeTextPrimary,
        unfocusedTextColor = ClaudeTextPrimary,
        focusedContainerColor = ClaudePureWhite,
        unfocusedContainerColor = ClaudePureWhite
      )
    )

    // Fetch Models from Endpoint Action Button
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = ClaudeSurfaceSubtle,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, ClaudeTerracotta, RoundedCornerShape(8.dp))
        .clickable(enabled = !isFetchingModels && baseUrl.isNotBlank()) {
          val updated = currentConfig.copy(
            providerType = providerType,
            baseUrl = baseUrl.trim(),
            apiKey = apiKey.trim(),
            modelId = modelId.trim()
          )
          viewModel.saveProviderConfig(updated)
          viewModel.fetchModelsList()
        }
        .testTag("fetch_models_btn")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        if (isFetchingModels) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = ClaudeTerracotta
          )
        } else {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = ClaudeTerracotta, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isFetchingModels) "Fetching Models from Endpoint..." else "Fetch Models List from Endpoint",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = ClaudeTerracotta
        )
      }
    }

    // Models Fetch Status Banner
    if (modelsFetchStatus != null) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (modelsFetchStatus!!.startsWith("✓")) Color(0xFFF1F8E9) else Color(0xFFFFEBEE),
        border = BorderStroke(1.dp, if (modelsFetchStatus!!.startsWith("✓")) Color(0xFF81C784) else Color(0xFFE57373)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
      ) {
        Text(
          text = modelsFetchStatus!!,
          style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
          color = if (modelsFetchStatus!!.startsWith("✓")) Color(0xFF2E7D32) else Color(0xFFC62828),
          modifier = Modifier.padding(10.dp)
        )
      }
    }

    // Live Fetched Models Section
    if (fetchedModels.isNotEmpty()) {
      Text(
        text = "Discovered Models from Endpoint (${fetchedModels.size}) - Tap to select:",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = ClaudeTextPrimary
      )
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(fetchedModels) { m ->
          val isSelected = m == modelId
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSelected) ClaudeTerracottaContainer else ClaudePureWhite,
            modifier = Modifier
              .border(1.dp, if (isSelected) ClaudeTerracotta else ClaudeBorder, RoundedCornerShape(16.dp))
              .clickable {
                modelId = m
                val updated = currentConfig.copy(
                  providerType = providerType,
                  baseUrl = baseUrl.trim(),
                  apiKey = apiKey.trim(),
                  modelId = m
                )
                viewModel.saveProviderConfig(updated)
                Toast.makeText(context, "Selected model: $m", Toast.LENGTH_SHORT).show()
              }
          ) {
            Text(
              text = m,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
              ),
              color = if (isSelected) ClaudeTerracottaDark else ClaudeTextPrimary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // Model ID Manual Input
    OutlinedTextField(
      value = modelId,
      onValueChange = { modelId = it },
      label = { Text("Active Model ID", color = ClaudeTextSecondary) },
      modifier = Modifier.fillMaxWidth().testTag("settings_model_id_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ClaudeTerracotta,
        unfocusedBorderColor = ClaudeBorder,
        focusedTextColor = ClaudeTextPrimary,
        unfocusedTextColor = ClaudeTextPrimary,
        focusedContainerColor = ClaudePureWhite,
        unfocusedContainerColor = ClaudePureWhite
      )
    )

    // Popular Default Identifiers
    Text("Popular Default Identifiers:", style = MaterialTheme.typography.labelSmall, color = ClaudeTextSecondary)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      item { ClaudeLightModelPill("claude-3-7-sonnet-20250219") { modelId = "claude-3-7-sonnet-20250219" } }
      item { ClaudeLightModelPill("claude-3-5-sonnet-20241022") { modelId = "claude-3-5-sonnet-20241022" } }
      item { ClaudeLightModelPill("gpt-4o") { modelId = "gpt-4o" } }
      item { ClaudeLightModelPill("gemini-2.0-flash") { modelId = "gemini-2.0-flash" } }
      item { ClaudeLightModelPill("llama-3.3-70b-versatile") { modelId = "llama-3.3-70b-versatile" } }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Actions: Test Connection & Save Credentials
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
          .clickable {
            val updated = currentConfig.copy(
              providerType = providerType,
              baseUrl = baseUrl.trim(),
              apiKey = apiKey.trim(),
              modelId = modelId.trim()
            )
            viewModel.saveProviderConfig(updated)
            viewModel.testConnection()
          }
          .testTag("test_connection_btn")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = ClaudeTerracotta, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Test Connection", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = ClaudeTextPrimary)
        }
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ClaudeTerracotta,
        modifier = Modifier
          .clickable {
            val updated = currentConfig.copy(
              providerType = providerType,
              baseUrl = baseUrl.trim(),
              apiKey = apiKey.trim(),
              modelId = modelId.trim()
            )
            viewModel.saveProviderConfig(updated)
            savedFeedback = true
            Toast.makeText(context, "Credentials saved successfully!", Toast.LENGTH_SHORT).show()
          }
          .testTag("save_settings_btn")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Save Credentials",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
          )
        }
      }
    }

    if (savedFeedback) {
      Text("✓ Credentials saved into DataStore", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
    }

    if (connectionStatus != null) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
          .padding(12.dp)
      ) {
        Text(
          text = connectionStatus!!,
          style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
          color = if (connectionStatus!!.contains("successful")) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
      }
    }
  }
}

// =========================================================================
// 2. AGENT BEHAVIOR PAGE (ESSENTIAL AUTONOMY & REASONING SETTINGS)
// =========================================================================

@Composable
fun AgentBehaviorSettingsPage(viewModel: MainViewModel) {
  val currentConfig by viewModel.providerConfig.collectAsState()
  var autoApproveWrites by remember(currentConfig) { mutableStateOf(currentConfig.autoApproveFileWrites) }
  var autoApproveCommands by remember(currentConfig) { mutableStateOf(currentConfig.autoApproveSafeCommands) }
  var maxSteps by remember(currentConfig) { mutableFloatStateOf(currentConfig.maxAgentSteps.toFloat()) }
  var temperature by remember(currentConfig) { mutableFloatStateOf(currentConfig.temperature) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 18.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Agent Behavior & Safeguards",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Configure reasoning boundaries, automated execution approvals, and generation temperature.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    // Auto-approve file writes toggle
    ClaudeSettingRow(
      title = "Auto-approve file modifications",
      description = "Apply proposed diffs directly to source files without waiting for manual confirmation."
    ) {
      Switch(
        checked = autoApproveWrites,
        onCheckedChange = {
          autoApproveWrites = it
          viewModel.saveProviderConfig(currentConfig.copy(autoApproveFileWrites = it))
        },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ClaudeTerracotta
        ),
        modifier = Modifier.testTag("auto_approve_writes_switch")
      )
    }

    Divider(color = ClaudeBorderLight)

    // Auto-approve safe operations
    ClaudeSettingRow(
      title = "Auto-approve safe operations",
      description = "Execute read-only file inspections and project diagnostics without prompting."
    ) {
      Switch(
        checked = autoApproveCommands,
        onCheckedChange = {
          autoApproveCommands = it
          viewModel.saveProviderConfig(currentConfig.copy(autoApproveSafeCommands = it))
        },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ClaudeTerracotta
        ),
        modifier = Modifier.testTag("auto_approve_commands_switch")
      )
    }

    Divider(color = ClaudeBorderLight)

    // Max steps slider
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Max Reasoning Iterations", style = MaterialTheme.typography.bodyMedium, color = ClaudeTextPrimary)
        Text("${maxSteps.toInt()} steps", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ClaudeTerracotta)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Controls maximum consecutive tool invocations per task before pausing.", style = MaterialTheme.typography.bodySmall, color = ClaudeTextSecondary)
      Slider(
        value = maxSteps,
        onValueChange = {
          maxSteps = it
          viewModel.saveProviderConfig(currentConfig.copy(maxAgentSteps = it.toInt()))
        },
        valueRange = 1f..20f,
        steps = 18,
        colors = SliderDefaults.colors(
          thumbColor = ClaudeTerracotta,
          activeTrackColor = ClaudeTerracotta,
          inactiveTrackColor = ClaudeBorderLight
        )
      )
    }

    Divider(color = ClaudeBorderLight)

    // Temperature slider
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Sampling Temperature", style = MaterialTheme.typography.bodyMedium, color = ClaudeTextPrimary)
        Text(String.format(java.util.Locale.US, "%.2f", temperature), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ClaudeTerracotta)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Lower values produce more deterministic code; higher values are more creative.", style = MaterialTheme.typography.bodySmall, color = ClaudeTextSecondary)
      Slider(
        value = temperature,
        onValueChange = {
          temperature = it
          viewModel.saveProviderConfig(currentConfig.copy(temperature = it))
        },
        valueRange = 0.0f..1.0f,
        steps = 10,
        colors = SliderDefaults.colors(
          thumbColor = ClaudeTerracotta,
          activeTrackColor = ClaudeTerracotta,
          inactiveTrackColor = ClaudeBorderLight
        )
      )
    }
  }
}

// =========================================================================
// 3. APPEARANCE & EDITOR SETTINGS (COMBINED, POLISHED & PRACTICAL)
// =========================================================================

@Composable
fun EditorAndAppearanceSettingsPage(viewModel: MainViewModel) {
  val generalPrefs by viewModel.generalPreferences.collectAsState()
  val editorPrefs by viewModel.editorPreferences.collectAsState()
  val context = LocalContext.current
  var fontSize by remember(editorPrefs) { mutableFloatStateOf(editorPrefs.fontSizeSp.toFloat()) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 18.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Appearance & Editor",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Customize application theme, typography, font size, and code editing controls.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    // Theme Appearance Selector
    ClaudeSettingRow(
      title = "Theme Appearance",
      description = "Selected theme active across chat and code workspaces."
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("LIGHT" to "Light Warm", "DARK" to "Dark Slate", "SYSTEM" to "System").forEach { (id, label) ->
          val isSelected = generalPrefs.theme == id
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSelected) ClaudeTerracottaLight else ClaudePureWhite,
            modifier = Modifier
              .border(
                1.dp,
                if (isSelected) ClaudeTerracotta else ClaudeBorder,
                RoundedCornerShape(6.dp)
              )
              .clickable {
                viewModel.saveGeneralPreferences(generalPrefs.copy(theme = id))
                Toast.makeText(context, "$label Theme Selected", Toast.LENGTH_SHORT).show()
              }
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) ClaudeTerracotta else ClaudeTextSecondary
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
          }
        }
      }
    }

    Divider(color = ClaudeBorderLight)

    // AI Code Suggestions
    ClaudeSettingRow(
      title = "AI Code Suggestions",
      description = "Enable intelligent code completion and suggestion chips."
    ) {
      Switch(
        checked = generalPrefs.codeCompletionEnabled,
        onCheckedChange = {
          viewModel.saveGeneralPreferences(generalPrefs.copy(codeCompletionEnabled = it))
        },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ClaudeTerracotta
        )
      )
    }

    Divider(color = ClaudeBorderLight)

    // Line Numbers Toggle
    ClaudeSettingRow(
      title = "Line Numbers Gutter",
      description = "Display line numbers along the left edge in the code editor."
    ) {
      Switch(
        checked = editorPrefs.showLineNumbers,
        onCheckedChange = {
          viewModel.saveEditorPreferences(editorPrefs.copy(showLineNumbers = it))
        },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ClaudeTerracotta
        )
      )
    }

    Divider(color = ClaudeBorderLight)

    // Word Wrap Toggle
    ClaudeSettingRow(
      title = "Word Wrapping",
      description = "Wrap long code lines to prevent horizontal scrolling in editor."
    ) {
      Switch(
        checked = editorPrefs.wordWrap,
        onCheckedChange = {
          viewModel.saveEditorPreferences(editorPrefs.copy(wordWrap = it))
        },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ClaudeTerracotta
        )
      )
    }

    Divider(color = ClaudeBorderLight)

    // Font Size Slider with Live Preview
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Font Size", style = MaterialTheme.typography.bodyMedium, color = ClaudeTextPrimary)
        Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ClaudeTerracotta)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Controls editor and code block font scale.", style = MaterialTheme.typography.bodySmall, color = ClaudeTextSecondary)

      Slider(
        value = fontSize,
        onValueChange = {
          fontSize = it
          viewModel.saveEditorPreferences(editorPrefs.copy(fontSizeSp = it.toInt()))
        },
        valueRange = 11f..22f,
        steps = 10,
        colors = SliderDefaults.colors(
          thumbColor = ClaudeTerracotta,
          activeTrackColor = ClaudeTerracotta,
          inactiveTrackColor = ClaudeBorderLight
        )
      )

      // Live code preview box
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Text(
          text = "fun main() {\n    println(\"DroidCode Preview (${fontSize.toInt()}sp)\")\n}",
          style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize.toInt().sp,
            lineHeight = (fontSize.toInt() + 6).sp,
            color = ClaudeTextPrimary
          ),
          modifier = Modifier.padding(6.dp)
        )
      }
    }

    Divider(color = ClaudeBorderLight)

    // Tab Size Segmented
    ClaudeSettingRow(
      title = "Tab Indentation Size",
      description = "Spaces inserted per tab indentation level."
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(2, 4).forEach { size ->
          val isSelected = editorPrefs.tabSize == size
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSelected) ClaudeTerracottaLight else ClaudePureWhite,
            modifier = Modifier
              .border(
                1.dp,
                if (isSelected) ClaudeTerracotta else ClaudeBorder,
                RoundedCornerShape(6.dp)
              )
              .clickable {
                viewModel.saveEditorPreferences(editorPrefs.copy(tabSize = size))
              }
          ) {
            Text(
              text = "$size Spaces",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) ClaudeTerracotta else ClaudeTextSecondary
              ),
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            )
          }
        }
      }
    }
  }
}

// =========================================================================
// 4. STORAGE & ABOUT PAGE (CLEAN, MINIMAL, NO REDUNDANCY)
// =========================================================================

@Composable
fun StorageAndAboutPage(viewModel: MainViewModel) {
  val context = LocalContext.current
  var freeMb by remember { mutableStateOf(context.filesDir.freeSpace / (1024 * 1024)) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp, vertical = 18.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "Storage & About",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "DroidCode - Autonomous AI Coding Companion & Developer Workspace on Android.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(title = "App Version", description = "1.0.0 (Build 2026.10)") {
      Text("Production", style = MaterialTheme.typography.labelSmall, color = ClaudeTextSecondary)
    }

    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(title = "Android OS", description = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})") {
      Text("ARM64", style = MaterialTheme.typography.labelSmall, color = ClaudeTextSecondary)
    }

    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(title = "Local Device Storage", description = "$freeMb MB Available") {
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .border(1.dp, ClaudeBorder, RoundedCornerShape(6.dp))
          .clickable {
            viewModel.clearCache { updatedMb ->
              freeMb = updatedMb
              Toast.makeText(context, "Cache Cleared. $updatedMb MB Free", Toast.LENGTH_SHORT).show()
            }
          }
      ) {
        Text("Clear Cache", style = MaterialTheme.typography.labelSmall.copy(color = ClaudeTerracotta), modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
      }
    }

    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(
      title = "Privacy & Local Sandboxing",
      description = "Project files remain strictly on device. Only prompts and requested file contexts are sent to configured AI endpoints."
    ) {
      Icon(Icons.Default.Security, contentDescription = null, tint = ClaudeTerracotta, modifier = Modifier.size(16.dp))
    }

    Divider(color = ClaudeBorderLight)

    // Reset All Settings Action
    ClaudeSettingRow(
      title = "Reset All Settings",
      description = "Restore all configurations, AI credentials, and editor preferences to initial defaults."
    ) {
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .border(1.dp, Color(0xFFC62828), RoundedCornerShape(6.dp))
          .clickable { showResetConfirmDialog = true }
      ) {
        Text(
          text = "Reset Defaults",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            color = Color(0xFFC62828)
          ),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }
  }

  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("Reset All Settings?", color = ClaudeTextPrimary) },
      text = { Text("This will reset all AI provider configurations, editor settings, and preferences to defaults.", color = ClaudeTextSecondary) },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.resetAllSettings()
            showResetConfirmDialog = false
            Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
          }
        ) {
          Text("Reset", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel", color = ClaudeTextSecondary)
        }
      },
      containerColor = ClaudePureWhite
    )
  }
}

// =========================================================================
// REUSABLE HELPER COMPOSABLES
// =========================================================================

@Composable
fun ClaudeSettingRow(
  title: String,
  description: String,
  content: @Composable () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = ClaudeTextPrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = ClaudeTextSecondary
      )
    }
    content()
  }
}

@Composable
fun ClaudeLightModelPill(name: String, onClick: () -> Unit) {
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = ClaudePureWhite,
    modifier = Modifier
      .border(1.dp, ClaudeBorder, RoundedCornerShape(6.dp))
      .clickable { onClick() }
  ) {
    Text(
      text = name,
      style = MaterialTheme.typography.labelSmall.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp
      ),
      color = ClaudeTerracotta,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
    )
  }
}
