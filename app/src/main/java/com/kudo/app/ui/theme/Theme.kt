package com.kudo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.kudo.app.core.repository.KudoStateRepository
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val LightColorScheme = lightColorScheme(
    primary = LightGreen,
    secondary = LightOrange,
    tertiary = LightGold,
    background = LightBackground,
    surface = LightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightTextMain,
    onSurface = LightTextMain
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen,
    secondary = DarkOrange,
    tertiary = DarkGold,
    background = DarkBackground,
    surface = DarkCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkTextMain,
    onSurface = DarkTextMain
)

@Composable
fun KudoTheme(
    themeMode: String = KudoStateRepository.THEME_SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        KudoStateRepository.THEME_LIGHT -> false
        KudoStateRepository.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = colorScheme.background,
            darkIcons = !darkTheme
        )
        systemUiController.setNavigationBarColor(
            color = colorScheme.background,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
