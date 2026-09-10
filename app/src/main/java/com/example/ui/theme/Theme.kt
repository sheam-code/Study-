package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChallengeDarkColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF201823),
    primaryContainer = Color(0xFF3D2E14),
    onPrimaryContainer = AmberGold,
    secondary = SageGreen,
    onSecondary = Color(0xFF132A1C),
    secondaryContainer = Color(0xFF22422F),
    onSecondaryContainer = SageGreen,
    tertiary = SteelBlue,
    onTertiary = Color(0xFF0F2642),
    background = DarkBgTop,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkInkQuiet,
    outline = DarkCardBorder
)

private val ChallengeLightColorScheme = lightColorScheme(
    primary = Color(0xFF9E6E16),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEE8B7),
    onPrimaryContainer = Color(0xFF422C00),
    secondary = Color(0xFF3F7750),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2EED8),
    onSecondaryContainer = Color(0xFF0D3219),
    tertiary = Color(0xFF336094),
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightInkQuiet,
    outline = LightCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ChallengeDarkColorScheme else ChallengeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
