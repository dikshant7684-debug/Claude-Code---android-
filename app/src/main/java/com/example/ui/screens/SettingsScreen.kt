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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProviderType
import com.example.data.preferences.EditorPreferences
import com.example.data.preferences.GeneralPreferences
import com.example.data.preferences.ToolPermissionPreferences
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
import org.json.JSONObject

enum class SettingsCategory(val title: String, val group: String, val icon: ImageVector) {
  // GENERAL
  GENERAL("General", "GENERAL", Icons.Default.Settings),
  APPEARANCE("Appearance", "GENERAL", Icons.Default.Tune),
  EDITOR("Editor & Typography", "GENERAL", Icons.Default.Code),
  KEYBOARD("Keyboard & Shortcuts", "GENERAL", Icons.Default.Keyboard),

  // AI CONFIGURATION
  AI_PROVIDERS("AI Providers", "AI CONFIGURATION", Icons.Default.Psychology),
  MODELS("Models", "AI CONFIGURATION", Icons.Default.Dns),
  AGENT_BEHAVIOR("Agent Behavior", "AI CONFIGURATION", Icons.Default.Tune),

  // TOOLS & EXTENSIONS
  PLUGINS_MARKETPLACE("Plugins & Marketplaces", "DEVELOPMENT", Icons.Default.Extension),
  MCP_SERVERS("MCP Servers", "DEVELOPMENT", Icons.Default.Computer),
  TOOL_PERMISSIONS("Tool Permissions", "DEVELOPMENT", Icons.Default.Security),

  // APPLICATION
  PRIVACY("Privacy & Security", "APPLICATION", Icons.Default.Security),
  STORAGE("Storage", "APPLICATION", Icons.Default.Storage),
  ABOUT("About Claude Code", "APPLICATION", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
  val configuration = LocalConfiguration.current
  val isWideScreen = configuration.screenWidthDp >= 640

  var selectedCategory by remember { mutableStateOf(SettingsCategory.GENERAL) }
  var showMobileDrawer by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  Scaffold(
    containerColor = ClaudeWarmBg,
    topBar = {
      // Clean, elegant header matching the warm Chat screen theme
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(ClaudeWarmBg)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isWideScreen) {
              IconButton(
                onClick = { showMobileDrawer = true },
                modifier = Modifier
                  .size(34.dp)
                  .testTag("settings_categories_menu_btn")
              ) {
                Icon(
                  Icons.Default.Menu,
                  contentDescription = "Open categories",
                  tint = ClaudeTextPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = "Settings",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
              ),
              color = ClaudeTextPrimary
            )
            if (!isWideScreen) {
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "· ${selectedCategory.title}",
                style = MaterialTheme.typography.bodySmall,
                color = ClaudeTextSecondary
              )
            }
          }

          // Category group pill
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ClaudeSurfaceSubtle,
            modifier = Modifier.border(1.dp, ClaudeBorder, RoundedCornerShape(12.dp))
          ) {
            Text(
              text = selectedCategory.group,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              ),
              color = ClaudeTextSecondary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
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
        // Persistent Left Sidebar for tablet/desktop layout
        ClaudeSettingsSidebar(
          selectedCategory = selectedCategory,
          onSelectCategory = { selectedCategory = it },
          modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(ClaudeSurfaceSubtle)
            .border(width = 1.dp, color = ClaudeBorderLight)
        )
      }

