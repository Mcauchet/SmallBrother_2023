package com.projet.sluca.smallbrother

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.activities.AideActivity
import com.projet.sluca.smallbrother.activities.Launch1Activity
import com.projet.sluca.smallbrother.activities.WorkActivity
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.isOnline
import com.projet.sluca.smallbrother.utils.warnAidantNoInternet

/**
 * SmsReceiver updates aide's log depending on received messages coming from the aidant
 * It listens to upcoming SMS and checks if it is relevant to SmallBrother app
 * (with the [#SBxx] code)
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 01-05-2023)
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        userData = context.applicationContext as UserData
        val vibreur = Vibration()

        val bundle = intent.extras
        userData.loadData(context)
        val message = getTextFromSms(bundle)
        clef = ""

        if (!message.startsWith("SmallBrother :")) return
        if (numero != userData.telephone) return

        // If SMS is for the application and comes from the partner :
        // Fetch the #SBxx code of the SMS (Can't exceed 100 #SB code)
        clef = message.substring(message.length - 7)
        val motsclef = arrayOf(
            "[#SB01]",  // -> reinitialisation of data
            "[#SB02]",  // -> aidant asks aide if everything's ok
            "[#SB03]",  // -> aide is alright
            "[#SB04]",  // -> context capture
            "[#SB05]",  // -> aide not connected
            "[#SB07]", // -> private mode ON
            "[#SB08]", // -> aide needs help
            "[#SB09]", // -> upload failed
            "[#SB10]", // -> aidant receives url to files
        )

        if (!listOf(*motsclef).contains(clef)) return

        if(userData.role == "Aidant") {
            val intnt = Intent(context, AidantActivity::class.java)
            when (clef) {
                "[#SB03]" -> {
                    userData.bit = 0
                    userData.refreshLog(5)
                }
                "[#SB05]" -> {
                    userData.bit = 0
                    userData.refreshLog(13)
                }
                "[#SB07]" -> {
                    userData.bit = 0
                    tempsrestant = message.substring(message.indexOf("(") + 1, message.indexOf(")"))
                    userData.refreshLog(19)
                }
                "[#SB08]" -> userData.bit = 8
                "[#SB09]" -> userData.bit = 9
                "[#SB10]" -> {
                    // The subsequence depends on the URL to the file, if its length changes,
                    // the subsequence must be changed too
                    val urlFile = message
                        .subSequence(message.length - 37, message.length - 8)
                        .toString()
                    userData.urlToFile = urlFile
                    intnt.putExtra("url", urlFile)
                }
            }
            intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intnt)
        }

        if(userData.role != "Aidé") return

        if (userData.bit == 1) // Private mode ON
        {
            when(clef) {
                "[#SB02]" -> userData.bit = 2
                "[#SB04]" -> userData.bit = 4
            }
        } else {
            when(clef) {
                "[#SB01]" -> {
                    Vibration().vibration(context, 330)
                    userData.refreshLog(3)
                    userData.byeData("donnees.txt")
                    if(!userData.loadData(context)){
                        val mIntent = Intent(context, Launch1Activity::class.java)
                        mIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(mIntent)
                    }
                }
                "[#SB02]" -> {
                    userData.refreshLog(6)
                    val intnt = Intent(context, AideActivity::class.java)
                    intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intnt)
                }
                "[#SB04]" -> {
                    if(isOnline(context)) {
                        val intnt = Intent(context, WorkActivity::class.java)
                        intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intnt)
                    } else {
                        userData.refreshLog(12)

                        warnAidantNoInternet(context, vibreur, userData)

                        val intnt = Intent(context, AideActivity::class.java)
                        context.startActivity(intnt)
                    }
                }
            }
        }
    }

    /**
     * extract message body from SMS
     * @param extras the Bundle object containing the pdus
     * @return the SMS message as a String
     * @author joekickass (https://github.com/joekickass)
     * (code on https://github.com/joekickass/android-monday-madness/blob/master/app/src/main/
     * kotlin/com/joekickass/mondaymadness/SmsReceiver.kt)
     */
    private fun getTextFromSms(extras: Bundle?): String {
        @Suppress("DEPRECATION")
        val pdus = extras?.get("pdus") as Array<*>
        val format = extras.getString("format")
        var txt = ""
        pdus.forEach { pdu ->
            val smsMsg = getSmsMsg(pdu as ByteArray?, format)
            val subMsg = smsMsg?.displayMessageBody
            subMsg?.let {txt = "$txt$it"}
            numero = smsMsg?.originatingAddress?.replace("+32", "0")
        }
        return txt
    }

    /**
     * create an SmsMessage from a raw PDU with the specified message format.
     *
     * @param [pdu] the message PDU from the SMS_RECEIVED_ACTION intent
     * @param [format] the format extra from the SMS_RECEIVED_ACTION intent
     * @return the SmsMessage object from the Pdu
     * @author joekickass (https://github.com/joekickass)
     * (code on https://github.com/joekickass/android-monday-madness/blob/master/app/src/main/
     * kotlin/com/joekickass/mondaymadness/SmsReceiver.kt)
     */
    private fun getSmsMsg(pdu: ByteArray?, format: String?): SmsMessage? {
        return SmsMessage.createFromPdu(pdu, format)
    }

    companion object {
        var numero: String? = null
        var clef: String? = null
        lateinit var tempsrestant: String
        lateinit var userData: UserData
    }
}