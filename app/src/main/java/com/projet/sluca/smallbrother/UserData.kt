package com.projet.sluca.smallbrother

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import org.apache.commons.io.IOUtils
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

// Mémorisation et gestion des données globales de l'utilisateur et autres données utiles, en session et en DB.
/***
 * creates an object UserData which contains all pieces of information about the user
 *
 * @param version: last version of SB
 * @param role: Aidant or Aidé
 * @param nom: user's name
 * @param telephone: user's phone
 * @param email: helper's email
 * @param mymail: aide's email
 * @param password: user's password
 * @param motion: Is the user moving
 * @param prive: DND mode
 * @param delai: time spent in DND mode
 * @param esquive: avoid redundant handlers for AideActivity
 * @param log: log content
 * @param canGoBack: indicates if going back is possible
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 2 (modified on 29-09-22)
 */
data class UserData(var version: String = "", var role: String? = null, var nom: String = "",
                    var telephone: String = "", var email: String = "", var mymail: String = "",
                    var password:String = "", var motion: Boolean = false,
                    var prive: Boolean = false, var delai: Long = 0, var esquive: Boolean = false,
                    var log: String? = null, var canGoBack: Boolean = true) : Application() {

    // -> Appel du chemin globalisé vers le dossier "SmallBrother".
    // Centralisation des chemins de fichiers :
    val path = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .absolutePath + "/SmallBrother/"
    private val file = "donnees.txt" // datas de l'utilisateur
    private val fiche = "fiche_aide.txt" // fiche de l'Aidé
    private val date = "date.txt" // date de création de la fiche de l'Aidé
    private val photo = "photo_aide.jpg" // photo de l'Aidé

    // -> Appel du chemin globalisé vers le zip d'un rapport de situation.
    //val zipath = "/sdcard/Download/SmallBrother/situation_partenaire.zip"
    val zipath: String = Environment.getExternalStorageDirectory().path

    // -> Donne l'URL de la racine du dossier Web de SB.
    // Centralisation des URL :
    val url = "https://projects.info.unamur.be/geras/projects/smallbrother/"

    // -> Donne la part d'URL nécessaire pour accéder à l'aide de SB.
    val help = "help/"

    // Fonctions complexes :
    // -> Sauvegarde en TXT des données de l'utilisateur.
    fun saveData(context: Context?) {
        // Structuration du contenu du futur fichier (info, retour-charriot).
        var contenu = version + "\r" + role + "\r" + nom + "\r" + telephone
        contenu += "\r" + email + "\r" + mymail + "\r" + password

        Log.d("CONTENU", contenu)
        Log.d("PATH", path)

        // Enregistrement :
        try {
            // Création du dossier "Downloads/SmallBrother", s'il n'existe pas déjà.
            /*val dossier = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .absolutePath, "SmallBrother")*/
            val dossier = File(this.filesDir, "SmallBrother")
            if (!dossier.exists()) {
                dossier.mkdirs()
            }

            // Création du fichier "donnees.txt" dans ce dossier, via la variable "path".
            val testFile = File(path + file)
            if (!testFile.exists()) testFile.createNewFile() else {
                byeData() // Suppression du fichier de données s'il existe déjà.
            }

            // Ecriture.
            //val writer = BufferedWriter(FileWriter(testFile, true))

            this.openFileOutput(file, Context.MODE_PRIVATE).use {
                it.write(contenu.toByteArray())
            }
            //TODO Test this (see if lines are well written in the document)
            File(file).forEachLine { Log.d("WRITING", it) }
            //writer.write(contenu)
            //writer.close()
            MediaScannerConnection.scanFile(context, arrayOf(testFile.toString()), null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // -> Suppression des données de l'utilisateur.
    fun byeData() {
        val data = File(path + file)
        data.delete()
    }

    // -> Chargement du fichier TXT contenant les données de l'utilisateur et conversion en session.
    fun loadData(): Boolean {
        // Chargement du fichier TXT pointé par "path".
        val data = File(path + file)
        Log.d("DATA", data.toString())
        //TODO this condition fails (the canRead() part)
        if (data.exists() && data.canRead()) {
            Log.d("IFLOOP", "I'm in")
            try  // Récupération du contenu du fichier :
            {
                // Placement des données dans un array, séparation par le retour-charriot.
                val br = BufferedReader(FileReader(data))
                val dataLine = IOUtils.toString(br)
                val dataTab: Array<String> = dataLine.split("\r").toTypedArray()
                Log.d("DATATAB", dataTab[0])

                // Rapatriement des données :
                version = dataTab[0]
                role = dataTab[1]
                nom = dataTab[2]
                telephone = dataTab[3]
                if (dataTab.size > 4) // Si compte de l'Aidé :
                {
                    email = dataTab[4] // + email de l'Aidant
                    mymail = dataTab[5] // + email de l'Aidé
                    password = dataTab[6] // + son mdp
                }
                return true
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Log.d("FILE", "Can't read the file")
        }
        return false
    }

    // -> Appel du chemin globalisé vers la photo d'identité de l'aidé.
    val photoIdentPath: String = path + photo

    // -> Appel du chemin globalisé vers la capture audio.
    val audioPath: String = path + "audio.ogg"

    // -> Appel du chemin globalisé vers les photos capturées (1 et 2).
    fun getAutophotosPath(num: Int): String = path + "autophoto" + num.toString() + ".jpg"

    // -> Appel du chemin globalisé vers la fiche de l'aidé.
    val fichePath: String = path + fiche

    // -> Mise à jour du Log en fonction du numéro entré en paramètre.
    fun refreshLog(code: Int) {
        // Capture de la date et de l'heure actuelles.
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.FRENCH)
        val date = df.format(c.time)
        var texte = "$date : " // Début de message.
        when (code) {
            1 -> texte += getString(R.string.log01)
            2 -> texte += getString(R.string.log02)
            3 -> texte += getString(R.string.log03)
            4 -> texte += getString(R.string.log04)
            5 -> texte += getString(R.string.log05)
            6 -> texte += getString(R.string.log06)
            7 -> texte += getString(R.string.log07)
            8 -> texte += getString(R.string.log08)
            9 -> texte += getString(R.string.log09)
            10 -> texte += getString(R.string.log10)
            11 -> texte += getString(R.string.log11)
            12 -> texte += getString(R.string.log12)
            13 -> texte += getString(R.string.log13)
            14 -> texte += getString(R.string.log14)
            15 -> texte += getString(R.string.log15)
            16 -> texte += getString(R.string.log16)
            17 -> texte += getString(R.string.log17)
            18 -> texte += getString(R.string.log18)
            19 -> {
                // Message avec insertion du temps de Mode Privé restant.
                texte += getString(R.string.log19)
                texte = texte.replace("N#", SmsReceiver.catchTempsRestant())
            }
        }
        log = texte // Set du Log.
    }

    // -> Retrait d'une certaine quantité de temps au délai gardé en mémoire.
    fun subDelai(sub: Long) {
        delai = delai.minus(sub)
    } // Délai moins le paramètre.

    // -> Création de la fiche de l'Aidé.
    fun createFiche(context: Context?) {
        var texte = "" // Futur contenu du fichier texte.
        val champs = arrayOf(
            "Concerne : Mr/Mme ... ",
            "Ses centres d'intérêt : ",
            "Ses médicaments : ",
            "Son médecin traitant : ",
            "Sa famille se compose de : ",
            "Ses handicaps : ",
            "Ses allergies : ",
            "Capable de solliciter de l’aide à des passants ? : ",
            "La personne de confiance à joindre : "
        )

        // Assemblage du contenu en un String.
        for (ligne in champs) {
            texte += ligne + "\r\r"
        }
        Log.d("FORLOOP", texte)
        //TODO Test to see if same result
        champs.forEach {
            texte += it + "\r\r"
        }
        Log.d("FOREACHLOOP", texte)

        // Enregistrement de la fiche :
        val fichette = File(path + fiche)
        writeFile(fichette, texte, context)
        try {
            Runtime.getRuntime().exec("chmod 777 $path$fiche")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Mémorisation de la date de création de la fiche :
        val datation = File(path + date)
        writeFile(datation, toDayte, context)
    }

    // -> Centralisation de l'écriture de fichier
    private fun writeFile(file: File, texte: String?, context: Context?) {
        try {
            if (!file.exists()) // Créer uniquement si non déjà existant.
            {
                file.createNewFile()
                // Ecriture.
                val writer = BufferedWriter(FileWriter(file, true))
                writer.write(texte)
                writer.close()
                MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
            }
        } catch (_: IOException) {
        }
    }

    private val toDayte: String = DateFormat.getDateTimeInstance().format(Date())

    // -> Récupère la date de modification d'un fichier
    private fun dateFichier(chemin: String): String {
        val file = File(chemin)
        val lastModDate = Date(file.lastModified())
        return lastModDate.toString()
    }

    // -> Vérification : la fiche de l'Aidé existe et a été complétée.
    fun pleineFiche(): Boolean {
        // Chargement du fichier TXT retenant la date de création de la fiche de l'Aidé.
        val dataD = File(path + date)
        val dataF = File(path + fiche)

        return dataD.exists()
                && dataF.exists()
                && (dateFichier(path + date) != dateFichier(path + fiche))
    }
}