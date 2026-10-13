package com.leadrescue.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald900,
    secondary = Gold500,
    onSecondary = Ink,
    secondaryContainer = Gold100,
    onSecondaryContainer = Color(0xFF5A3D00),
    tertiary = StatusNew,
    background = Mist,
    onBackground = Ink,
    surface = Mist,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE7ECE9),
    onSurfaceVariant = Slate600,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFCFB),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF0F3F1),
    surfaceContainerHighest = Color(0xFFE7ECE9),
    outline = Color(0xFFC9D2CE),
    outlineVariant = Color(0xFFE2E8E5),
    error = Coral,
)

private val DarkColors = darkColorScheme(
    primary = Emerald500,
    onPrimary = Color(0xFF00241C),
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald200,
    secondary = Gold500,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF4A3608),
    onSecondaryContainer = Gold100,
    tertiary = StatusNew,
    background = Color(0xFF0B1311),
    onBackground = Color(0xFFE6EEEB),
    surface = Color(0xFF0B1311),
    onSurface = Color(0xFFE6EEEB),
    surfaceVariant = Color(0xFF1D2926),
    onSurfaceVariant = Color(0xFFA9B6B2),
    surfaceContainerLowest = Color(0xFF070D0B),
    surfaceContainerLow = Color(0xFF111A18),
    surfaceContainer = Color(0xFF141F1C),
    surfaceContainerHigh = Color(0xFF1A2623),
    surfaceContainerHighest = Color(0xFF22302C),
    outline = Color(0xFF3A4844),
    outlineVariant = Color(0xFF26332F),
    error = Color(0xFFFF6B6F),
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun LeadRescueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
