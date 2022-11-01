package com.projet.sluca.smallbrother

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import org.apache.commons.io.IOUtils
import java.io.*

// Classe vouée à l'écoute de l'arrivée des SMS.
//TODO check if this class works and get rid of comments if it does (and document the process)
class SmsReceiver : BroadcastReceiver() {
    /*override fun onReceive(context: Context, intent: Intent) // Lors d'une réception de SMS :
    {
        lateinit var message: String
        val bundle = intent.extras
        val sms: Array<SmsMessage?>?
        userdata.loadData()
        if (bundle != null) {
            // Récupération du SMS reçu.
            val pdus = bundle["pdus"] as Array<*>?
            sms = arrayOfNulls(pdus!!.size)
            clef = "" // Réinitialisation de la valeur de la clef.
            for (i in sms.indices) {
                // Tri et mémorisation du contenu du SMS.
                sms[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray, bundle.getString("format"))
                numero = sms[i]?.originatingAddress // Set du numéro.
                message = sms[i]!!.messageBody
            }
            if (message.startsWith("SmallBrother :")) // Si SMS destiné à l'appli.
            {
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
                if (listOf(*motsclef).contains(clef)) {
                    if (bit == 1) // Si le Mode Privé est activé.
                    {
                        // Avertir :
                        if (clef == "[#SB02]") bit =
                            2 // cas d'un SMS
                        else if (clef == "[#SB04]") bit =
                            4 // cas d'un email
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
                        context.startActivity(intnt)
                    }
                }
            }
        }
    }*/

    lateinit var userdata: UserData

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras
        userdata.loadData()
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
        if (userdata.bit == 1) // Si le Mode Privé est activé.
        {
            // Avertir :
            if (clef == "[#SB02]") userdata.bit = 2 // cas d'un SMS
            else if (clef == "[#SB04]") userdata.bit = 4 // cas d'un email
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

        //lateinit var userdata: UserData// Liaison avec les données globales de l'utilisateur.


        // Placement des données dans un array, séparation par le retour-charriot.
        // Suppression du fichier de données s'il existe déjà (pour éviter concaténation).
        // Ecriture.
        // Rapatriement des données :
        // Récupération du contenu du fichier :

        // ---- Méchanismes de communication AideActivity / SmsReceiver.
        /*var bit: Int?
            get() {
                userdata.loadData()
                Log.d("USERDATA SMSRCV", userdata.toString())
                val data = File(userdata.path + "/SmallBrother/bit.txt")
                if (data.exists()) {
                    try  // Récupération du contenu du fichier :
                    {
                        // Placement des données dans un array, séparation par le retour-charriot.
                        val br =
                            BufferedReader(FileReader(data))
                        val dataLine = IOUtils.toString(br)
                        val dataTab =
                            dataLine.split("\r").toTypedArray()

                        // Rapatriement des données :
                        return Integer.valueOf(dataTab[0])
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return 0
            }
            set(bit) {
                userdata.loadData()
                Log.d("SETBIT UD", userdata.toString())
                try {
                    val bitFile = File(userdata.path + "/SmallBrother/bit.txt")
                    if (!bitFile.exists()) bitFile.createNewFile() else {
                        // Suppression du fichier de données s'il existe déjà (pour éviter concaténation).
                        val previousFile = File(userdata.path + "/SmallBrother/bit.txt")
                        previousFile.delete()
                    }

                    // Ecriture.
                    val writer = BufferedWriter(FileWriter(bitFile, true))
                    writer.write(bit.toString())
                    writer.close()
                } catch (_: IOException) {
                }
            }*/
    }
}