package com.example.ui.navigation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ChatBottomNavigationBar
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DroidNavTab
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  val storageInitialized by viewModel.storageInitialized.collectAsState()
  var showStorageDialog by remember { mutableStateOf(false) }
  var currentTab by remember { mutableStateOf(DroidNavTab.CHAT) }

  val isCodeMode by viewModel.isCodeMode.collectAsState()
  val conversations by viewModel.conversations.collectAsState()
  val activeConversationId by viewModel.activeConversationId.collectAsState()
  val generalPrefs by viewModel.generalPreferences.collectAsState()

  val isDarkTheme = when (generalPrefs.theme) {
    "DARK" -> true
    "LIGHT" -> false
    else -> isSystemInDarkTheme()
  }

  val storagePermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    showStorageDialog = false
    viewModel.initializeClaudeStorage()
    Toast.makeText(context, "Claude code workspace created in storage", Toast.LENGTH_SHORT).show()
  }

  // First time launch: prompt storage permission and create Claude code directories
  LaunchedEffect(storageInitialized) {
    if (!storageInitialized) {
      showStorageDialog = true
    } else {
      viewModel.initializeClaudeStorage()
    }
  }

  // First-time storage permission dialog
  if (showStorageDialog) {
    AlertDialog(
      onDismissRequest = {
        showStorageDialog = false
        viewModel.initializeClaudeStorage()
      },
      title = {
        Text(
          text = "Storage Permission Required",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
      },
      text = {
        Text(
          text = "Claude Code will create a 'Claude code' folder in your internal and external storage to systematically save your project files, exports, backups, and logs.\n\nAllow storage permissions to initialize your mobile workspace?",
          style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
              storagePermissionLauncher.launch(
                arrayOf(
                  Manifest.permission.READ_EXTERNAL_STORAGE,
                  Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
              )
            } else {
              showStorageDialog = false
              viewModel.initializeClaudeStorage()
              Toast.makeText(context, "Claude code workspace created in storage", Toast.LENGTH_SHORT).show()
            }
          },
          modifier = Modifier.testTag("storage_permission_grant_btn")
        ) {
          Text(
            text = "Grant & Create Folder",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showStorageDialog = false
            viewModel.initializeClaudeStorage()
            Toast.makeText(context, "Created Claude code in app storage", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.testTag("storage_permission_skip_btn")
        ) {
          Text(
            text = "App Storage Only",
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      containerColor = MaterialTheme.colorScheme.surface,
      modifier = Modifier.testTag("first_time_storage_dialog")
    )
  }

  // BackHandler to handle drawer closing, Code mode exiting, and Settings back navigation
  BackHandler(enabled = drawerState.isOpen || currentTab != DroidNavTab.CHAT || isCodeMode) {
    when {
      drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
      currentTab != DroidNavTab.CHAT -> currentTab = DroidNavTab.CHAT
      isCodeMode -> viewModel.setCodeMode(false)
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    scrimColor = Color.Black.copy(alpha = 0.45f),
    drawerContent = {
      ClaudeNavigationDrawer(
        isDark = isDarkTheme,
        isCodeMode = isCodeMode,
        onModeToggle = { isCode ->
          viewModel.setCodeMode(isCode)
          currentTab = DroidNavTab.CHAT
          coroutineScope.launch { drawerState.close() }
        },
        onCloseDrawer = {
          coroutineScope.launch { drawerState.close() }
        },
        onNewChat = {
          viewModel.startNewConversation()
          currentTab = DroidNavTab.CHAT
          coroutineScope.launch { drawerState.close() }
        },
        currentConversationId = activeConversationId,
        conversations = conversations,
        onSelectConversation = { session ->
          viewModel.selectConversation(session)
          currentTab = DroidNavTab.CHAT
          coroutineScope.launch { drawerState.close() }
        },
        onToggleStar = { convId ->
          viewModel.toggleStarConversation(convId)
        },
        onDeleteConversation = { convId ->
          viewModel.deleteConversation(convId)
        },
        onNavigateToSettings = {
          currentTab = DroidNavTab.SETTINGS
          coroutineScope.launch { drawerState.close() }
        },
        onNavigateToProjects = {
          viewModel.setCodeMode(true)
          currentTab = DroidNavTab.CHAT
          coroutineScope.launch { drawerState.close() }
        },
        onNavigateToAskAi = {
          viewModel.setCodeMode(false)
          currentTab = DroidNavTab.CHAT
          coroutineScope.launch { drawerState.close() }
        },
        onNavigateToArtifacts = {
          viewModel.setCodeMode(true)
          currentTab = DroidNavTab.CHAT
          coroutineScope.launch { drawerState.close() }
        },
        onToggleTheme = {
          val nextTheme = if (isDarkTheme) "LIGHT" else "DARK"
          viewModel.saveGeneralPreferences(generalPrefs.copy(theme = nextTheme))
        },
        onClearHistory = {
          viewModel.clearChat()
          Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
        }
      )
    }
  ) {
    when (currentTab) {
      DroidNavTab.CHAT -> {
        if (isCodeMode) {
          WorkspaceScreen(viewModel = viewModel)
        } else {
          ChatScreen(
            viewModel = viewModel,
            onNavigateToWorkspace = { viewModel.setCodeMode(true) },
            onNavigateToSettings = { currentTab = DroidNavTab.SETTINGS },
            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
            onTabSelected = { currentTab = it }
          )
        }
      }

      DroidNavTab.SETTINGS -> {
        Scaffold(
          containerColor = MaterialTheme.colorScheme.background,
          bottomBar = {
            ChatBottomNavigationBar(
              currentTab = DroidNavTab.SETTINGS,
              onTabSelected = { currentTab = it }
            )
          }
        ) { padding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(padding)
              .background(MaterialTheme.colorScheme.background)
          ) {
            SettingsScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}
