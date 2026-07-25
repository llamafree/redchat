package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val RedChatDarkColorScheme = darkColorScheme(
    primary = ProSecondaryAccent,
    onPrimary = ProOnPrimaryContainer,
    primaryContainer = ProPrimaryActive,
    onPrimaryContainer = ProPrimaryContainer,
    secondary = ProSecondaryAccent,
    onSecondary = ProOnPrimaryContainer,
    tertiary = ProPrimaryContainer,
    background = ProBackground,
    onBackground = ProOnBackground,
    surface = ProSurface,
    onSurface = ProOnBackground,
    surfaceVariant = ProSurfaceVariant,
    onSurfaceVariant = ProOnSurfaceVariant,
    outline = ProCardBorder
)

private val RedChatLightColorScheme = darkColorScheme(
    primary = ProSecondaryAccent,
    onPrimary = ProOnPrimaryContainer,
    primaryContainer = ProPrimaryActive,
    onPrimaryContainer = ProPrimaryContainer,
    secondary = ProSecondaryAccent,
    background = ProBackground,
    onBackground = ProOnBackground,
    surface = ProSurface,
    onSurface = ProOnBackground,
    surfaceVariant = ProSurfaceVariant,
    onSurfaceVariant = ProOnSurfaceVariant,
    outline = ProCardBorder
)

@Composable
fun REDChatTheme(
    themeSelection: String = "DARK", // DARK, LIGHT, SYSTEM
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSelection) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> RedChatDarkColorScheme
        else -> RedChatLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
