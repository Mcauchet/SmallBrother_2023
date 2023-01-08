package com.projet.sluca.smallbrother

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.telephony.SmsManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.activities.InstallDantPicActivity
import com.projet.sluca.smallbrother.activities.QRCodeScannerInstallActivity
import com.projet.sluca.smallbrother.models.UserData
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

//Edit URL Server until it is redefined in deployment
const val URLServer = "https://16ce-2a02-a03f-ae4e-1900-25ec-d7af-e7dd-3f45.eu.ngrok.io"

/***
 * Sends an SMS
 *
 * @param [context] context of the activity
 * @param [msg] body of the SMS
 * @param [receiver] receiver of the SMS
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 27-12-2022)
 */
fun sendSMS(context: Context, msg: String, receiver: String, vibreur: Vibration) {
    if (!smsAvailable(context)) {
        message(context, "Veuillez retirer le mode avion pour envoyer un SMS.", vibreur)
        return
    }
    val subscriptionId: Int = SmsManager.getDefaultSmsSubscriptionId()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
            .sendTextMessage(receiver, null, msg, sentPI(context), null)
    } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault().sendTextMessage(receiver, null, msg, sentPI(context), null)
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
 * @version 1.2 (Updated on 04-01-2023)
 */
fun isOnline(context: Context): Boolean {
    try {
        return checkInternetCapabilities(context)
    } catch (e:IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    return false
}

/**
 * Checks Network Capabilities
 * @param [context] the context of the activity
 * @return true if has Network capabilities, false otherwise
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
private fun checkInternetCapabilities(context: Context) : Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
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
 * @version 1.2 (Updated on 04-01-2023)
 */
fun particule(name: String) : String {
    val particule = name[0].toString()
    val voyelles = arrayOf("A", "E", "Y", "U", "I", "O", "É", "È", "Œ", "a", "e", "y", "u", "i",
        "o", "é", "è"
    )
    return if (listOf(*voyelles).contains(particule)) "d'" else "de "
}

/**
 * Redirects to the adequate activity
 *
 * @author Maxime Caucheteur (with contribution of Sébastien Luca (java version))
 * @version 1.2 (Updated on 03-01-2023)
 */
fun redirectRole(context: Context, userData: UserData) {
    when (userData.role) {
        "Aidant" -> {
            val intent = Intent(context, AidantActivity::class.java)
            startActivity(context, intent, null)
        }
        "Aidé" -> {
            val intent = Intent(context, AideActivity::class.java)
            startActivity(context, intent, null)
        }
    }
}

/**
 * Checks if inputs are valid
 * @param [name] the name of the aidant
 * @param [namePartner] the name of the aidé
 * @param [telephone] the phone number of the aidé
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
fun checkInputs(name: String, namePartner: String, telephone: String, context: Context,
                        userData: UserData, vibreur: Vibration) {
    when {
        telephone.length > 10 || !telephone.matches("".toRegex()) && !telephone.startsWith("04")
        -> message(context, context.getString(R.string.error01), vibreur)

        name.matches("".toRegex()) || telephone.matches("".toRegex())
        -> message(context, context.getString(R.string.error03), vibreur)

        namePartner.matches("".toRegex()) || telephone.matches("".toRegex())
        -> message(context, context.getString(R.string.error03), vibreur)

        else -> registerData(name, namePartner, telephone, userData, context)
    }
}

/**
 * Save the user's data in a file
 * @param [name] the name of the user
 * @param [namePartner] the name of the partner
 * @param [telephone] the phone number of the partner
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
fun registerData(name: String, namePartner: String, telephone: String, userData: UserData,
                 context: Context) {
    userData.version = getAppVersion(context)
    userData.nom = name
    userData.nomPartner = namePartner
    userData.telephone = telephone
    userData.saveData(context)
    if (userData.role == "Aidant") {
        val intent = Intent(context, InstallDantPicActivity::class.java)
        startActivity(context, intent, null)
    } else {
        val intent = Intent(context, QRCodeScannerInstallActivity::class.java)
        startActivity(context, intent, null)
    }
}

/**
 * Get the app version
 * @return the app version as a String
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
fun getAppVersion(context: Context): String {
    var version = ""
    try {
        version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName,
                PackageManager.PackageInfoFlags.of(0)).versionName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return version
}

/**
 * Set the log appearance
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
fun setLogAppearance(userData: UserData, tvLog: TextView) {
    val sb = SpannableStringBuilder(userData.log)
    val fcs = ForegroundColorSpan(Color.rgb(57, 114, 26))
    val bss = StyleSpan(Typeface.BOLD)
    sb.setSpan(fcs, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    sb.setSpan(bss, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    tvLog.text = sb
}

/**
 * Shows the Aide's picture if it exists
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
fun showPicture(apercu: ImageView, userData: UserData) {
    val path = userData.path + "/SmallBrother/photo_aide.jpg"
    val file = File(path)
    if (file.exists()) apercu.setImageURI(Uri.fromFile(file))
}

/**
 * Get the current time and format it
 * @return The date as a String
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 08-01-2023)
 */
fun getCurrentTime(format: String) : String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val date = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(format)
        date.format(formatter)
    } else {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.format(date)
    }
}