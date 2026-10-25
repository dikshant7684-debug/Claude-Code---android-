package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.ripple
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageRole
import com.example.data.model.UiChatMessage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.formatting.SmartMessageBody
import com.example.ui.theme.DarkUserBubble
import com.example.ui.theme.DarkUserBubbleBorder
import com.example.ui.theme.DroidCodeTheme
import com.example.ui.theme.DroidOrange
import com.example.ui.theme.DroidOrangeDisabled
import com.example.ui.theme.LightOnBackground
import com.example.ui.theme.LightUserBubble
import com.example.ui.theme.LightUserBubbleBorder
import com.example.ui.viewmodel.ChatUiState
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.MainViewModel

enum class DroidNavTab(val label: String, val icon: ImageVector, val tag: String) {
  CHAT("Chat", Icons.Default.Home, "nav_tab_chat"),
  SETTINGS("Settings", Icons.Default.Settings, "nav_tab_settings")
}

data class SuggestionItem(
  val title: String,
  val prompt: String,
  val icon: ImageVector
)

/**
 * Stateful entry point integrating with MainViewModel and ChatViewModel
 */
@Composable
fun ChatScreen(
  viewModel: MainViewModel,
  onNavigateToWorkspace: () -> Unit = {},
  onNavigateToSettings: () -> Unit = {},
  onOpenDrawer: () -> Unit = {},
  onTabSelected: (DroidNavTab) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val chatViewModel = remember(viewModel) { ChatViewModel(mainViewModel = viewModel) }
  val uiState by chatViewModel.uiState.collectAsState()

  ChatScreenContent(
    uiState = uiState,
    onInputChanged = chatViewModel::onInputTextChanged,
    onSend = chatViewModel::onSendMessage,
    onRegenerate = chatViewModel::onRegeneratePrompt,
    onModelSelected = chatViewModel::onModelSelected,
    onSuggestionClick = chatViewModel::onSuggestionClick,
    onCancelTask = chatViewModel::onCancelTask,
    onOpenFile = { relativePath ->
      viewModel.openFileByRelativePath(relativePath)
      onNavigateToWorkspace()
    },
    onNavigateToSettings = onNavigateToSettings,
    onOpenDrawer = onOpenDrawer,
    currentTab = DroidNavTab.CHAT,
    onTabSelected = { tab ->
      when (tab) {
        DroidNavTab.CHAT -> { /* already here */ }
        DroidNavTab.SETTINGS -> onNavigateToSettings()
      }
      onTabSelected(tab)
    },
    modifier = modifier
  )
}

