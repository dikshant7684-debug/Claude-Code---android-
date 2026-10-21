package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
  primary = DroidOrange,
  onPrimary = Color.White,
  primaryContainer = DroidOrangeSubtle,
  onPrimaryContainer = DroidOrangeDark,
  secondary = DroidOrange,
  onSecondary = Color.White,
  secondaryContainer = LightSurfaceVariant,
  onSecondaryContainer = LightOnSurface,
  background = LightBackground,
  onBackground = LightOnBackground,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightOnSurfaceVariant,
  outline = LightOutline,
  outlineVariant = LightOutlineVariant,
  error = ColorError,
  onError = Color.White
)

val DarkColorScheme = darkColorScheme(
  primary = Color(0xFFFF9E66),
  onPrimary = Color(0xFF451A00),
  primaryContainer = Color(0xFF662900),
  onPrimaryContainer = Color(0xFFFFDBCF),
  secondary = Color(0xFFFF9E66),
  onSecondary = Color(0xFF451A00),
  secondaryContainer = DarkSurfaceVariant,
  onSecondaryContainer = DarkOnSurface,
  background = DarkBackground,
  onBackground = DarkOnBackground,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkOnSurfaceVariant,
  outline = DarkOutline,
  outlineVariant = DarkOutlineVariant,
  error = ColorError,
  onError = Color.White
)

/**
 * Claude Code Theme - defaults to crisp clean white theme for all options (Chat, Settings)
 */
@Composable
fun ClaudeCodeTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = DroidCodeTypography,
    content = content
  )
}

/**
 * Backward compatibility alias for DroidCodeTheme
 */
@Composable
fun DroidCodeTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) = ClaudeCodeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
