package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = SlateDark,
    secondary = TealSecondary,
    onSecondary = SlateDark,
    tertiary = GoldAccent,
    background = SlateDark,
    onBackground = WhiteText,
    surface = SlateSurface,
    onSurface = WhiteText,
    error = RedAlert,
    onError = WhiteText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme for premium futuristic feeling
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the brand Slate/Cyan aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
