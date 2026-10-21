package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.MainViewModel

/**
 * HomeScreen acts as the Chat screen destination.
 * Redesigned in ChatScreen.kt according to Material 3 specifications.
 */
@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onNavigateToWorkspace: () -> Unit = {},
  onNavigateToSettings: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  ChatScreen(
    viewModel = viewModel,
    onNavigateToWorkspace = onNavigateToWorkspace,
    onNavigateToSettings = onNavigateToSettings,
    modifier = modifier
  )
}
