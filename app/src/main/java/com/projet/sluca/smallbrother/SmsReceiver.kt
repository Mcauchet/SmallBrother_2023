package com.projet.sluca.smallbrother

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.projet.sluca.smallbrother.activities.WorkActivity
import com.projet.sluca.smallbrother.models.UserData

//TODO check if this class works and get rid of comments if it does (and document the process)
/***
 * SmsReceiver updates aide's log depending on received messages coming from the aidant
 * It listens to upcoming SMS and checks if it is relevant to SmallBrother app
 * (with the [#SBxx] code)
 *
 * @author Maxime Caucheteur & Sébastien Luca (Updated on 17-11-22)
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val bundle = intent?.extras
        Log.d("ONRCV SMS", bundle.toString())
        userdata?.loadData()
        val message = getTextFromSms(bundle)
        Log.d("MSG SMSRCV", message)
        clef = ""

        //Si SMS pas destiné à l'appli, on quitte la fonction
        if (!message.startsWith("SmallBrother :")) return

        // Si SMS destiné à l'appli.
        // Isolement du code d'identification, en fin de SMS (7 caras).
        clef = message.substring(message.length - 7)
        val motsclef = arrayOf(
            "[#SB01]",  // -> réinit aidé
            "[#SB02]",  // -> va bien reçu par aidé
            "[#SB03]",  // -> oui va bien reçu par aidant
            "[#SB04]",  // -> urgence reçue par aidé
            "[#SB05]",  // -> aidé pas connecté
            "[#SB06]",  // -> mail d'urgence reçu
            "[#SB07]" // -> mode privé activé
        )

        //Si la clef n'est pas contenue dans la liste des mots clés, on quitte la fonction
        if (!listOf(*motsclef).contains(clef)) return

        //La clef est dans la liste
        if (userdata?.bit == 1) // Si le Mode Privé est activé.
        {
            // Avertir :
            if (clef == "[#SB02]") userdata?.bit = 2 // cas d'un SMS
            else if (clef == "[#SB04]") userdata?.bit = 4 // cas d'un email
        } else {
            if (clef == "[#SB07]") // Récupération du temps restant si Mode Privé.
            {
                val extrait =
                    message.substring(message.indexOf("(") + 1, message.indexOf(")"))
                tempsrestant = extrait
            }
            // lancement de la "WorkActivity".
            val intnt = Intent(context, WorkActivity::class.java)
            intnt.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(intnt)
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

        var userdata: UserData? = null// Liaison avec les données globales de l'utilisateur.
    }
}