      // Main Settings Content Region
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .background(ClaudeWarmBg)
      ) {
        ClaudeSettingsContentArea(
          category = selectedCategory,
          viewModel = viewModel
        )
      }
    }

    // Mobile Bottom Sheet for category selection
    if (showMobileDrawer && !isWideScreen) {
      ModalBottomSheet(
        onDismissRequest = { showMobileDrawer = false },
        sheetState = sheetState,
        containerColor = ClaudePureWhite,
        scrimColor = Color.Black.copy(alpha = 0.35f)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
        ) {
          Text(
            text = "Settings Categories",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = ClaudeTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
          )
          Divider(color = ClaudeBorderLight)

          ClaudeSettingsSidebar(
            selectedCategory = selectedCategory,
            onSelectCategory = {
              selectedCategory = it
              showMobileDrawer = false
            },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}

// =========================================================================
// SIDEBAR (LIGHT WARM CLAUDE THEME)
// =========================================================================

@Composable
fun ClaudeSettingsSidebar(
  selectedCategory: SettingsCategory,
  onSelectCategory: (SettingsCategory) -> Unit,
  modifier: Modifier = Modifier
) {
  val groups = remember {
    SettingsCategory.values().groupBy { it.group }
  }

  LazyColumn(
    modifier = modifier.padding(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    groups.forEach { (groupName, categories) ->
      item {
        Text(
          text = groupName,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp
          ),
          color = ClaudeTextMuted,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
      }

      items(categories) { category ->
        val isSelected = category == selectedCategory
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSelected) ClaudePureWhite else Color.Transparent,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .border(
              width = 1.dp,
              color = if (isSelected) ClaudeBorder else Color.Transparent,
              shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelectCategory(category) }
            .testTag("settings_cat_${category.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              category.icon,
              contentDescription = null,
              tint = if (isSelected) ClaudeTerracotta else ClaudeTextSecondary,
              modifier = Modifier.size(16.dp)
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

      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

// =========================================================================
// ROUTER FOR SETTINGS CONTENT AREA
// =========================================================================

@Composable
fun ClaudeSettingsContentArea(
  category: SettingsCategory,
  viewModel: MainViewModel
) {
  when (category) {
    SettingsCategory.GENERAL, SettingsCategory.APPEARANCE -> GeneralSettingsPage(viewModel = viewModel)
    SettingsCategory.EDITOR, SettingsCategory.KEYBOARD -> EditorSettingsPage(viewModel = viewModel)
    SettingsCategory.AI_PROVIDERS, SettingsCategory.MODELS -> AiProvidersSettingsPage(viewModel = viewModel)
    SettingsCategory.AGENT_BEHAVIOR -> AgentBehaviorSettingsPage(viewModel = viewModel)
    SettingsCategory.PLUGINS_MARKETPLACE -> PluginsMarketplaceSettingsPage(viewModel = viewModel)
    SettingsCategory.MCP_SERVERS -> McpServersSettingsPage(viewModel = viewModel)
    SettingsCategory.TOOL_PERMISSIONS -> ToolPermissionsSettingsPage(viewModel = viewModel)
    SettingsCategory.PRIVACY, SettingsCategory.STORAGE, SettingsCategory.ABOUT -> PrivacyAndAboutPage(viewModel = viewModel)
  }
}

// =========================================================================
// 1. GENERAL & APPEARANCE SETTINGS PAGE (FULLY WORKING)
// =========================================================================

@Composable
fun GeneralSettingsPage(viewModel: MainViewModel) {
  val generalPrefs by viewModel.generalPreferences.collectAsState()
  val context = LocalContext.current
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 20.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "General Preferences",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Application theme, workspace launch behavior, and developer defaults.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    // Theme Appearance Selector (Working)
    ClaudeSettingRow(
      title = "Theme Appearance",
      description = "Selected theme active across chat and workspace."
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

    // Auto open workspace on startup (Working)
    ClaudeSettingRow(
      title = "Startup Workspace Auto-Reopen",
      description = "Automatically reopen the active project and open files on app launch."
    ) {
      Switch(
        checked = generalPrefs.autoOpenWorkspace,
        onCheckedChange = {
          viewModel.saveGeneralPreferences(generalPrefs.copy(autoOpenWorkspace = it))
        },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ClaudeTerracotta
        )
      )
    }

    Divider(color = ClaudeBorderLight)

    // Code completion / suggestions (Working)
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

    // Editor Quick Symbols Toolbar (Working)
    ClaudeSettingRow(
      title = "Editor Virtual Toolbar",
      description = "Show mobile helper keys (Tab, { }, ;, =, quotes) above keyboard in editor."
    ) {
      Switch(
        checked = generalPrefs.editorToolbarEnabled,
        onCheckedChange = {
          viewModel.saveGeneralPreferences(generalPrefs.copy(editorToolbarEnabled = it))
        },
        colors = SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ClaudeTerracotta
        )
      )
    }

    Divider(color = ClaudeBorderLight)

    // Reset All Settings Action (Working)
    ClaudeSettingRow(
      title = "Reset All Settings",
      description = "Restore all configurations, preferences, and permissions to initial defaults."
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
// 2. EDITOR & KEYBOARD SETTINGS PAGE (FULLY WORKING)
// =========================================================================

@Composable
fun EditorSettingsPage(viewModel: MainViewModel) {
  val editorPrefs by viewModel.editorPreferences.collectAsState()
  var fontSize by remember(editorPrefs) { mutableFloatStateOf(editorPrefs.fontSizeSp.toFloat()) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 20.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Editor & Typography",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Code typography, line numbering gutter, and tab indentation.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    // Line Numbers Toggle (Working)
    ClaudeSettingRow(
      title = "Line Numbers Gutter",
      description = "Display line numbers along the left edge of the code editor in the Code tab."
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

    // Word Wrap Toggle (Working)
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

    // Font Size Slider with Live Preview (Working)
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Font Size", style = MaterialTheme.typography.bodyMedium, color = ClaudeTextPrimary)
        Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ClaudeTerracotta)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text("Controls editor font scale and line spacing.", style = MaterialTheme.typography.bodySmall, color = ClaudeTextSecondary)

      Slider(
        value = fontSize,
        onValueChange = {
          fontSize = it
          viewModel.saveEditorPreferences(editorPrefs.copy(fontSizeSp = it.toInt()))
        },
        valueRange = 10f..22f,
        steps = 11,
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
          text = "fun main() {\n    println(\"Claude Code Preview (${fontSize.toInt()}sp)\")\n}",
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

    // Tab Size Segmented (Working)
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

    Divider(color = ClaudeBorderLight)

    // Keyboard Shortcuts Cheat Sheet
    Text(
      text = "Keyboard Shortcuts",
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    ClaudeShortcutRow("Save Active File", "Ctrl", "S")
    ClaudeShortcutRow("Send AI Prompt", "Ctrl", "Enter")
    ClaudeShortcutRow("Run Syntax Check", "Ctrl", "B")
    ClaudeShortcutRow("Close File Tab", "Ctrl", "W")
  }
}

@Composable
fun ClaudeShortcutRow(action: String, mod: String, key: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(action, style = MaterialTheme.typography.bodySmall, color = ClaudeTextSecondary)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      Surface(
        shape = RoundedCornerShape(4.dp),
        color = ClaudePureWhite,
        modifier = Modifier.border(1.dp, ClaudeBorder, RoundedCornerShape(4.dp))
      ) {
        Text(mod, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = ClaudeTextPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
      }
      Text("+", style = MaterialTheme.typography.bodySmall, color = ClaudeTextMuted)
      Surface(
        shape = RoundedCornerShape(4.dp),
        color = ClaudePureWhite,
        modifier = Modifier.border(1.dp, ClaudeBorder, RoundedCornerShape(4.dp))
      ) {
        Text(key, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = ClaudeTextPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
      }
    }
  }
}

// =========================================================================
// 3. AI PROVIDERS & CREDENTIALS PAGE (FULLY WORKING)
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
      .padding(horizontal = 24.dp, vertical = 20.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "AI Providers & Models",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Configure API endpoints, model identifiers, and credentials for autonomous agent reasoning.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    // Provider Protocol Dropdown (Working)
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
      placeholder = { Text("e.g. https://api.openai.com/v1 or http://10.0.2.2:11434/v1") },
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
        ClaudeLightModelPill("OpenAI (/v1)") {
          baseUrl = "https://api.openai.com/v1"
          providerType = ProviderType.OPENAI
        }
      }
      item {
        ClaudeLightModelPill("OpenRouter") {
          baseUrl = "https://openrouter.ai/api/v1"
          providerType = ProviderType.OPENAI
        }
      }
      item {
        ClaudeLightModelPill("Anthropic") {
          baseUrl = "https://api.anthropic.com/v1"
          providerType = ProviderType.ANTHROPIC
        }
      }
      item {
        ClaudeLightModelPill("Ollama (Local)") {
          baseUrl = "http://10.0.2.2:11434/v1"
          providerType = ProviderType.CUSTOM
        }
      }
      item {
        ClaudeLightModelPill("Groq") {
          baseUrl = "https://api.groq.com/openai/v1"
          providerType = ProviderType.OPENAI
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

    // Popular Fallback Model Identifiers
    Text("Popular Default Identifiers:", style = MaterialTheme.typography.labelSmall, color = ClaudeTextSecondary)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      item { ClaudeLightModelPill("claude-3-7-sonnet-20250219") { modelId = "claude-3-7-sonnet-20250219" } }
      item { ClaudeLightModelPill("claude-3-5-sonnet-20241022") { modelId = "claude-3-5-sonnet-20241022" } }
      item { ClaudeLightModelPill("gpt-4o") { modelId = "gpt-4o" } }
      item { ClaudeLightModelPill("gemini-2.0-flash") { modelId = "gemini-2.0-flash" } }
      item { ClaudeLightModelPill("llama3.2") { modelId = "llama3.2" } }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Actions: Test Connection & Save
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
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
// 4. AGENT BEHAVIOR PAGE (FULLY WORKING)
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
      .padding(horizontal = 24.dp, vertical = 20.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Agent Behavior & Safeguards",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Configure reasoning boundaries, automated execution approvals, and temperature.",
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
      description = "Execute read-only file inspections and diagnostics without prompting."
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
// 5. LOCAL MCP SERVERS PAGE (FULLY WORKING WITH EDIT/ADD)
// =========================================================================

@Composable
fun McpServersSettingsPage(viewModel: MainViewModel) {
  val mcpJson by viewModel.mcpServersJson.collectAsState()
  var showConfigEditor by remember { mutableStateOf(false) }
  var showDocsDialog by remember { mutableStateOf(false) }
  var showAddPresetDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

  // Parse configured MCP servers
  val parsedServers = remember(mcpJson) {
    try {
      val obj = JSONObject(mcpJson)
      val serversObj = obj.optJSONObject("mcpServers") ?: JSONObject()
      val list = mutableListOf<Pair<String, String>>()
      serversObj.keys().forEach { key ->
        val serverConfig = serversObj.optJSONObject(key)
        val cmd = serverConfig?.optString("command") ?: "command"
        list.add(key to cmd)
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 20.dp)
  ) {
    Text(
      text = "Local MCP servers",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = "Add and manage MCP servers that you use with Claude Code.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )

    Spacer(modifier = Modifier.height(16.dp))
    Divider(color = ClaudeBorderLight, thickness = 1.dp)

    if (parsedServers.isEmpty()) {
      // Empty state matching clean Claude design
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Surface(
            shape = CircleShape,
            color = ClaudeSurfaceSubtle,
            modifier = Modifier.size(56.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Computer,
                contentDescription = null,
                tint = ClaudeTextMuted,
                modifier = Modifier.size(28.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No servers added",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = ClaudeTextPrimary
          )
          Spacer(modifier = Modifier.height(16.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Edit Config
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ClaudePureWhite,
              modifier = Modifier
                .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
                .clickable { showConfigEditor = true }
                .testTag("edit_config_btn")
            ) {
              Text(
                text = "Edit Config",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = ClaudeTextPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
              )
            }

            // Quick Add Preset
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ClaudeTerracotta,
              modifier = Modifier
                .clickable { showAddPresetDialog = true }
                .testTag("add_mcp_preset_btn")
            ) {
              Text(
                text = "+ Add Server",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Developer docs",
            style = MaterialTheme.typography.bodySmall,
            color = ClaudeTerracotta,
            modifier = Modifier.clickable { showDocsDialog = true }
          )
        }
      }
    } else {
      // Configured servers list
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(parsedServers) { (name, command) ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ClaudePureWhite,
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ClaudeTextPrimary)
                  Text(text = command, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = ClaudeTextSecondary)
                }
              }

              IconButton(
                onClick = {
                  try {
                    val obj = JSONObject(mcpJson)
                    val sObj = obj.optJSONObject("mcpServers") ?: JSONObject()
                    sObj.remove(name)
                    obj.put("mcpServers", sObj)
                    viewModel.saveMcpServersJson(obj.toString(2))
                    Toast.makeText(context, "Removed $name", Toast.LENGTH_SHORT).show()
                  } catch (_: Exception) {}
                }
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ClaudeTextMuted, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(onClick = { showDocsDialog = true }) {
          Text("Developer docs", color = ClaudeTerracotta)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ClaudePureWhite,
            modifier = Modifier
              .border(1.dp, ClaudeBorder, RoundedCornerShape(8.dp))
              .clickable { showConfigEditor = true }
          ) {
            Text("Edit Config", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = ClaudeTextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = ClaudeTerracotta,
            modifier = Modifier.clickable { showAddPresetDialog = true }
          ) {
            Text("+ Add Server", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
          }
        }
      }
    }
  }

  // Edit Config Dialog
  if (showConfigEditor) {
    var draftJson by remember { mutableStateOf(mcpJson) }
    var jsonError by remember { mutableStateOf<String?>(null) }

    Dialog(
      onDismissRequest = { showConfigEditor = false },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = ClaudePureWhite,
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .height(480.dp)
          .border(1.dp, ClaudeBorder, RoundedCornerShape(12.dp))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Edit MCP Servers JSON", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = ClaudeTextPrimary)
            IconButton(onClick = { showConfigEditor = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = ClaudeTextSecondary)
            }
          }
          Divider(color = ClaudeBorderLight)
          Spacer(modifier = Modifier.height(10.dp))

          BasicTextField(
            value = draftJson,
            onValueChange = {
              draftJson = it
              jsonError = null
            },
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .background(ClaudeSurfaceSubtle, RoundedCornerShape(8.dp))
              .border(1.dp, if (jsonError != null) Color(0xFFC62828) else ClaudeBorder, RoundedCornerShape(8.dp))
              .padding(12.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = ClaudeTextPrimary
            )
          )

          if (jsonError != null) {
            Text(jsonError!!, style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828), modifier = Modifier.padding(top = 4.dp))
          }

          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { showConfigEditor = false }) {
              Text("Cancel", color = ClaudeTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                try {
                  JSONObject(draftJson) // validate
                  viewModel.saveMcpServersJson(draftJson)
                  showConfigEditor = false
                  Toast.makeText(context, "MCP Config Saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                  jsonError = "Invalid JSON: ${e.message}"
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = ClaudeTerracotta)
            ) {
              Text("Save Config", color = Color.White)
            }
          }
        }
      }
    }
  }

  // Quick Preset Add Dialog
  if (showAddPresetDialog) {
    AlertDialog(
      onDismissRequest = { showAddPresetDialog = false },
      title = { Text("Select MCP Server Preset", color = ClaudeTextPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(
            "Filesystem" to "npx -y @modelcontextprotocol/server-filesystem /workspace",
            "Postgres" to "npx -y @modelcontextprotocol/server-postgres postgresql://localhost/db",
            "GitHub" to "npx -y @modelcontextprotocol/server-github",
            "Memory" to "npx -y @modelcontextprotocol/server-memory"
          ).forEach { (name, cmd) ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = ClaudePureWhite,
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ClaudeBorder, RoundedCornerShape(6.dp))
                .clickable {
                  try {
                    val root = try { JSONObject(mcpJson) } catch (_: Exception) { JSONObject() }
                    val sObj = root.optJSONObject("mcpServers") ?: JSONObject()
                    val srv = JSONObject().apply {
                      put("command", cmd.split(" ").first())
                      put("args", org.json.JSONArray(cmd.split(" ").drop(1)))
                    }
                    sObj.put(name.lowercase(), srv)
                    root.put("mcpServers", sObj)
                    viewModel.saveMcpServersJson(root.toString(2))
                    showAddPresetDialog = false
                    Toast.makeText(context, "Added $name MCP Server", Toast.LENGTH_SHORT).show()
                  } catch (_: Exception) {}
                }
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ClaudeTextPrimary)
                Text(cmd, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp), color = ClaudeTextSecondary)
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showAddPresetDialog = false }) {
          Text("Cancel", color = ClaudeTextSecondary)
        }
      },
      containerColor = ClaudePureWhite
    )
  }

  // Developer Docs Dialog
  if (showDocsDialog) {
    AlertDialog(
      onDismissRequest = { showDocsDialog = false },
      title = { Text("Model Context Protocol (MCP)", color = ClaudeTextPrimary) },
      text = {
        Text(
          text = "The Model Context Protocol (MCP) is an open standard that enables AI models to securely discover, inspect, and execute local tools, resources, and databases.\n\nConfigured MCP servers communicate via JSON-RPC to provide Claude Code with context, specialized APIs, and file access.",
          style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
          color = ClaudeTextSecondary
        )
      },
      confirmButton = {
        TextButton(onClick = { showDocsDialog = false }) {
          Text("Done", color = ClaudeTerracotta, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = ClaudePureWhite
    )
  }
}

// =========================================================================
// 6. TOOL PERMISSIONS PAGE (FULLY WORKING)
// =========================================================================

@Composable
fun ToolPermissionsSettingsPage(viewModel: MainViewModel) {
  val toolPerms by viewModel.toolPermissions.collectAsState()

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 20.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Tool Permissions & Security",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Grant or restrict agent tool access to the filesystem and project inspection.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(
      title = "read_file Permission",
      description = "Allow AI agent to inspect and read workspace source files."
    ) {
      Switch(
        checked = toolPerms.readFileAllowed,
        onCheckedChange = {
          viewModel.saveToolPermissions(toolPerms.copy(readFileAllowed = it))
        },
        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ClaudeTerracotta)
      )
    }

    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(
      title = "write_file Safety Review",
      description = "Require user confirmation before new source files are written to workspace."
    ) {
      Switch(
        checked = toolPerms.writeFileRequireReview,
        onCheckedChange = {
          viewModel.saveToolPermissions(toolPerms.copy(writeFileRequireReview = it))
        },
        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ClaudeTerracotta)
      )
    }

    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(
      title = "apply_patch Safety Review",
      description = "Require user confirmation before applying diff hunks to existing code."
    ) {
      Switch(
        checked = toolPerms.applyPatchRequireReview,
        onCheckedChange = {
          viewModel.saveToolPermissions(toolPerms.copy(applyPatchRequireReview = it))
        },
        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ClaudeTerracotta)
      )
    }

    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(
      title = "inspect_workspace Permission",
      description = "Allow autonomous discovery and listing of project directories."
    ) {
      Switch(
        checked = toolPerms.inspectWorkspaceAllowed,
        onCheckedChange = {
          viewModel.saveToolPermissions(toolPerms.copy(inspectWorkspaceAllowed = it))
        },
        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ClaudeTerracotta)
      )
    }
  }
}

// =========================================================================
// 7. PRIVACY, STORAGE & ABOUT PAGE (FULLY WORKING)
// =========================================================================

@Composable
fun PrivacyAndAboutPage(viewModel: MainViewModel) {
  val context = LocalContext.current
  var freeMb by remember { mutableStateOf(context.filesDir.freeSpace / (1024 * 1024)) }

  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp, vertical = 20.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "About Claude Code",
      style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
      color = ClaudeTextPrimary
    )
    Text(
      text = "Claude Code - Autonomous AI Coding Companion & Developer Workspace on Android.",
      style = MaterialTheme.typography.bodyMedium,
      color = ClaudeTextSecondary
    )
    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(title = "Version", description = "1.0.0 (Build 2026.10)") {
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
        Text("Clear Cache", style = MaterialTheme.typography.labelSmall.copy(color = ClaudeTerracotta), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
      }
    }

    Divider(color = ClaudeBorderLight)

    ClaudeSettingRow(
      title = "Privacy & Local Sandboxing",
      description = "Project files remain strictly on device. Only prompts and requested file contexts are sent to configured AI endpoints."
    ) {
      Icon(Icons.Default.Security, contentDescription = null, tint = ClaudeTerracotta, modifier = Modifier.size(16.dp))
    }
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
