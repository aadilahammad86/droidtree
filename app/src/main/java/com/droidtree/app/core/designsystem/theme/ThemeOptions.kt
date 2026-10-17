package com.droidtree.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Supported display theme modes.
 */
enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
    OLED("OLED Pure Black")
}

/**
 * 8 distinct curated color palettes plus wallpaper-based Monet dynamic theming.
 */
enum class AppColorPalette(
    val id: String,
    val displayName: String,
    val description: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
) {
    MONET(
        id = "monet",
        displayName = "Monet Dynamic",
        description = "Wallpaper-based Material You colors (Android 12+)",
        primary = Color(0xFF6750A4),
        secondary = Color(0xFF625B71),
        tertiary = Color(0xFF7D5260)
    ),
    ARBOR_EMERALD(
        id = "arbor_emerald",
        displayName = "Arbor Emerald",
        description = "Lush botanical greens & mint",
        primary = Color(0xFF10B981),
        secondary = Color(0xFF34D399),
        tertiary = Color(0xFF059669)
    ),
    CYBER_CYAN(
        id = "cyber_cyan",
        displayName = "Cyber Cyan",
        description = "Electric neon cyan & WizTree teal",
        primary = Color(0xFF06B6D4),
        secondary = Color(0xFF22D3EE),
        tertiary = Color(0xFF0891B2)
    ),
    ELECTRIC_BLUE(
        id = "electric_blue",
        displayName = "Electric Blue",
        description = "Vibrant macOS & Apple sapphire blue",
        primary = Color(0xFF3B82F6),
        secondary = Color(0xFF60A5FA),
        tertiary = Color(0xFF1D4ED8)
    ),
    AMETHYST_VIOLET(
        id = "amethyst_violet",
        displayName = "Amethyst Violet",
        description = "Deep royal purple & cyberpunk violet",
        primary = Color(0xFF8B5CF6),
        secondary = Color(0xFFA78BFA),
        tertiary = Color(0xFF6D28D9)
    ),
    SUNSET_AMBER(
        id = "sunset_amber",
        displayName = "Sunset Amber",
        description = "Warm golden amber & solar flare",
        primary = Color(0xFFF59E0B),
        secondary = Color(0xFFFBBF24),
        tertiary = Color(0xFFD97706)
    ),
    CRIMSON_RUBY(
        id = "crimson_ruby",
        displayName = "Crimson Ruby",
        description = "Fiery red & rich crimson",
        primary = Color(0xFFEF4444),
        secondary = Color(0xFFF87171),
        tertiary = Color(0xFFB91C1C)
    ),
    SAKURA_PINK(
        id = "sakura_pink",
        displayName = "Sakura Pink",
        description = "Delicate blossom pink & rose",
        primary = Color(0xFFEC4899),
        secondary = Color(0xFFF472B6),
        tertiary = Color(0xFFBE185D)
    ),
    MONOCHROME_SLATE(
        id = "monochrome_slate",
        displayName = "Monochrome Slate",
        description = "Minimalist titanium & high-contrast slate",
        primary = Color(0xFF94A3B8),
        secondary = Color(0xFFCBD5E1),
        tertiary = Color(0xFF475569)
    )
}
