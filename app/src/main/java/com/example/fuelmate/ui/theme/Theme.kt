package com.example.fuelmate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Brand palette — a calm "fuel / nature" green with warm amber accents.
// ---------------------------------------------------------------------------
private val BrandGreen = Color(0xFF2E7D5B)
private val BrandGreenDark = Color(0xFF1B4D3A)
private val BrandGreenContainer = Color(0xFFB8E6CF)
private val BrandGreenContainerDark = Color(0xFF2C5E49)
private val Amber = Color(0xFFE8A33D)
private val AmberDark = Color(0xFFC9872A)

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreenContainer,
    onPrimaryContainer = BrandGreenDark,
    secondary = Amber,
    onSecondary = Color(0xFF3A2A08),
    secondaryContainer = Color(0xFFFBE3C2),
    onSecondaryContainer = Color(0xFF5A4108),
    tertiary = Color(0xFF3F6FB5),
    onTertiary = Color.White,
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF1A1F1D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1F1D),
    surfaceVariant = Color(0xFFE3EAE7),
    onSurfaceVariant = Color(0xFF4E5854),
    error = Color(0xFFB3261E),
    onError = Color.White,
    outline = Color(0xFFC7D0CC)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7FD3A8),
    onPrimary = BrandGreenDark,
    primaryContainer = BrandGreenContainerDark,
    onPrimaryContainer = BrandGreenContainer,
    secondary = AmberDark,
    onSecondary = Color(0xFF2A1E05),
    secondaryContainer = Color(0xFF5A4108),
    onSecondaryContainer = Color(0xFFFBE3C2),
    tertiary = Color(0xFF9DBDF0),
    onTertiary = Color(0xFF0E1F3A),
    background = Color(0xFF121614),
    onBackground = Color(0xFFE2E8E5),
    surface = Color(0xFF1A1F1D),
    onSurface = Color(0xFFE2E8E5),
    surfaceVariant = Color(0xFF2A322F),
    onSurfaceVariant = Color(0xFFB6C2BD),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    outline = Color(0xFF3C4642)
)

// Display font for headings uses the system default; the custom typography
// weights below give the app its distinct look without bundling font files.
private val DisplayFont = FontFamily.Default

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

@Composable
fun FuelMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (API 31+).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
