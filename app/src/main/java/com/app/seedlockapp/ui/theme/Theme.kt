package com.app.seedlockapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    background    = Gray1A,
    onBackground  = WhiteDE,
    primary       = Blue54A9FF,
    onPrimary     = Black,
    surface       = Gray2C,
    onSurface     = WhiteDE,
    secondary     = Gray2C,
    onSecondary   = WhiteDE,
    tertiary      = Teal5EEAD4,
    onTertiary    = OnTealBlack,
)

private val LightColorScheme = lightColorScheme(
    background    = White,
    onBackground  = Black,
    primary       = Blue007AFF,
    onPrimary     = White,
    surface       = White,
    onSurface     = Black,
    secondary     = GrayF3,
    onSecondary   = Black,
    tertiary      = Green34D399,
    onTertiary    = OnGreenWhite,
    )

@Composable
fun SeedLockAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        typography = Typography,
        content = content
    )
}