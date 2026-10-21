package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.ClaudeCodeTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ClaudeCodeTheme(darkTheme = false) {
        MainAppNavigation(viewModel = viewModel)
      }
    }
  }
}
