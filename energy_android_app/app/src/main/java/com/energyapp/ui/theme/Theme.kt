package com.energyapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Theme mode enum for user preference
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

// =============== STUNNING MODERN LIGHT THEME ===============
private val LightColorScheme = lightColorScheme(
    primary = GradientPurple,
    onPrimary = OnPrimary,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = OnSurface,
    secondary = GradientCyan,
    onSecondary = OnSecondary,
    secondaryContainer = CardHighlight,
    onSecondaryContainer = OnSurface,
    tertiary = GradientPink,
    onTertiary = OnPrimary,
    background = LightBackground,
    onBackground = OnBackground,
    surface = LightSurface,
    onSurface = OnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = OnPrimary,
    outline = CardBorder,
    surfaceTint = GradientPurple
)

// =============== STUNNING MODERN DARK THEME ===============
private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkTextPrimary,
    secondary = NeonCyan,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = NeonPink,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    error = Error,
    onError = OnPrimary,
    outline = DarkSurfaceVariant,
    surfaceTint = NeonPurple
)

@Composable
fun EnergyAppTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT, // Default to LIGHT for outdoor visibility
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use gradient purple for status bar
            window.statusBarColor = GradientPurple.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
