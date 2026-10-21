package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DroidCodeTypography = Typography(
  // Greeting: 34-40sp, serif font, tight line height
  displayLarge = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 38.sp,
    lineHeight = 42.sp,
    letterSpacing = (-0.5).sp
  ),
  displayMedium = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 34.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.25).sp
  ),
  displaySmall = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 34.sp
  ),
  headlineLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 30.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp
  ),
  headlineSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 24.sp
  ),
  // Top bar title: 20sp semibold
  titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp
  ),
  // Subtitle: 17-18sp, muted onSurfaceVariant
  titleMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.1.sp
  ),
  titleSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  // Message text: 16sp minimum, line height 24sp
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
  ),
  // Suggestion chips: 15sp
  bodyMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.15.sp
  ),
  // Model badge / secondary items: 13sp (strictly > 12sp)
  bodySmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.2.sp
  ),
  labelLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  labelMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.2.sp
  ),
  // Navigation bar labels: 12sp. Nothing smaller than 12sp!
  labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp
  )
)

val Typography = DroidCodeTypography
