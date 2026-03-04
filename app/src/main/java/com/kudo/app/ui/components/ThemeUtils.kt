package com.kudo.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun isSystemInDarkTheme(): Boolean {
    // Simple implementation - will be replaced with actual theme detection
    return false
}

@Composable
fun Dp.coerceIn(min: Dp, max: Dp): Dp {
    return this.coerceAtLeast(min).coerceAtMost(max)
}

@Composable
fun Float.coerceIn(min: Float, max: Float): Float {
    return this.coerceAtLeast(min).coerceAtMost(max)
}
