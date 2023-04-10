package com.projet.sluca.smallbrother.utils

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SmsReceiver
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData

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
 * Warns the Aidant that aide is not connected to Internet
 * @param context the context of the application
 * @param vibreur the vibrator of the phone
 * @param userData the UserData instance
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 10-04-2023)
 */
fun warnAidantNoInternet(context: Context, vibreur: Vibration, userData: UserData) {
    MediaPlayer.create(context, R.raw.alarme).start()
    vibreur.vibration(context, 2000)

    var sms = context.getString(R.string.smsys05)
    sms = sms.replace("§%", userData.nom)
    sendSMS(context, sms, userData.telephone, vibreur)
}