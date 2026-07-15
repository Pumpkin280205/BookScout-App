package com.anjor.bookscout.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF),
    onPrimaryContainer = Color(0xFF1A1A5E),
    inversePrimary = BrandIndigo,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2F0EE),
    onSecondaryContainer = Color(0xFF003735),
    background = BrandBackground,
    onBackground = Color(0xFF1A1A26),
    surface = BrandSurface,
    onSurface = Color(0xFF1A1A26),
    surfaceVariant = Color(0xFFE3E1F2),
    onSurfaceVariant = Color(0xFF48475A),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = BrandIndigoDark,
    onPrimary = Color(0xFF1A1A5E),
    primaryContainer = Color(0xFF3A35A0),
    onPrimaryContainer = Color(0xFFE0E0FF),
    inversePrimary = BrandIndigoDark,
    secondary = BrandSecondary,
    onSecondary = Color(0xFF003735),
    secondaryContainer = Color(0xFF004F4D),
    onSecondaryContainer = Color(0xFFB2F0EE),
    background = BrandBackgroundDark,
    onBackground = BrandOnSurfaceDark,
    surface = BrandSurfaceDark,
    onSurface = BrandOnSurfaceDark,
    surfaceVariant = Color(0xFF2C2C3C),
    onSurfaceVariant = Color(0xFFC7C5DC),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

/**
 * App theme. [darkTheme] is supplied by the activity based on the user's theme preference
 * (System / Light / Dark). Dynamic (Material You) color is intentionally disabled to keep
 * the brand identity consistent.
 */
@Composable
fun BookScoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = BookScoutTypography,
        content = content
    )
}
