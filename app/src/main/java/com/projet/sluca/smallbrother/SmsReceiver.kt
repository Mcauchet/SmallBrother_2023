package com.projet.sluca.smallbrother

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.activities.WorkActivity
import com.projet.sluca.smallbrother.models.UserData

/***
 * SmsReceiver updates aide's log depending on received messages coming from the aidant
 * It listens to upcoming SMS and checks if it is relevant to SmallBrother app
 * (with the [#SBxx] code)
 *
 * @author Maxime Caucheteur & Sébastien Luca (Updated on 27-12-22)
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        userData = context.applicationContext as UserData

        val bundle = intent.extras
        userData.loadData()
        val message = getTextFromSms(bundle)
        clef = ""

        //Si SMS pas destiné à l'appli, on quitte la fonction
        if (!message.startsWith("SmallBrother :")) return

        // Si SMS destiné à l'appli.
        // Isolement du code d'identification, en fin de SMS (7 caras).
        // Can't exceed 100 #SB code
        clef = message.substring(message.length - 7)
        Log.d("clef", clef.toString())
        val motsclef = arrayOf(
            "[#SB01]",  // -> réinit aidé
            "[#SB02]",  // -> va bien? reçu par aidé
            "[#SB03]",  // -> oui va bien reçu par aidant
            "[#SB04]",  // -> urgence reçue par aidé
            "[#SB05]",  // -> aidé pas connecté
            "[#SB06]",  // -> fichier sur serveur
            "[#SB07]", // -> mode privé activé
            "[#SB08]", // -> aide needs help
            "[#SB10]", // -> aidant receives url to files
        )

        //Si la clef n'est pas contenue dans la liste des mots clés, on quitte la fonction
        if (!listOf(*motsclef).contains(clef)) return

        if(userData.role == "Aidant") {
            when (clef) {
                "[#SB10]" -> {
                    userData.bit = 10
                    // The subsequence depends on the URL to the file, if its length changes,
                    // the subsequence must be changed too
                    val urlFile = message
                        .subSequence(message.length - 37, message.length - 8)
                        .toString()
                    userData.urlToFile = urlFile
                    val intnt = Intent(context, AidantActivity::class.java)
                    intnt.putExtra("url", urlFile)
                    intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intnt)
                }
                "[#SB08]" -> {
                    userData.bit = 8
                    val intnt = Intent(context, AidantActivity::class.java)
                    intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intnt)
                }
                else -> return
            }
        }

        if(userData.role != "Aidé") {
            Log.d("role", userData.role.toString())
            return
        }

        //La clef est dans la liste
        if (userData.bit == 1) // Si le Mode Privé est activé.
        {
            // Avertir :
            if (clef == "[#SB02]") userData.bit = 2 // cas d'un SMS
            else if (clef == "[#SB04]") userData.bit = 4 // cas d'une capture de contexte
        } else {
            //TODO see if works
            if(clef == "[#SB07]") {
                tempsrestant = message.substring(message.indexOf("(") + 1, message.indexOf(")"))
            }
            // lancement de la "WorkActivity".
            val intnt2 = Intent(context, WorkActivity::class.java)
            intnt2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intnt2)
        }
    }

    /***
     * extract message body from SMS
     *
     * @author joekickass (https://github.com/joekickass)
     * (code on https://github.com/joekickass/android-monday-madness/blob/master/app/src/main/
     * kotlin/com/joekickass/mondaymadness/SmsReceiver.kt)
     */
    private fun getTextFromSms(extras: Bundle?): String {
        @Suppress("DEPRECATION")
        val pdus = extras?.get("pdus") as Array<*>
        val format = extras.getString("format")
        var txt = ""
        for(pdu in pdus) {
            val smsMsg = getSmsMsg(pdu as ByteArray?, format)
            val subMsg = smsMsg?.displayMessageBody
            subMsg?.let {txt = "$txt$it"}
            numero = smsMsg?.originatingAddress
        }
        return txt
    }

    /***
     * create an SmsMessage from a raw PDU with the specified message format.
     *
     * @param [pdu] the message PDU from the SMS_RECEIVED_ACTION intent
     * @param [format] the format extra from the SMS_RECEIVED_ACTION intent
     * @return
     * @author joekickass (https://github.com/joekickass)
     * (code on https://github.com/joekickass/android-monday-madness/blob/master/app/src/main/
     * kotlin/com/joekickass/mondaymadness/SmsReceiver.kt)
     */
    private fun getSmsMsg(pdu: ByteArray?, format: String?): SmsMessage? {
        return SmsMessage.createFromPdu(pdu, format)
    }

    companion object {
        var numero: String? = null // Retiendra le numéro de l'envoyeur.
        var clef: String? = null // Retiendra le mot-clef du sms.

        lateinit var tempsrestant: String // Retiendra le temps restant de Mode Privé pour l'Aidant.

        lateinit var userData: UserData// Liaison avec les données globales de l'utilisateur.
    }
}