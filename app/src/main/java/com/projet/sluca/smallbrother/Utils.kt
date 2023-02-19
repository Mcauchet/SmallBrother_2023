package com.projet.sluca.smallbrother

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.ComponentName
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

const val URLServer = "https://smallbrother.be"

/**
 * Sends an SMS through the SMSManager class
 *
 * @param [context] context of the activity
 * @param [receiver] receiver of the SMS
 * @param [vibreur] the Vibration object
 * @return true if SMS was sent, false otherwise
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 17-01-2023)
 */
fun sendSMS(context: Context, msg: String, receiver: String, vibreur: Vibration): Boolean {
    if (!smsAvailable(context)) {
        message(context, "Veuillez retirer le mode avion pour envoyer un SMS.", vibreur)
        return false
    }
    val subscriptionId: Int = SmsManager.getDefaultSmsSubscriptionId()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
            .sendTextMessage(receiver, null, msg, sentPI(context), null)
    } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault().sendTextMessage(receiver, null, msg, sentPI(context), null)
    }
    return true
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

/**
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

/**
 * creates a toast with a message to print and vibrate
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

/**
 * Launches the application and put it on screen even if device locked
 *
 * @param [window] the window of the application
 * @param [activity] the activity to put on screen
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 07-02-2023)
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

/**
 * Returns true if device has validated network capabilities (Cellular, Wifi or Ethernet)
 * @param context the context of the application
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

/**
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
 * @version 1.2 (Updated on 19-02-2023)
 */
fun particule(name: String) : String {
    val particule = name[0].toString()
    val voyelles = arrayOf("A", "E", "Y", "U", "I", "O", "É", "È", "Œ", "a", "e", "y", "u", "i",
        "o", "é", "è")
    return if (listOf(*voyelles).contains(particule)) "d'" else "de "
}

/**
 * Redirects to the adequate activity according to the role
 * @param context the context of the application
 * @param userData the data of the user as a UserData object
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
 * @param [context] the Context of the application
 * @param [userData] the user's data
 * @param [vibreur] the phone Vibration system
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
fun checkInputs(name: String, namePartner: String, telephone: String, context: Context,
                        userData: UserData, vibreur: Vibration) {
    when {
        telephone.length > 10 || telephone.matches("".toRegex()) || !telephone.startsWith("04")
        -> message(context, context.getString(R.string.error01), vibreur)
        name.matches("".toRegex()) || telephone.matches("".toRegex())
                || namePartner.matches("".toRegex())
        -> message(context, context.getString(R.string.error03), vibreur)
        else -> registerData(name, namePartner, telephone, userData, context)
    }
}

/**
 * Save the user's data in a file
 * @param [name] the name of the user
 * @param [namePartner] the name of the partner
 * @param [telephone] the phone number of the partner
 * @param [userData] the user's data
 * @param [context] the Context of the application
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 16-01-2023)
 */
fun registerData(name: String, namePartner: String, telephone: String, userData: UserData,
                 context: Context) {
    require(name.isNotBlank() && telephone.isNotBlank() && namePartner.isNotBlank())
    userData.version = getAppVersion(context)
    userData.nom = name
    userData.nomPartner = namePartner
    userData.telephone = telephone
    userData.saveData(context)
    redirectAfterRegister(userData, context)
}

/**
 * Redirects the user after registering his datas
 * @param [userData] the user's data
 * @param [context] the Context of the application
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 16-01-2023)
 */
private fun redirectAfterRegister(userData: UserData, context: Context) {
    check(userData.role == "Aidant" || userData.role == "Aidé")
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
 * @param [context] the Context of the application
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
 * @param [userData] the user's data
 * @param [tvLog] the Log TextView
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 19-02-2023)
 */
fun setLogAppearance(userData: UserData, tvLog: TextView) {
    check(userData.log != null)
    val sb = SpannableStringBuilder(userData.log)
    val fcs = ForegroundColorSpan(Color.rgb(57, 114, 26))
    val bss = StyleSpan(Typeface.BOLD)
    sb.setSpan(fcs, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    sb.setSpan(bss, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    tvLog.text = sb
}

/**
 * Shows the Aide's picture if it exists
 * @param [apercu] the picture preview
 * @param [userData] the user's data
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 19-02-2023)
 */
fun showPicture(apercu: ImageView, userData: UserData) {
    check(userData.path != "")
    val path = userData.path + "/SmallBrother/photo_aide.jpg"
    assert(path != "/SmallBrother/photo_aide.jpg")
    val file = File(path)
    if (file.exists()) apercu.setImageURI(Uri.fromFile(file))
}

/**
 * Get the current time and format it
 * @param [format] the format of the date
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

/**
 * Activate the SMSReceiver to listen to incoming sms
 * @param context the context of the application
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 19-02-2023)
 */
fun activateSMSReceiver(context: Context) {
    val pm = context.packageManager
    val componentName = ComponentName(context, SmsReceiver::class.java)
    pm.setComponentEnabledSetting(
        componentName,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )
}

/**
 * Deactivate the SMSReceiver to listen to incoming sms
 * @param context the context of the application
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 19-02-2023)
 */
fun deactivateSmsReceiver(context: Context) {
    val pm = context.packageManager
    val componentName = ComponentName(context, SmsReceiver::class.java)
    pm.setComponentEnabledSetting(
        componentName,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
}

/**
 * Change the AppBarTitle according to the user's role
 * @param userData the data of the user
 * @param activity the activity running
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 19-02-2023)
 */
fun setAppBarTitle(userData: UserData, activity: AppCompatActivity) {
    activity.supportActionBar?.title = if (userData.role == "Aidé") "SmallBrother - Aidé"
    else "SmallBrother - Aidant"
}