package com.projet.sluca.smallbrother.utils

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.models.UserData
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val URLServer = "https://smallbrother.be"

/**
 * Launches the application and put it on screen even if device locked
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
 * emits a fast sound to signal the user something is happening on his phone
 * @param context the Context of the application
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 10-04-2023)
 */
fun alarm(context: Context) {
    setAlarmVolume(context)
    val mediaPlayer = MediaPlayer.create(
        context,
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    )
    mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(1.5f)
    mediaPlayer.start()
    Handler(Looper.getMainLooper()).postDelayed( {
        mediaPlayer.stop()
    }, 5000L)
}

/**
 * Sets the volume of the alarm
 * @param context the context of the application
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 10-04-2023)
 */
private fun setAlarmVolume(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
    val volume = 0.6f
    val calculatedVolume = (maxVolume * volume).toInt()
    audioManager.setStreamVolume(AudioManager.STREAM_RING, calculatedVolume, 0)
}
