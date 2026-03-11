package com.kudo.app.core.platform

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class KudoHaptics(context: Context) {

    private val vibrator: Vibrator? = when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        }

        else -> context.getSystemService(Vibrator::class.java)
    }

    fun vibrate(durationMs: Long) {
        val target = vibrator ?: return
        if (!target.hasVibrator()) return

        target.vibrate(
            VibrationEffect.createOneShot(
                durationMs,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    fun vibrateDoubleTick() {
        val target = vibrator ?: return
        if (!target.hasVibrator()) return

        target.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 24, 52, 10),
                intArrayOf(0, 255, 0, 90),
                -1
            )
        )
    }
}
