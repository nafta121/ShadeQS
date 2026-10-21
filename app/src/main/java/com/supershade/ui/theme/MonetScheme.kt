package com.supershade.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.supershade.settings.AccentColor

/**
 * Overlays the wallpaper-derived (Monet / Material You) palette onto an OS-specific [base]
 * scheme so One UI, Pixel, Nothing and Cyberpunk themes follow the wallpaper while keeping
 * their own structure. Accent roles are always replaced; neutral surfaces are blended by
 * [surfaceBlend] (0 keeps the OS surfaces untouched, 1 uses full dynamic surfaces).
 */
@Composable
fun monetScheme(
    base: ColorScheme,
    isDark: Boolean,
    surfaceBlend: Float,
    accentStrength: Float = 1.0f,
): ColorScheme {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return base
    val clampedStrength = accentStrength.coerceIn(0f, 1f)
    if (clampedStrength <= 0f) return base

    val context = LocalContext.current
    val dyn = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    val effectiveSurfaceBlend = (surfaceBlend * clampedStrength).coerceIn(0f, 1f)

    fun a(own: Color, dynamic: Color) = if (clampedStrength >= 1f) dynamic else lerp(own, dynamic, clampedStrength)
    fun s(own: Color, dynamic: Color) = if (effectiveSurfaceBlend <= 0f) own else lerp(own, dynamic, effectiveSurfaceBlend)

    return base.copy(
        primary = a(base.primary, dyn.primary),
        onPrimary = a(base.onPrimary, dyn.onPrimary),
        primaryContainer = a(base.primaryContainer, dyn.primaryContainer),
        onPrimaryContainer = a(base.onPrimaryContainer, dyn.onPrimaryContainer),
        secondary = a(base.secondary, dyn.secondary),
        onSecondary = a(base.onSecondary, dyn.onSecondary),
        secondaryContainer = a(base.secondaryContainer, dyn.secondaryContainer),
        onSecondaryContainer = a(base.onSecondaryContainer, dyn.onSecondaryContainer),
        tertiary = a(base.tertiary, dyn.tertiary),
        onTertiary = a(base.onTertiary, dyn.onTertiary),
        tertiaryContainer = a(base.tertiaryContainer, dyn.tertiaryContainer),
        onTertiaryContainer = a(base.onTertiaryContainer, dyn.onTertiaryContainer),
        surfaceTint = a(base.primary, dyn.primary),
        surfaceContainer = s(base.surfaceContainer, dyn.surfaceContainer),
        surfaceContainerHigh = s(base.surfaceContainerHigh, dyn.surfaceContainerHigh),
        surfaceContainerHighest = s(base.surfaceContainerHighest, dyn.surfaceContainerHighest),
        surfaceContainerLow = s(base.surfaceContainerLow, dyn.surfaceContainerLow),
        surfaceVariant = s(base.surfaceVariant, dyn.surfaceVariant),
        onSurfaceVariant = s(base.onSurfaceVariant, dyn.onSurfaceVariant),
        outlineVariant = s(base.outlineVariant, dyn.outlineVariant),
    )
}

/**
 * Applies a user-selected [AccentColor] with guaranteed WCAG-readable [ColorScheme.onPrimary]
 * and [ColorScheme.onPrimaryContainer] contrast in both dark and light modes.
 */
fun ColorScheme.withCustomAccent(accentColor: AccentColor, isDark: Boolean): ColorScheme {
    if (accentColor == AccentColor.MONET || accentColor.hex == 0L) return this
    val c = Color(accentColor.hex)
    val onPrimaryColor = if (c.luminance() > 0.45f) Color.Black else Color.White
    val containerAlpha = if (isDark) 0.35f else 0.20f
    val containerBg = lerp(surface, c, containerAlpha)
    val onContainerColor = if (containerBg.luminance() > 0.45f) Color(0xFF10141A) else Color.White
    return copy(
        primary = c,
        onPrimary = onPrimaryColor,
        primaryContainer = c.copy(alpha = containerAlpha),
        onPrimaryContainer = onContainerColor,
        surfaceTint = c,
    )
}

@Composable
fun resolveShadeColorScheme(
    darkThemeMode: DarkThemeMode,
    isAmoled: Boolean,
    accentColor: AccentColor,
    surfaceBlend: Float,
    monetBlendStrength: Float,
    darkColors: ColorScheme,
    lightColors: ColorScheme,
    amoledColors: ColorScheme,
): ColorScheme {
    val isDark = darkThemeMode.isDark(isSystemInDarkTheme())
    val useAmoled = isAmoled || darkThemeMode == DarkThemeMode.AMOLED
    val base = when {
        useAmoled -> amoledColors
        isDark -> darkColors
        else -> lightColors
    }
    val effectiveSurfaceBlend = if (useAmoled) 0f else surfaceBlend
    val monetApplied = monetScheme(base, isDark, effectiveSurfaceBlend, monetBlendStrength)
    return if (accentColor != AccentColor.MONET) {
        base.copy(
            surfaceContainer = monetApplied.surfaceContainer,
            surfaceContainerHigh = monetApplied.surfaceContainerHigh,
            surfaceContainerHighest = monetApplied.surfaceContainerHighest,
            surfaceContainerLow = monetApplied.surfaceContainerLow,
            surfaceVariant = monetApplied.surfaceVariant,
            onSurfaceVariant = monetApplied.onSurfaceVariant,
            outlineVariant = monetApplied.outlineVariant,
        ).withCustomAccent(accentColor, isDark)
    } else {
        monetApplied
    }
}

@Composable
fun SuperShadeTheme(
    theme: ShadeTheme,
    darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.GALAXY_BLUE,
    monetBlendStrength: Float = 1.0f,
    content: @Composable () -> Unit,
) {
    val isAmoled = darkThemeMode == DarkThemeMode.AMOLED
    CompositionLocalProvider(LocalShadeTheme provides theme) {
        when (theme) {
            ShadeTheme.Pixel -> PixelShadeTheme(isAmoled, darkThemeMode, accentColor, monetBlendStrength, content)
            ShadeTheme.PureMaterial -> PureMaterialShadeTheme(isAmoled, darkThemeMode, accentColor, monetBlendStrength, content)
            ShadeTheme.Nothing -> NothingShadeTheme(isAmoled, darkThemeMode, accentColor, monetBlendStrength, content)
            ShadeTheme.Cyberpunk -> CyberpunkShadeTheme(isAmoled, darkThemeMode, accentColor, monetBlendStrength, content)
            ShadeTheme.OneUI -> OneUiShadeTheme(isAmoled, darkThemeMode, accentColor, monetBlendStrength, content)
        }
    }
}
