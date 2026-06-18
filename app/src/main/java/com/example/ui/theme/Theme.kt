package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LuxuryDarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    secondary = RichGold,
    tertiary = SoftGold,
    background = DeepBlack,
    surface = DarkOnyx,
    onPrimary = IvoryLight,
    onSecondary = DeepBlack,
    onTertiary = DeepBlack,
    onBackground = IvoryLight,
    onSurface = IvoryLight,
    surfaceVariant = GlassCardBackground,
    onSurfaceVariant = IvoryWhite,
    error = CrimsonWarning
)

private val LuxuryLightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    secondary = RichGold,
    tertiary = DeepBlack,
    background = IvoryWhite,
    surface = Color(0xFFFAFAF5),
    onPrimary = IvoryWhite,
    onSecondary = DeepBlack,
    onTertiary = IvoryWhite,
    onBackground = DeepBlack,
    onSurface = DeepBlack,
    surfaceVariant = Color(0xFFEFECE0),
    onSurfaceVariant = DeepBlack,
    error = CrimsonWarning
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamicColor by default to guarantee the premium Makkah/Madinah palette takes absolute stage
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LuxuryDarkColorScheme
        else -> LuxuryLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