/**
 * Stateless, Previewable ChatScreen content adhering strictly to M3 guidelines
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreenContent(
  uiState: ChatUiState,
  onInputChanged: (String) -> Unit,
  onSend: () -> Unit,
  onModelSelected: (String) -> Unit,
  onSuggestionClick: (String) -> Unit,
  onCancelTask: () -> Unit,
  onOpenFile: (String) -> Unit,
  onNavigateToSettings: () -> Unit,
  onOpenDrawer: () -> Unit = {},
  onRegenerate: (String) -> Unit = {},
  currentTab: DroidNavTab = DroidNavTab.CHAT,
  onTabSelected: (DroidNavTab) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val density = LocalDensity.current
  val isKeyboardOpen = WindowInsets.isImeVisible || WindowInsets.ime.getBottom(density) > 0

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    contentWindowInsets = WindowInsets.statusBars,
    topBar = {
      ChatTopAppBar(
        selectedModel = uiState.selectedModel,
        onNavigateToSettings = onNavigateToSettings,
        onOpenDrawer = onOpenDrawer
      )
    }
  ) { paddingValues ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = paddingValues.calculateTopPadding())
        .imePadding(),
      contentAlignment = Alignment.TopCenter
    ) {
      // Responsive width capping on wide screens/tablets (cap at 720dp)
      val contentWidth = if (maxWidth >= 600.dp) 720.dp else maxWidth

      Column(
        modifier = Modifier
          .widthIn(max = contentWidth)
          .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Content Area: Empty State OR Message History
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          if (uiState.messages.isEmpty()) {
            EmptyChatState(
              onSuggestionClick = { prompt ->
                onSuggestionClick(prompt)
                focusRequester.requestFocus()
                keyboardController?.show()
              }
            )
            val currentStatus = uiState.currentTask?.currentActivity?.takeIf { it.isNotBlank() }
              ?: uiState.currentTask?.currentPlan?.takeIf { it.isNotBlank() }
              ?: "Generating response..."
            MessagesList(
              messages = uiState.messages,
              isLoading = uiState.isLoading,
              statusText = currentStatus,
              modelName = uiState.selectedModel,
              onOpenFile = onOpenFile,
              onCancelTask = onCancelTask,
              onRegenerate = onRegenerate
            )
          }
        }

        // Pinned Composer directly above keyboard or bottom navigation
        ChatComposerCard(
          inputText = uiState.inputText,
          onInputChanged = onInputChanged,
          onSend = onSend,
          selectedModel = uiState.selectedModel,
          availableModels = uiState.availableModels,
          onModelSelected = onModelSelected,
          isLoading = uiState.isLoading,
          onCancelTask = onCancelTask,
          focusRequester = focusRequester,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Bottom Navigation Bar only shown when keyboard is NOT open
        if (!isKeyboardOpen) {
          ChatBottomNavigationBar(
            currentTab = currentTab,
            onTabSelected = onTabSelected
          )
        }
      }
    }
  }
}

/**
 * Top App Bar with Logo, Title (20sp semibold), readable Model Badge (13sp), and 48dp touch target settings button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
  selectedModel: String,
  onNavigateToSettings: () -> Unit,
  onOpenDrawer: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  TopAppBar(
    navigationIcon = {
      IconButton(
        onClick = onOpenDrawer,
        modifier = Modifier
          .size(48.dp)
          .testTag("chat_open_drawer_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Open Drawer",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(24.dp)
        )
      }
    },
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Logo Brand Icon
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.cat_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(24.dp).clip(CircleShape)
          )
        }

        // Title at 20sp semibold
        Text(
          text = "Claude Code",
          style = MaterialTheme.typography.titleLarge.copy(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.onSurface
        )

        // Readable Model Badge at 13sp
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Text(
            text = selectedModel,
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }
    },
    actions = {
      IconButton(
        onClick = onNavigateToSettings,
        modifier = Modifier
          .size(48.dp)
          .semantics { contentDescription = "Open Settings" }
          .testTag("chat_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(24.dp)
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      titleContentColor = MaterialTheme.colorScheme.onBackground
    ),
    modifier = modifier.testTag("chat_top_bar")
  )
}

/**
 * Empty State: Vertically centered greeting (34-40sp serif), subtitle (17-18sp muted), and suggestion chips
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyChatState(
  onSuggestionClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val suggestions = listOf(
    SuggestionItem("Build Android App", "Build a complete Jetpack Compose screen with state", Icons.Default.Android),
    SuggestionItem("Fix Bug", "Inspect workspace source files and fix syntax or compile errors", Icons.Default.BugReport),
    SuggestionItem("Generate Unit Tests", "Write unit tests verifying application state logic", Icons.Default.CheckCircle),
    SuggestionItem("Explain Architecture", "Review the project structure, models, and state flows", Icons.Default.Code)
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Brand Cat emblem
    Image(
      painter = painterResource(id = R.drawable.cat_logo),
      contentDescription = "DroidCode Logo",
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .testTag("app_logo_image")
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Greeting "Good to see you." (34-40sp, serif font, tight line height)
    Text(
      text = "Good to see you.",
      style = MaterialTheme.typography.displayLarge.copy(
        fontSize = 38.sp,
        lineHeight = 42.sp,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal
      ),
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center,
      modifier = Modifier.testTag("chat_greeting_text")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Subtitle: 17-18sp, muted onSurfaceVariant
    Text(
      text = "Your native autonomous coding assistant on Android.",
      style = MaterialTheme.typography.titleMedium.copy(
        fontSize = 17.sp,
        lineHeight = 24.sp
      ),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.testTag("chat_subtitle_text")
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Suggestion chips in FlowRow with 10dp spacing
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalArrangement = Arrangement.spacedBy(10.dp),
      maxItemsInEachRow = 2
    ) {
      suggestions.forEach { item ->
        SuggestionChipItem(
          label = item.title,
          icon = item.icon,
          onClick = { onSuggestionClick(item.prompt) },
          modifier = Modifier.padding(horizontal = 4.dp)
        )
      }
    }
  }
}

/**
 * Suggestion chip: 48dp minimum height, 16dp horizontal padding, 20dp icons, 15sp text
 */
