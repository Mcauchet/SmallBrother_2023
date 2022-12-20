package com.projet.sluca.smallbrother

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import android.util.Base64
import java.security.Key
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import kotlin.text.toByteArray

//Edit URL Server until it is redefined in deployment
const val URLServer = "https://bda3-2a02-a03f-ae4e-1900-207e-be86-cbe9-c72f.eu.ngrok.io"

/***
 * Send a SMS
 *
 * @param [context] context of the activity
 * @param [msg] body of the SMS
 * @param [receiver] receiver of the SMS
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 01-12-2022)
 */
fun sendSMS(context: Context, msg: String, receiver: String) {
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
 * @version 1.2 (Updated on 15-12-22)
 */
fun message(context: Context, msg: String, vibreur: Vibration) {
    val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
    toast.show()
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
 * isOnline returns true if device has network capabilities (Cellular, Wifi or Ethernet)
 *
 * @return true if connected, false otherwise
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 24-11-22)
 */
fun isOnline(context: Context): Boolean {
    try {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "TRANSPORT_ETHERNET")
                return true
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

/***
 * Transform a String into a Key object used as a public key
 *
 * @param publicKey the String to transform into a Key
 * @return the Key object
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-12-2022)
 */
fun loadPublicKey(publicKey: String): Key {
    val data: ByteArray = Base64.decode(publicKey.toByteArray(), Base64.DEFAULT)
    val spec = X509EncodedKeySpec(data)
    val fact = KeyFactory.getInstance("RSA")
    return fact.generatePublic(spec)
}
