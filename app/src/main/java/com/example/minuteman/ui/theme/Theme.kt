package com.example.minuteman.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkGreen = Color(0xFF0F300F)
val NeonGreen = Color(0xFF00FF33)
val ConsoleBody = Color(0xFFC4CFA1)
val DpadDark = Color(0xFF303030)
val ActionWine = Color(0xFF8B1D40)

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = ActionWine,
    background = Color(0xFF16191B),
    surface = Color(0xFF22262B),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MinutemanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
