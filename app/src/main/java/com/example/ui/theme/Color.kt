package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Orange / Terracotta Accents (Claude aesthetic)
val DroidOrange = Color(0xFFD95B26)          // Warm Claude terracotta / orange
val DroidOrangeDark = Color(0xFFBF360C)      // Deep burnt terracotta
val DroidOrangeLight = Color(0xFFFFE0B2)     // Soft light tint for indicator
val DroidOrangeContainer = Color(0xFFFFDBCF) // Container tint
val DroidOrangeDisabled = Color(0xFFC4C2BC)  // Muted gray for disabled state
val DroidOrangeSubtle = Color(0xFFFFF0E6)    // Very soft warm terracotta background

// Light Theme Neutrals (Crisp clean white & warm paper aesthetic)
val LightBackground = Color(0xFFFFFFFF)      // Pure crisp white
val LightSurface = Color(0xFFFFFFFF)         // Pure white surface
val LightSurfaceVariant = Color(0xFFF7F6F3)  // Subtle off-white for cards, chips & inputs
val LightOnBackground = Color(0xFF1E1D1B)    // Charcoal black primary text
val LightOnSurface = Color(0xFF1E1D1B)
val LightOnSurfaceVariant = Color(0xFF6F6D66) // Refined muted secondary text
val LightOutline = Color(0xFFE5E2DA)         // Subtle clean border
val LightOutlineVariant = Color(0xFFECEAE4)  // Very soft divider line
val LightUserBubble = Color(0xFFFFF0E6)      // Warm soft user message bubble
val LightUserBubbleBorder = Color(0xFFFFD1B3)

// Dark Theme Neutrals (Charcoal fallback if requested)
val DarkBackground = Color(0xFF141311)
val DarkSurface = Color(0xFF1F1E1B)
val DarkSurfaceVariant = Color(0xFF2B2925)
val DarkOnBackground = Color(0xFFEDEAE4)
val DarkOnSurface = Color(0xFFEDEAE4)
val DarkOnSurfaceVariant = Color(0xFFA5A29A)
val DarkOutline = Color(0xFF423F3A)
val DarkOutlineVariant = Color(0xFF33312C)
val DarkUserBubble = Color(0xFF3B2618)
val DarkUserBubbleBorder = Color(0xFF6B4025)

// Status / Feedback
val ColorSuccess = Color(0xFF2E7D32)
val ColorSuccessBg = Color(0xFFE8F5E9)
val ColorWarning = Color(0xFFED6C02)
val ColorWarningBg = Color(0xFFFFF3E0)
val ColorError = Color(0xFFC62828)
val ColorErrorBg = Color(0xFFFFEBEE)

// Code Syntax Colors
val SyntaxKeyword = Color(0xFFA83232)
val SyntaxFunction = Color(0xFF2B5B84)
val SyntaxString = Color(0xFF38761D)
val SyntaxComment = Color(0xFF8C887B)
val SyntaxNumber = Color(0xFFB45F06)
val SyntaxType = Color(0xFF674EA7)

// Backward compatibility tokens for workspace and settings screens
val ClaudeWarmBg = LightBackground
val ClaudePureWhite = LightSurface
val ClaudeSurfaceSubtle = LightSurfaceVariant
val ClaudeBorder = LightOutline
val ClaudeBorderLight = LightOutlineVariant
val ClaudeTextPrimary = LightOnBackground
val ClaudeTextSecondary = LightOnSurfaceVariant
val ClaudeTextMuted = Color(0xFF9E9C94)
val ClaudeTerracotta = DroidOrange
val ClaudeTerracottaDark = DroidOrangeDark
val ClaudeTerracottaLight = DroidOrangeSubtle
val ClaudeTerracottaContainer = DroidOrangeContainer
val ClaudeSuccess = ColorSuccess
val ClaudeSuccessBg = ColorSuccessBg
val ClaudeWarning = ColorWarning
val ClaudeWarningBg = ColorWarningBg
val ClaudeError = ColorError
val ClaudeErrorBg = ColorErrorBg
val ClaudeTerminalBg = Color(0xFF181715)
val ClaudeTerminalSurface = Color(0xFF24221F)
val ClaudeTerminalText = Color(0xFFEDEAE4)
