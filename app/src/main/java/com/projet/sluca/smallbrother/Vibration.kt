package com.projet.sluca.smallbrother

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * class Vibration creates a Vibrator and manages the vibrations made by all activities
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 08-01-2023)
 */
class Vibration {

    /**
     * signal an event by vibrating
     * @param [context] the context of the activity
     * @param [duree] duration in milliseconds
     * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
     * @version 1.2 (Updated on 08-01-2023)
     */
    fun vibration(context: Context, duree: Int) {
        val shake = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            shake.vibrate(VibrationEffect
                .createOneShot(duree.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            shake.vibrate(duree.toLong())
        }
    }

    /**
     * Get a Vibrator object
     * @param [context] the context of the activity
     * @return the Vibrator object
     * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
     * @version 1.2 (Updated on 08-01-2023)
     */
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }
}
