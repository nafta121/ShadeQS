package com.supershade

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.supershade.domain.notification.OtpDetector
import com.supershade.domain.tile.canonicalTileId
import com.supershade.domain.tile.humanizeTileLabel
import com.supershade.domain.update.UpdateInfo
import com.supershade.settings.AccentColor
import com.supershade.settings.TileShape
import com.supershade.ui.theme.DarkThemeMode
import com.supershade.ui.theme.ShadeShapeScheme
import com.supershade.ui.theme.ShadeTheme
import com.supershade.ui.theme.withCustomAccent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainLogicTest {

    @Test
    fun otpDetector_extractsPrefixedHyphenatedAndNumericCodes_whileIgnoringYears() {
        assertEquals("G-492810", OtpDetector.extractOtp("Google", "G-492810 is your Google verification code."))
        assertEquals("123-456", OtpDetector.extractOtp("Login OTP", "Use security code 123-456 to sign in."))
        assertEquals(
            "849201",
            OtpDetector.extractOtp(
                "Copyright 2026 Bank",
                " Since 2025 we protect your account. Your verification code is 849201.",
            ),
        )
        assertNull(OtpDetector.extractOtp("Order #482910", "Your package will arrive in 2026."))
        assertTrue(OtpDetector.isVipAlert("Security Alert", "Unrecognized login attempt detected"))
    }

    @Test
    fun tileDefinition_normalizesVendorAliasesAndHumanizesCustomComponents() {
        assertEquals("wifi", canonicalTileId("com.android.systemui/.qs.tiles.InternetTile"))
        assertEquals("mute", canonicalTileId("SoundMode"))
        assertEquals("night", canonicalTileId("BlueLightFilter"))
        assertEquals("SmartThings", humanizeTileLabel("custom(com.samsung.android.oneconnect/.QcTileService)"))
        assertEquals("Dolby Atmos", humanizeTileLabel("sec_dolby_atmos"))
    }

    @Test
    fun updateInfo_comparesSemanticVersionsWithPrefixesAndSuffixes() {
        val newer = UpdateInfo("v1.10.0", "1.9.36-debug", "", null, "")
        val same = UpdateInfo("v1.9.36", "1.9.36", "", null, "")
        val older = UpdateInfo("v1.9.9", "1.9.36", "", null, "")
        assertTrue(newer.isUpdateAvailable)
        assertFalse(same.isUpdateAvailable)
        assertFalse(older.isUpdateAvailable)
    }

    @Test
    fun themeAndShapeLogic_respectsUserTileShapeAndReadableAccentContrast() {
        val squircleTile = ShadeShapeScheme.fromTileShape(TileShape.SQUIRCLE).tile
        assertEquals(squircleTile, ShadeShapeScheme.forTheme(ShadeTheme.Cyberpunk, TileShape.SQUIRCLE).tile)
        assertEquals(squircleTile, ShadeShapeScheme.forTheme(ShadeTheme.Pixel, TileShape.SQUIRCLE).tile)
        assertEquals(TileShape.PILL, ShadeShapeScheme.defaultTileShapeForTheme(ShadeTheme.Pixel))
        assertEquals(TileShape.SHARP, ShadeShapeScheme.defaultTileShapeForTheme(ShadeTheme.Cyberpunk))
        TileShape.entries.forEach { shape ->
            assertEquals(shape, TileShape.entries.find { it.id == shape.id })
        }

        val darkAmber = darkColorScheme().withCustomAccent(AccentColor.AMBER, isDark = true)
        assertEquals(Color.Black, darkAmber.onPrimary)
        assertEquals(Color.White, darkAmber.onPrimaryContainer)

        val lightGalaxy = lightColorScheme(surface = Color.White).withCustomAccent(AccentColor.GALAXY_BLUE, isDark = false)
        assertEquals(Color.White, lightGalaxy.onPrimary)
        assertEquals(Color(0xFF10141A), lightGalaxy.onPrimaryContainer)
        assertTrue(DarkThemeMode.AMOLED.isDark(systemInDark = false))
        assertFalse(DarkThemeMode.LIGHT.isDark(systemInDark = true))
    }
}
