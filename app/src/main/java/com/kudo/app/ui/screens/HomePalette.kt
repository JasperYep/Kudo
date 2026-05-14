package com.kudo.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kudo.app.ui.theme.DarkBackground
import com.kudo.app.ui.theme.DarkBlue
import com.kudo.app.ui.theme.DarkCard
import com.kudo.app.ui.theme.DarkGold
import com.kudo.app.ui.theme.DarkGoldBg
import com.kudo.app.ui.theme.DarkGreen
import com.kudo.app.ui.theme.DarkGreenBg
import com.kudo.app.ui.theme.DarkLine
import com.kudo.app.ui.theme.DarkOrange
import com.kudo.app.ui.theme.DarkOrangeBg
import com.kudo.app.ui.theme.DarkRed
import com.kudo.app.ui.theme.DarkTextMain
import com.kudo.app.ui.theme.DarkTextSub
import com.kudo.app.ui.theme.LightBackground
import com.kudo.app.ui.theme.LightBlue
import com.kudo.app.ui.theme.LightCard
import com.kudo.app.ui.theme.LightGold
import com.kudo.app.ui.theme.LightGoldBg
import com.kudo.app.ui.theme.LightGreen
import com.kudo.app.ui.theme.LightGreenBg
import com.kudo.app.ui.theme.LightLine
import com.kudo.app.ui.theme.LightOrange
import com.kudo.app.ui.theme.LightOrangeBg
import com.kudo.app.ui.theme.LightRed
import com.kudo.app.ui.theme.LightTextMain
import com.kudo.app.ui.theme.LightTextSub

@Immutable
internal data class KudoPalette(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val line: Color,
    val textMain: Color,
    val textSub: Color,
    val green: Color,
    val greenBg: Color,
    val orange: Color,
    val orangeBg: Color,
    val gold: Color,
    val goldBg: Color,
    val blue: Color,
    val red: Color
)

internal const val HapticTickMs = 8L
internal const val HapticConfirmMs = 12L
internal const val HapticErrorMs = 24L
internal const val ReorderSwapHapticMs = 4L
internal const val ReorderSwapHapticCooldownMs = 42L
internal const val ReorderCancelAnimationStiffness = 520f
internal val ReorderAutoScrollMax = 34.dp
internal val EditControlShape = RoundedCornerShape(16.dp)
internal val EditFieldMinHeight = 54.dp
internal val EditButtonHeight = 50.dp
internal val EditChipSize = 34.dp
internal val TaskTabBalanceRowEstimate = 60.dp

internal fun staticPalette(isDark: Boolean): KudoPalette {
    return if (isDark) {
        KudoPalette(
            isDark = true,
            background = DarkBackground,
            card = DarkCard,
            line = DarkLine,
            textMain = DarkTextMain,
            textSub = DarkTextSub,
            green = DarkGreen,
            greenBg = DarkGreenBg,
            orange = DarkOrange,
            orangeBg = DarkOrangeBg,
            gold = DarkGold,
            goldBg = DarkGoldBg,
            blue = DarkBlue,
            red = DarkRed
        )
    } else {
        KudoPalette(
            isDark = false,
            background = LightBackground,
            card = LightCard,
            line = LightLine,
            textMain = LightTextMain,
            textSub = LightTextSub,
            green = LightGreen,
            greenBg = LightGreenBg,
            orange = LightOrange,
            orangeBg = LightOrangeBg,
            gold = LightGold,
            goldBg = LightGoldBg,
            blue = LightBlue,
            red = LightRed
        )
    }
}

@Composable
internal fun rememberPalette(isDark: Boolean): KudoPalette {
    val target = staticPalette(isDark)
    val animationSpec = tween<Color>(
        durationMillis = 420,
        easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    )

    val background by animateColorAsState(target.background, animationSpec, label = "paletteBackground")
    val card by animateColorAsState(target.card, animationSpec, label = "paletteCard")
    val line by animateColorAsState(target.line, animationSpec, label = "paletteLine")
    val textMain by animateColorAsState(target.textMain, animationSpec, label = "paletteTextMain")
    val textSub by animateColorAsState(target.textSub, animationSpec, label = "paletteTextSub")
    val green by animateColorAsState(target.green, animationSpec, label = "paletteGreen")
    val greenBg by animateColorAsState(target.greenBg, animationSpec, label = "paletteGreenBg")
    val orange by animateColorAsState(target.orange, animationSpec, label = "paletteOrange")
    val orangeBg by animateColorAsState(target.orangeBg, animationSpec, label = "paletteOrangeBg")
    val gold by animateColorAsState(target.gold, animationSpec, label = "paletteGold")
    val goldBg by animateColorAsState(target.goldBg, animationSpec, label = "paletteGoldBg")
    val blue by animateColorAsState(target.blue, animationSpec, label = "paletteBlue")
    val red by animateColorAsState(target.red, animationSpec, label = "paletteRed")

    return KudoPalette(
        isDark = target.isDark,
        background = background,
        card = card,
        line = line,
        textMain = textMain,
        textSub = textSub,
        green = green,
        greenBg = greenBg,
        orange = orange,
        orangeBg = orangeBg,
        gold = gold,
        goldBg = goldBg,
        blue = blue,
        red = red
    )
}
