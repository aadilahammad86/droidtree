package com.droidtree.app.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun buildLightScheme(palette: AppColorPalette): ColorScheme {
    return lightColorScheme(
        primary = palette.primary,
        onPrimary = Color.White,
        primaryContainer = palette.primary.copy(alpha = 0.14f),
        onPrimaryContainer = palette.tertiary,
        secondary = palette.secondary,
        onSecondary = Color.White,
        secondaryContainer = palette.secondary.copy(alpha = 0.14f),
        onSecondaryContainer = palette.secondary,
        tertiary = palette.tertiary,
        onTertiary = Color.White,
        background = Color(0xFFF8FAFC),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFEDF2F7),
        onBackground = Color(0xFF0F172A),
        onSurface = Color(0xFF0F172A),
        onSurfaceVariant = Color(0xFF64748B),
        outline = Color(0xFFE2E8F0),
        error = DroidError
    )
}

private fun buildDarkScheme(palette: AppColorPalette): ColorScheme {
    return darkColorScheme(
        primary = palette.primary,
        onPrimary = Color.White,
        primaryContainer = palette.primary.copy(alpha = 0.22f),
        onPrimaryContainer = palette.secondary,
        secondary = palette.secondary,
        onSecondary = Color.White,
        secondaryContainer = palette.secondary.copy(alpha = 0.20f),
        onSecondaryContainer = palette.secondary,
        tertiary = palette.tertiary,
        onTertiary = Color.White,
        background = DroidDarkBackground,
        surface = DroidDarkSurface,
        surfaceVariant = DroidDarkSurfaceVariant,
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF8FAFC),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF334155),
        error = DroidError
    )
}

private fun buildOledScheme(palette: AppColorPalette): ColorScheme {
    return darkColorScheme(
        primary = palette.primary,
        onPrimary = Color.White,
        primaryContainer = palette.primary.copy(alpha = 0.25f),
        onPrimaryContainer = palette.secondary,
        secondary = palette.secondary,
        onSecondary = Color.White,
        secondaryContainer = palette.secondary.copy(alpha = 0.22f),
        onSecondaryContainer = palette.secondary,
        tertiary = palette.tertiary,
        onTertiary = Color.White,
        background = Color(0xFF000000),
        surface = Color(0xFF080808),
        surfaceVariant = Color(0xFF141414),
        onBackground = Color(0xFFF8FAFC),
        onSurface = Color(0xFFF8FAFC),
        onSurfaceVariant = Color(0xFFA1A1AA),
        outline = Color(0xFF27272A),
        error = DroidError
    )
}

@Composable
fun DroidTreeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorPalette: AppColorPalette = AppColorPalette.MONET,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.OLED -> true
    }

    val isOled = themeMode == ThemeMode.OLED

    val colorScheme = when {
        colorPalette == AppColorPalette.MONET && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val base = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isOled) {
                base.copy(
                    background = Color(0xFF000000),
                    surface = Color(0xFF080808),
                    surfaceVariant = Color(0xFF141414)
                )
            } else {
                base
            }
        }
        isOled -> buildOledScheme(colorPalette)
        isDark -> buildDarkScheme(colorPalette)
        else -> buildLightScheme(colorPalette)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DroidTypography,
        content = content
    )
}
