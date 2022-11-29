package com.projet.sluca.smallbrother

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/***
 * class Vibration creates a Vibrator and manages the vibrations made by all activities
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 24-10-2022)
 */
class Vibration {

    /***
     * signal an event by vibrating
     * @param [context] the context of the activity
     * @param [duree] duration in milliseconds
     */
    fun vibration(context: Context, duree: Int) {
        val shake = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        shake.vibrate(VibrationEffect
            .createOneShot(duree.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
