package com.projet.sluca.smallbrother

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

//Edit URL Server until it is redefined in deployment
const val URLServer = "https://2fd7-2a02-a03f-ae4e-1900-e1c1-1a0b-5bd9-140c.eu.ngrok.io"

/***
 * Sends an SMS
 *
 * @param [context] context of the activity
 * @param [msg] body of the SMS
 * @param [receiver] receiver of the SMS
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 27-12-2022)
 */
fun sendSMS(context: Context, msg: String, receiver: String) {
    if (!smsAvailable(context)) {
        Toast.makeText(context,
            "Veuillez retirer le mode avion pour envoyer un SMS.",
            Toast.LENGTH_LONG
        ).show()
        return
    }
    val subscriptionId: Int = SmsManager.getDefaultSmsSubscriptionId()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
            .sendTextMessage(receiver, null, msg, sentPI(context), null)
    } else {
        @Suppress("DEPRECATION")
        SmsManager
            .getDefault()
            .sendTextMessage(receiver, null, msg, sentPI(context), null)
    }
}

/**
 * Checks if the app is in airplane mode to see if sms is available
 *
 * @param [context] the context of the activity
 * @return true if airplane mode off, false otherwise
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 26-12-22)
 */
fun smsAvailable(context: Context): Boolean {
    return Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.AIRPLANE_MODE_ON, 0
    ) == 0
}

/***
 * returns the PendingIntent for the SMS
 *
 * @param [context] the context of the activity
 * @return the PendingIntent for the SMS
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 28-10-2022)
 */
fun sentPI(context: Context): PendingIntent = PendingIntent.getBroadcast(
    context,
    0,
    Intent("SMS_SENT"),
    0)

/***
 * creates a toast with a msg to print and vibrate
 *
 * @param [context] the context of the activity
 * @param [msg] the message to print
 * @param [vibreur] the phone Vibration system
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 26-12-22)
 */
fun message(context: Context, msg: String, vibreur: Vibration) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    vibreur.vibration(context, 300)
}

/***
 * Launches the application and put it on screen even if device locked
 *
 * @param [window] the window of the application
 * @param [activity] the activity to put on screen
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on ??-10-22)
 */
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

/***
 * isOnline returns true if device has validated network capabilities (Cellular, Wifi or Ethernet)
 *
 * @return true if connected, false otherwise
 * @author Maxime Caucheteur (inspired by https://medium.com/@veniamin.vynohradov/monitoring-internet-connection-state-in-android-da7ad915b5e5)
 * @version 1.2 (Updated on 02-01-23)
 */
fun isOnline(context: Context): Boolean {
    try {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            /*if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "TRANSPORT_ETHERNET")
                return true
            }*/
            return when {
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                -> true
                else -> false
            }
        }
    } catch (e:IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    return false
}

/***
 * Animation of loading
 *
 * @param [tvLoading] the TextView in which the animation takes place
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 15-12-2022)
 */
fun loading(tvLoading: TextView) {
    object : CountDownTimer(2000, 1) {
        override fun onTick(millisUntilFinished: Long) {
            when (millisUntilFinished) {
                in 1501..2000 -> tvLoading.text = ""
                in 1001..1500 -> tvLoading.text = "."
                in 501..1000 -> tvLoading.text = ".."
                in 0..500 -> tvLoading.text = "..."
            }
        }
        override fun onFinish(): Unit = loading(tvLoading)
    }.start()
}

/**
 * Returns the particule before the name of the user
 * @param [name] the name of the user
 * @return the particule needed before the name
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (java version))
 * @version 1.2 (Updated on 27-12-22)
 */
fun particule(name: String) : String {
    val particule = name[0].toString()
    val voyelles = arrayOf(
        "A",
        "E",
        "Y",
        "U",
        "I",
        "O",
        "É",
        "È",
        "Œ",
        "a",
        "e",
        "y",
        "u",
        "i",
        "o",
        "é",
        "è"
    )

    return if (listOf(*voyelles).contains(particule)) "d'" else "de "
}
