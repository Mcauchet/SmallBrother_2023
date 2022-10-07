package com.projet.sluca.smallbrother

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun message(context: Context, msg: String, vibreur: Vibration) {
    val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
    toast.show()
    vibreur.vibration(context, 330)
}

fun wakeup(window: Window, activity: AppCompatActivity) {
    @Suppress("DEPRECATION")
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
        val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(activity, null)
        activity.setShowWhenLocked(true)
        activity.setTurnScreenOn(true)
        window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            (WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        )
    }
}

//TODO Test this (with ux rework, might be better)
/*fun precedent(context: Context, vibreur: Vibration, activity: AppCompatActivity) {
    Log.d("PRECEDENT UTILS", "It works")
    vibreur.vibration(context, 100)
    activity.finish()
}*/