package com.projet.sluca.smallbrother.models

import android.app.Application
import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SmsReceiver
import org.apache.commons.io.IOUtils
import java.io.*
import java.security.KeyPair
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

// Mémorisation et gestion des données globales de l'utilisateur et autres données utiles, en session et en DB.
/***
 * creates an object UserData which contains all pieces of information about the user
 *
 * @property version: last version of SB
 * @property role: Aidant or Aidé
 * @property nom: other one's name (if on aide's phone, nom = aidant's name)
 * @property telephone: other one's phone (if on aide's phone, telephone = aidant's phone number)
 * @property motion: Is the user moving
 * @property prive: DND mode
 * @property delai: time spent in DND mode
 * @property esquive: avoid redundant handlers for AideActivity
 * @property log: log content
 * @property canGoBack: indicates if going back is possible
 * @property bit: on Sms received, change the log message
 * @constructor creates a user with default properties
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (modified on 28-11-22)
 */
data class UserData(
    var version: String = "", var role: String? = null, var nom: String = "",
    var telephone: String = "", var motion: Boolean = false,
    var prive: Boolean = false, var delai: Long = 0, var esquive: Boolean = false,
    var log: String? = null, var canGoBack: Boolean = true, var bit: Int = 0,
) : Application() {

    // -> Appel du chemin globalisé vers le dossier "SmallBrother".
    // Centralisation des chemins de fichiers :
    //val path = Environment
    //    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    //    .absolutePath + "/SmallBrother/"

    var keyPair: KeyPair? = null

    var pubKey: String = ""

    var urlToFile: String = ""

    //this path is configured at first launch of the app through configurePath(..)
    var path: String = ""

    private val file = "donnees.txt" // datas de l'utilisateur
    private val fiche = "fiche_aide.txt" // fiche de l'Aidé
    private val date = "date.txt" // date de création de la fiche de l'Aidé
    //private val photo = "photo_aide.jpg" // photo de l'Aidé

    // -> Appel du chemin globalisé vers le zip d'un rapport de situation.
    //val zipath = "/sdcard/Download/SmallBrother/situation_partenaire.zip"
    val zipath: String = "$path/situation_partenaire.zip"

    // -> Donne l'URL de la racine du dossier Web de SB.
    // Centralisation des URL :
    val url = "https://projects.info.unamur.be/geras/projects/smallbrother/"

    // -> Donne la part d'URL nécessaire pour accéder à l'aide de SB.
    val help = "help/"

    /***
     * configurePath sets the path at runtime in Launch1Activity
     *
     * @param [context] the context of the activity running
     * @see Launch1Activity.onCreate
     */
    fun configurePath(context: Context?) {
        val tmpPath: String? = context?.filesDir?.path
        if(tmpPath != null) path = tmpPath

        Log.d("TMPPATH", tmpPath.toString())
    }

    /***
     * saveData stores the user's data in a .txt file on the device
     *
     * @param [context] the context of the activity running
     */
    fun saveData(context: Context?) {
        // Structuration du contenu du futur fichier (info, retour-charriot).
        val contenu = version + "\r" + role + "\r" + nom + "\r" + telephone

        Log.d("CONTENU", contenu)

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
            val testFile = File(dossier, file)
            testFile.parent?.let { Log.d("PARENT", it) }
            Log.d("DONNEES.TXT BEFORE", testFile.exists().toString())

            // Suppression du fichier de données s'il existe déjà.
            if(!testFile.exists()) testFile.createNewFile()
            else byeData()

            Log.d("DONNEES.TXT AFTER", testFile.exists().toString())
            Log.d("DONNEES PATH", testFile.path)

            // Ecriture.
            val writer = BufferedWriter(FileWriter(testFile, true))
            writer.write(contenu)

            Log.d("TESTFILE", testFile.canRead().toString())
            testFile.forEachLine { Log.d("WRITING", it) }

            writer.close()
            MediaScannerConnection.scanFile(context, arrayOf(testFile.toString()), null, null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /***
     * byeData deletes the donnees.txt file of the device
     */
    fun byeData() {
        val data = File("$path/SmallBrother/$file")
        data.delete()
    }

    /***
     * loadData fetches the donnees.txt file and sets the UserData properties accordingly
     *
     * @return true if data loaded, false otherwise
     */
    fun loadData(): Boolean {

        // Chargement du fichier TXT pointé par "path".
        val data = File(this.filesDir, "SmallBrother/$file")
        Log.d("DATA", data.toString())
        Log.d("donnees exists", data.exists().toString())
        Log.d("PERMISSION", data.canRead().toString())
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
                    Log.d("AIDEDATA", "test")
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
    //val photoIdentPath: String = "$path/SmallBrother/$photo"

    // -> Appel du chemin globalisé vers la capture audio.
    //val audioPath: String = "$path/SmallBrother/audio.ogg"

    /***
     * getAutophotosPath retrieves the path of the captured pictures (1 and 2)
     *
     * @param [num] the number of the picture
     * @return the path of the pictures
     */
    fun getAutophotosPath(num: Int): String = "$path/SmallBrother/autophoto$num.jpg"

    // -> Appel du chemin globalisé vers la fiche de l'aidé.
    //val fichePath: String = "$path/SmallBrother/$fiche"

    /***
     * refreshLog sets the log accordingly to the code entered as a parameter
     *
     * @param [code] the code associated to the log message
     */
    fun refreshLog(code: Int) {
        // Capture de la date et de l'heure actuelles.
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.FRENCH)
        val date = df.format(c.time)
        var texte = "$date : " // Début de message.
        //TODO see if possible to put texte += when (code) {...} instead of 19 times texte += ...
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
                texte = texte.replace("N#", SmsReceiver.tempsrestant)
            }
            else -> texte += ""
        }
        log = texte // Set du Log.
    }

    /***
     * subDelai subtracts a number from the remaining delay in private mode
     *
     * @param [sub] the time to subtract
     * @see [AideActivity.reloadLog] for usage
     */
    fun subDelai(sub: Long) {
        delai = delai.minus(sub)
    }

    /***
     * createFiche creates the file containing all the "Aidé"'s information
     *
     * @param [context] context of the activity running
     */
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
        for (champ in champs) {
            texte += champ + "\r\r"
        }
        Log.d("FOREACHLOOP", texte)

        // Enregistrement de la fiche :
        val fichette = File("$path/SmallBrother/$fiche")
        writeFile(fichette, texte, context)
        try {
            Runtime.getRuntime().exec("chmod 777 $path/SmallBrother/$fiche")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Mémorisation de la date de création de la fiche :
        val datation = File("$path/SmallBrother/$date")
        writeFile(datation, toDayte, context)
    }

    // -> Centralisation de l'écriture de fichier
    /***
     * writeFile creates the file and writes the text in the file (text and file in parameters)
     *
     * @param [file] the file to create, nothing happens if file already exists
     * @param [texte] the text to write in [file]
     * @param [context] the context of the activity running
     */
    private fun writeFile(file: File, texte: String?, context: Context?) {
        try {
            if (file.exists()) return // Créer uniquement si non déjà existant.
            file.createNewFile()
            // Ecriture.
            val writer = BufferedWriter(FileWriter(file, true))
            writer.write(texte)
            writer.close()
            MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
            Log.d("WriteFile", texte.toString())
        } catch (e: IOException) {
            Log.e("WRITEFILE", e.toString())
        }
    }

    private val toDayte: String = DateFormat.getDateTimeInstance().format(Date())

    /***
     * dateFichier retrieves the modification date of a file given his path and transforms it
     * into a string
     *
     * @param [chemin] the path of the file
     * @return the date of the last modification as a String
     * @see [pleineFiche] for usage
     */
    private fun dateFichier(chemin: String): String {
        val file = File(chemin)
        val lastModDate = Date(file.lastModified())
        return lastModDate.toString()
    }

    /***
     * pleineFiche checks if the "Aidé's fiche" exists (and is completed?)
     *
     * @return true if the fiche exists, false otherwise
     * @see [AidantActivity.reloadLog] for usage
     */
    fun pleineFiche(): Boolean {
        // Chargement du fichier TXT retenant la date de création de la fiche de l'Aidé.
        val dataD = File("$path/SmallBrother/$date")
        val dataF = File("$path/SmallBrother/$fiche")

        return dataD.exists()
                && dataF.exists()
                && (dateFichier("$path/SmallBrother/$date")
                != dateFichier("$path/SmallBrother/$fiche"))
    }

}