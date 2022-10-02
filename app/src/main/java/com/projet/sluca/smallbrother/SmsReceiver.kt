/*package com.projet.sluca.smallbrother

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import org.apache.commons.io.IOUtils
import java.io.*

// Classe vouée à l'écoute de l'arrivée des SMS.
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) // Lors d'une réception de SMS :
    {
        var message = ""
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
    }

    companion object {
        var numero: String? = null // Retiendra le numéro de l'envoyeur.
        var clef: String? = null // Retiendra le mot-clef du sms.

        lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.
        lateinit var tempsrestant: String // Retiendra le temps restant de Mode Privé pour l'Aidant.

        fun catchNumero(): String? {
            return numero
        } // Getter du numéro.

        fun catchClef(): String? {
            return clef
        } // Getter du mot-clef.

        fun catchTempsRestant(): String {
            return tempsrestant
        } // Getter du temps restant.

        // Placement des données dans un array, séparation par le retour-charriot.
        // Suppression du fichier de données s'il existe déjà (pour éviter concaténation).
        // Ecriture.
        // Rapatriement des données :
        // Récupération du contenu du fichier :

        // ---- Méchanismes de communication AideActivity / SmsReceiver.
        var bit: Int
            get() {
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
                try {
                    val bitFile = File(userdata.path + "/SmallBrother/bit.txt")
                    if (!bitFile.exists()) bitFile.createNewFile() else {
                        // Suppression du fichier de données s'il existe déjà (pour éviter concaténation).
                        val ciao = File(userdata.path + "/SmallBrother/bit.txt")
                        ciao.delete()
                    }

                    // Ecriture.
                    val writer = BufferedWriter(FileWriter(bitFile, true))
                    writer.write(bit.toString())
                    writer.close()
                } catch (_: IOException) {
                }
            }

    }
}*////