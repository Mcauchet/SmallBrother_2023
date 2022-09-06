package com.projet.sluca.smallbrother

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


class Vibration {
    // --> VIBRATION() : vibration pour signifier un évènement.
    //     Prend en paramètre : le contexte de l'activité d'où elle est appelée, une durée en ms.
    fun vibration(context: Context, duree: Int) {
        /*
        val shake = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        shake.vibrate(duree.toLong())
        */
        val shake = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        shake.vibrate(VibrationEffect
            .createOneShot(duree.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