@Composable
fun SuggestionChipItem(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 1.dp,
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .semantics { contentDescription = "Suggestion: $label" }
      .testTag("suggestion_chip_${label.lowercase().replace(" ", "_")}")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

/**
 * Composer: Rounded card (28dp corners, 64dp min height, subtle border),
 * multi-line text field (16sp, up to 6 lines),
 * bottom row with 48dp attach button, model dropdown pill, and 48dp orange circular send button.
 */
@Composable
fun ChatComposerCard(
  inputText: String,
  onInputChanged: (String) -> Unit,
  onSend: () -> Unit,
  selectedModel: String,
  availableModels: List<String>,
  onModelSelected: (String) -> Unit,
  isLoading: Boolean,
  onCancelTask: () -> Unit = {},
  focusRequester: FocusRequester,
  modifier: Modifier = Modifier
) {
  var modelMenuExpanded by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val isSendEnabled = inputText.trim().isNotEmpty()

  Surface(
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 2.dp,
    modifier = modifier
      .defaultMinSize(minHeight = 64.dp)
      .animateContentSize(tween(180))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Multi-line OutlinedTextField (16sp, growing up to 6 lines, then scrolling)
      OutlinedTextField(
        value = inputText,
        onValueChange = onInputChanged,
        placeholder = {
          Text(
            text = "What would you like to build today?",
            style = MaterialTheme.typography.bodyLarge.copy(
              fontSize = 16.sp,
              lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
          fontSize = 16.sp,
          lineHeight = 24.sp,
          color = MaterialTheme.colorScheme.onSurface
        ),
        maxLines = 6,
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Sentences,
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(
          onSend = {
            if (isSendEnabled) onSend()
          }
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          disabledContainerColor = Color.Transparent,
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester)
          .testTag("chat_composer_input")
      )

      // Bottom Row: "+" Attach (48dp), Model Selector Pill, Circular Send (48dp)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // "+" Attach IconButton (48dp touch target)
          IconButton(
            onClick = {
              Toast.makeText(context, "Attach file or context", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .size(48.dp)
              .semantics { contentDescription = "Attach file or workspace context" }
              .testTag("composer_attach_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Attach",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )
          }

          // Model Selector Pill with Dropdown
          Box {
            Surface(
              onClick = { modelMenuExpanded = true },
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier
                .defaultMinSize(minHeight = 40.dp)
                .semantics { contentDescription = "Select AI model: currently $selectedModel" }
                .testTag("composer_model_selector")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = selectedModel,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            DropdownMenu(
              expanded = modelMenuExpanded,
              onDismissRequest = { modelMenuExpanded = false },
              modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
              availableModels.forEach { model ->
                DropdownMenuItem(
                  text = {
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      if (model == selectedModel) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(18.dp)
                        )
                      } else {
                        Spacer(modifier = Modifier.width(18.dp))
                      }
                      Text(
                        text = model,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  },
                  onClick = {
                    onModelSelected(model)
                    modelMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        // Circular Orange Send Button (48dp touch target)
        val buttonColor by animateColorAsState(
          targetValue = when {
            isLoading -> MaterialTheme.colorScheme.error
            isSendEnabled -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
          },
          animationSpec = tween(160),
          label = "send_button_color"
        )

        IconButton(
          onClick = {
            if (isLoading) {
              onCancelTask()
            } else if (isSendEnabled) {
              onSend()
            }
          },
          enabled = isSendEnabled || isLoading,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(buttonColor)
            .semantics {
              contentDescription = if (isLoading) "Stop generation" else "Send message"
            }
            .testTag("composer_send_button")
        ) {
          if (isLoading) {
            Icon(
              imageVector = Icons.Default.Stop,
              contentDescription = "Stop",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          } else {
            Icon(
              imageVector = Icons.Default.ArrowUpward,
              contentDescription = "Send",
              tint = if (isSendEnabled) Color.White else DroidOrangeDisabled,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Message history list with auto-scroll to latest message
 */
/**
 * Text-to-Speech manager state holder for assistant responses
 */
class TtsState(
  val speakingMessageId: String?,
  val speak: (String, String) -> Unit,
  val stop: () -> Unit
)

@Composable
fun rememberTtsState(): TtsState {
  val context = LocalContext.current
  var tts by remember { mutableStateOf<TextToSpeech?>(null) }
  var speakingMessageId by remember { mutableStateOf<String?>(null) }
  var isReady by remember { mutableStateOf(false) }

  DisposableEffect(context) {
    val speech = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        isReady = true
      }
    }
    tts = speech
    onDispose {
      speech.stop()
      speech.shutdown()
    }
  }

  return remember(tts, speakingMessageId, isReady) {
    TtsState(
      speakingMessageId = speakingMessageId,
      speak = { id, text ->
        if (speakingMessageId == id) {
          tts?.stop()
          speakingMessageId = null
        } else {
          tts?.stop()
          val cleanText = text
            .replace("```[a-zA-Z]*".toRegex(), "")
            .replace("```", "")
            .replace("#", "")
            .replace("*", "")
            .trim()
          speakingMessageId = id
          tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
              speakingMessageId = null
            }
            override fun onError(utteranceId: String?) {
              speakingMessageId = null
            }
          })
          tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, id)
        }
      },
      stop = {
        tts?.stop()
        speakingMessageId = null
      }
    )
  }
}

enum class ThumbState {
  NONE, UP, DOWN
}

/**
 * Subtle action icon button with hover / press animation, outline style, and dark/light support
 */
@Composable
fun ToolbarActionButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
  tag: String = ""
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isPressed by interactionSource.collectIsPressedAsState()

  val backgroundColor by animateColorAsState(
    targetValue = when {
      isPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
      isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
      else -> Color.Transparent
    },
    label = "toolbar_btn_bg"
  )

  Box(
    modifier = modifier
      .size(34.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(bounded = true, radius = 17.dp),
        onClick = onClick
      )
      .semantics { this.contentDescription = contentDescription }
      .testTag(tag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(17.dp)
    )
  }
}

/**
 * Action toolbar directly below AI-generated message bubbles, matching Claude mobile layout:
 * Copy, Play (TTS), Share, Thumbs Up, Thumbs Down, Regenerate.
 */
@Composable
fun AssistantActionToolbar(
  message: UiChatMessage,
  isSpeaking: Boolean,
  onPlayClick: () -> Unit,
  onRegenerateClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var hasCopied by remember { mutableStateOf(false) }
  var thumbState by remember { mutableStateOf(ThumbState.NONE) }

  LaunchedEffect(hasCopied) {
    if (hasCopied) {
      delay(2000)
      hasCopied = false
    }
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // 1. Copy
    ToolbarActionButton(
      icon = if (hasCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
      contentDescription = if (hasCopied) "Copied!" else "Copy message",
      tint = if (hasCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
      tag = "ai_msg_action_copy",
      onClick = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Claude Code Message", message.content))
        hasCopied = true
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
      }
    )

    // 2. Play (Text-to-Speech)
    ToolbarActionButton(
      icon = if (isSpeaking) Icons.Outlined.Stop else Icons.Outlined.VolumeUp,
      contentDescription = if (isSpeaking) "Stop speech" else "Read aloud",
      tint = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
      tag = "ai_msg_action_tts",
      onClick = onPlayClick
    )

    // 3. Share
    ToolbarActionButton(
      icon = Icons.Outlined.Share,
      contentDescription = "Share response",
      tag = "ai_msg_action_share",
      onClick = {
        val sendIntent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, message.content)
          type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share response")
        context.startActivity(shareIntent)
      }
    )

    // 4. Thumbs Up
    ToolbarActionButton(
      icon = if (thumbState == ThumbState.UP) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
      contentDescription = "Thumbs up",
      tint = if (thumbState == ThumbState.UP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
      tag = "ai_msg_action_thumbs_up",
      onClick = {
        thumbState = if (thumbState == ThumbState.UP) ThumbState.NONE else ThumbState.UP
        if (thumbState == ThumbState.UP) {
          Toast.makeText(context, "Thanks for the feedback!", Toast.LENGTH_SHORT).show()
        }
      }
    )

    // 5. Thumbs Down
    ToolbarActionButton(
      icon = if (thumbState == ThumbState.DOWN) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
      contentDescription = "Thumbs down",
      tint = if (thumbState == ThumbState.DOWN) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
      tag = "ai_msg_action_thumbs_down",
      onClick = {
        thumbState = if (thumbState == ThumbState.DOWN) ThumbState.NONE else ThumbState.DOWN
        if (thumbState == ThumbState.DOWN) {
          Toast.makeText(context, "Feedback recorded", Toast.LENGTH_SHORT).show()
        }
      }
    )

    // 6. Regenerate
    ToolbarActionButton(
      icon = Icons.Outlined.Refresh,
      contentDescription = "Regenerate response",
      tag = "ai_msg_action_regenerate",
      onClick = onRegenerateClick
    )
  }
}

/**
 * Message history list with auto-scroll to latest message
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessagesList(
  messages: List<UiChatMessage>,
  isLoading: Boolean,
  onOpenFile: (String) -> Unit,
  onCancelTask: () -> Unit,
  onRegenerate: (String) -> Unit = {},
  statusText: String = "Generating response...",
  modelName: String = "Claude Code",
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()
  val ttsState = rememberTtsState()

  val isKeyboardOpen = WindowInsets.isImeVisible
  LaunchedEffect(messages.size, isLoading, isKeyboardOpen) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  LazyColumn(
    state = listState,
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
  ) {
    items(messages, key = { it.id }) { message ->
      when (message.role) {
        MessageRole.USER -> UserMessageBubble(message = message)
        MessageRole.ASSISTANT -> {
          val messageIndex = messages.indexOf(message)
          val previousUserPrompt = if (messageIndex > 0) {
            messages.subList(0, messageIndex).lastOrNull { it.role == MessageRole.USER }?.content ?: ""
          } else ""

          AssistantMessageItem(
            message = message,
            userPrompt = previousUserPrompt,
            onOpenFile = onOpenFile,
            ttsState = ttsState,
            onRegenerate = { onRegenerate(previousUserPrompt) }
          )
        }
        MessageRole.SYSTEM -> {}
      }
    }

    if (isLoading) {
      item {
        LoadingIndicatorRow(
          statusText = statusText,
          modelName = modelName,
          onCancelTask = onCancelTask
        )
      }
    }
  }
}

/**
 * User Message: Right-aligned bubble in light orange, max width 85%, min 16sp font
 */
@Composable
fun UserMessageBubble(
  message: UiChatMessage,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    Surface(
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
      color = LightUserBubble,
      border = BorderStroke(1.dp, LightUserBubbleBorder),
      modifier = Modifier
        .widthIn(max = 320.dp)
        .padding(start = 48.dp)
    ) {
      Text(
        text = message.content,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = 16.sp,
          lineHeight = 24.sp
        ),
        color = LightOnBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
      )
    }
  }
}

/**
 * Assistant Message: Full width, smart response formatted text, monospace prompt blocks,
 * syntax highlighted code blocks, tables, and horizontal action toolbar.
 */
@Composable
fun AssistantMessageItem(
  message: UiChatMessage,
  userPrompt: String = "",
  onOpenFile: (String) -> Unit,
  ttsState: TtsState,
  onRegenerate: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Header Row: Assistant Avatar and Name
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Image(
        painter = painterResource(id = R.drawable.cat_logo),
        contentDescription = "DroidCode Assistant",
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
      )
      Text(
        text = "DroidCode",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Smart Response Formatting: Prompts, Code, Commands, Tables, Lists & Normal text
    SmartMessageBody(
      content = message.content,
      userPrompt = userPrompt,
      onOpenFile = onOpenFile
    )

    // Action Toolbar directly below the AI text bubble
    if (!message.isRunning && message.content.isNotBlank()) {
      AssistantActionToolbar(
        message = message,
        isSpeaking = ttsState.speakingMessageId == message.id,
        onPlayClick = { ttsState.speak(message.id, message.content) },
        onRegenerateClick = onRegenerate
      )
    }
  }
}

/**
 * Markdown Body Parser: Handles text, inline formatting, and code blocks
 */
@Composable
fun MarkdownMessageBody(
  content: String,
  onOpenFile: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val segments = remember(content) { parseMarkdownSegments(content) }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    segments.forEach { segment ->
      when (segment) {
        is MarkdownSegment.CodeBlock -> {
          CodeBlockCard(
            code = segment.code,
            language = segment.language
          )
        }
        is MarkdownSegment.Paragraph -> {
          SelectionContainer {
            Text(
              text = segment.text,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

/**
 * Monospace Code Block Card with rounded surface, horizontal scroll, and copy button
 */
@Composable
fun CodeBlockCard(
  code: String,
  language: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier.fillMaxWidth()
  ) {
    Column {
      // Code Block Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = language.ifBlank { "code" },
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          ),
          color = MaterialTheme.colorScheme.primary
        )

        IconButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("code_snippet", code))
            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = "Copy code snippet" }
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy snippet",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Horizontally scrollable code area
      SelectionContainer {
        Text(
          text = code,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 18.sp
          ),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(14.dp)
        )
      }
    }
  }
}

/**
 * Animated Running / Loading Row
 */
@Composable
fun LoadingIndicatorRow(
  statusText: String = "Generating response...",
  modelName: String = "Claude Code",
  onCancelTask: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(18.dp),
      strokeWidth = 2.dp,
      color = MaterialTheme.colorScheme.primary
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = statusText,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = modelName,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    IconButton(
      onClick = onCancelTask,
      modifier = Modifier
        .size(40.dp)
        .semantics { contentDescription = "Cancel active generation task" }
    ) {
      Icon(
        imageVector = Icons.Default.Stop,
        contentDescription = "Stop generation",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

/**
 * Material 3 NavigationBar at default height (~80dp) with 24dp icons, 12sp labels, and orange indicator pill
 */
@Composable
fun ChatBottomNavigationBar(
  currentTab: DroidNavTab,
  onTabSelected: (DroidNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    tonalElevation = 2.dp,
    modifier = modifier
      .fillMaxWidth()
      .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
      .testTag("droid_bottom_navigation")
  ) {
    DroidNavTab.values().forEach { tab ->
      val isSelected = tab == currentTab
      NavigationBarItem(
        selected = isSelected,
        onClick = { onTabSelected(tab) },
        icon = {
          Icon(
            imageVector = tab.icon,
            contentDescription = "${tab.label} tab",
            modifier = Modifier.size(24.dp)
          )
        },
        label = {
          Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag(tab.tag)
      )
    }
  }
}

// =========================================================================
// MARKDOWN PARSING HELPER
// =========================================================================

sealed class MarkdownSegment {
  data class Paragraph(val text: String) : MarkdownSegment()
  data class CodeBlock(val code: String, val language: String) : MarkdownSegment()
}

fun parseMarkdownSegments(raw: String): List<MarkdownSegment> {
  val result = mutableListOf<MarkdownSegment>()
  val parts = raw.split("```")

  for (i in parts.indices) {
    val part = parts[i]
    if (i % 2 == 1) {
      // Code block
      val firstNewline = part.indexOf('\n')
      val language = if (firstNewline != -1) part.substring(0, firstNewline).trim() else ""
      val code = if (firstNewline != -1) part.substring(firstNewline + 1) else part
      result.add(MarkdownSegment.CodeBlock(code = code.trimEnd(), language = language))
    } else {
      // Text paragraph
      val trimmed = part.trim()
      if (trimmed.isNotEmpty()) {
        result.add(MarkdownSegment.Paragraph(trimmed))
      }
    }
  }

  if (result.isEmpty()) {
    result.add(MarkdownSegment.Paragraph(raw))
  }
  return result
}

// =========================================================================
// PREVIEWS (360x640, 411x891, Tablet in both Light & Dark)
// =========================================================================

private val previewSampleUiState = ChatUiState(
  messages = listOf(
    UiChatMessage(
      id = "1",
      role = MessageRole.USER,
      content = "How do I create a custom button with ripple in Jetpack Compose?",
      timestamp = System.currentTimeMillis() - 60000
    ),
    UiChatMessage(
      id = "2",
      role = MessageRole.ASSISTANT,
      content = "You can use `Surface` with `onClick` or a `Box` with `Modifier.clickable`:\n\n```kotlin\n@Composable\nfun CustomRippleButton(onClick: () -> Unit) {\n    Surface(\n        onClick = onClick,\n        shape = RoundedCornerShape(12.dp),\n        color = MaterialTheme.colorScheme.primary\n    ) {\n        Text(\"Tap Me\", modifier = Modifier.padding(16.dp))\n    }\n}\n```",
      timestamp = System.currentTimeMillis()
    )
  ),
  inputText = "",
  selectedModel = "GPT-4o",
  isLoading = false
)

@Preview(name = "Small Phone - Empty State Light", widthDp = 360, heightDp = 640)
@Composable
fun PreviewSmallPhoneEmptyLight() {
  DroidCodeTheme(darkTheme = false) {
    ChatScreenContent(
      uiState = ChatUiState(),
      onInputChanged = {},
      onSend = {},
      onModelSelected = {},
      onSuggestionClick = {},
      onCancelTask = {},
      onOpenFile = {},
      onNavigateToSettings = {}
    )
  }
}

@Preview(name = "Small Phone - Empty State Dark", widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewSmallPhoneEmptyDark() {
  DroidCodeTheme(darkTheme = true) {
    ChatScreenContent(
      uiState = ChatUiState(),
      onInputChanged = {},
      onSend = {},
      onModelSelected = {},
      onSuggestionClick = {},
      onCancelTask = {},
      onOpenFile = {},
      onNavigateToSettings = {}
    )
  }
}

@Preview(name = "Large Phone - Messages Light", widthDp = 411, heightDp = 891)
@Composable
fun PreviewLargePhoneMessagesLight() {
  DroidCodeTheme(darkTheme = false) {
    ChatScreenContent(
      uiState = previewSampleUiState,
      onInputChanged = {},
      onSend = {},
      onModelSelected = {},
      onSuggestionClick = {},
      onCancelTask = {},
      onOpenFile = {},
      onNavigateToSettings = {}
    )
  }
}

@Preview(name = "Large Phone - Messages Dark", widthDp = 411, heightDp = 891, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLargePhoneMessagesDark() {
  DroidCodeTheme(darkTheme = true) {
    ChatScreenContent(
      uiState = previewSampleUiState,
      onInputChanged = {},
      onSend = {},
      onModelSelected = {},
      onSuggestionClick = {},
      onCancelTask = {},
      onOpenFile = {},
      onNavigateToSettings = {}
    )
  }
}

@Preview(name = "Tablet - Messages Light", widthDp = 800, heightDp = 1280)
@Composable
fun PreviewTabletMessagesLight() {
  DroidCodeTheme(darkTheme = false) {
    ChatScreenContent(
      uiState = previewSampleUiState,
      onInputChanged = {},
      onSend = {},
      onModelSelected = {},
      onSuggestionClick = {},
      onCancelTask = {},
      onOpenFile = {},
      onNavigateToSettings = {}
    )
  }
}

@Preview(name = "Tablet - Messages Dark", widthDp = 800, heightDp = 1280, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewTabletMessagesDark() {
  DroidCodeTheme(darkTheme = true) {
    ChatScreenContent(
      uiState = previewSampleUiState,
      onInputChanged = {},
      onSend = {},
      onModelSelected = {},
      onSuggestionClick = {},
      onCancelTask = {},
      onOpenFile = {},
      onNavigateToSettings = {}
    )
  }
}
