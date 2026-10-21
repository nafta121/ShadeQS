package com.supershade.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

sealed class ShadeTheme(val displayName: String) {
    data object OneUI : ShadeTheme("One UI 8.5")
    data object Pixel : ShadeTheme("Pixel")
    data object PureMaterial : ShadeTheme("Material")
    data object Nothing : ShadeTheme("Nothing")
    data object Cyberpunk : ShadeTheme("Cyberpunk")
}

enum class DarkThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
    AMOLED;

    fun isDark(systemInDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemInDark
        DARK, AMOLED -> true
        LIGHT -> false
    }
}

val LocalShadeTheme = staticCompositionLocalOf<ShadeTheme> { ShadeTheme.OneUI }
