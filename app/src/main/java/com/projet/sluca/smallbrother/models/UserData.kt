package com.projet.sluca.smallbrother.models

import android.app.Application
import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SmsReceiver
import com.projet.sluca.smallbrother.getCurrentTime
import com.projet.sluca.smallbrother.particule
import org.apache.commons.io.IOUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/***
 * creates an object UserData which contains all pieces of information about the user
 *
 * @property version: last version of SB
 * @property role: Aidant or Aidé
 * @property nom: self name
 * @property nomPartner: other one's name
 * @property telephone: other one's phone (if on aide's phone, telephone = aidant's phone number)
 * @property motion: Is the user moving
 * @property prive: Private mode
 * @property delay: time spent in Private mode
 * @property esquive: avoid redundant handlers for AideActivity
 * @property log: log content
 * @property canGoBack: indicates if going back is possible
 * @property bit: on Sms received, change the log message
 * @constructor creates a user with default properties
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 17-01-2023)
 */
data class UserData(
    var version: String = "", var role: String? = null, var nom: String = "",
    var nomPartner: String = "", var telephone: String = "", var motion: Boolean = false,
    var prive: Boolean = false, var delay: Long = 0, var esquive: Boolean = false,
    var log: String? = null, var canGoBack: Boolean = true, var bit: Int = 0,
    var pubKey: String = ""
) : Application() {

    var urlToFile: String = ""

    // this path is configured at first launch of the app through configurePath(..)
    var path: String = ""

    private val file = "donnees.txt"

    val url = "https://projects.info.unamur.be/geras/projects/smallbrother/"

    val help = "help/"

    /***
     * configurePath sets the path at runtime in Launch1Activity
     *
     * @param [context] the context of the activity running
     * @see Launch1Activity.onCreate
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 17-01-2023)
     */
    fun configurePath(context: Context?) {
        val tmpPath: String? = context?.filesDir?.path
        if(tmpPath != null) path = tmpPath
    }

    /***
     * saveData stores the user's data in a .txt file on the device
     *
     * @param [context] the context of the application
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 17-01-2023)
     */
    fun saveData(context: Context?) {
        val content = version + "\r" + role + "\r" + nom + "\r" + telephone + "\r" + pubKey + "\r" +
                nomPartner + "\r" + path
        try {
            val directory = File(context?.filesDir, "SmallBrother") // change this.filesDir into path ?
            if (!directory.exists()) directory.mkdirs()
            val dataFile = File(directory, file)
            if(!dataFile.exists()) dataFile.createNewFile() else byeData()
            writeDataInFile(dataFile, content, context)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Write content in dataFile
     * @param [dataFile] the file
     * @param [content] the content to write
     * @param [context] the context of the activity
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun writeDataInFile(dataFile: File, content: String, context: Context?) {
        val writer = BufferedWriter(FileWriter(dataFile, true))
        writer.write(content)
        writer.close()
        MediaScannerConnection.scanFile(context, arrayOf(dataFile.toString()), null, null)
    }

    /***
     * byeData deletes the donnees.txt file of the device
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    fun byeData() {
        val data = File("$path/SmallBrother/$file")
        data.delete()
        assert(!data.exists())
    }

    /**
     * deletePicture deletes the picture of the Aide on the device
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    fun deletePicture() {
        val file = File("$path/SmallBrother/photo_aide.jpg")
        if(file.exists()) {
            file.delete()
            assert(!file.exists())
        }
    }

    /***
     * loadData fetches the donnees.txt file and sets the UserData properties accordingly
     *
     * @param [context] the context of the application
     * @return true if data loaded, false otherwise
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 17-01-2023)
     */
    fun loadData(context: Context?): Boolean {
        val data = File(context?.filesDir, "SmallBrother/$file")
        if (data.exists() && data.canRead()) {
            try {
                return retrieveData(data) //test this (retrieveData returns true or false)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else Log.d("FILE", "Can't read the file")
        return false
    }

    /**
     * Retrieve the data written in the data file
     * @param [file] the file where the data are
     * @return true if the data were retrieved, false otherwise
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 17-01-2023)
     */
    private fun retrieveData(file: File): Boolean {
        require(file.exists())
        val dataTab: Array<String> = readDataFile(file)
        if(dataTab.size != 7) return false
        version = dataTab[0]
        role = dataTab[1]
        nom = dataTab[2]
        telephone = dataTab[3]
        pubKey = dataTab[4]
        nomPartner = dataTab[5]
        path = dataTab[6]
        return true
    }

    /**
     * Read the data file and returns it as an Array of String
     * @param [file] the read file
     * @return the data as an array of String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun readDataFile(file:File): Array<String> {
        val br = BufferedReader(FileReader(file))
        val dataLine = IOUtils.toString(br)
        return dataLine.split("\r").toTypedArray()
    }

    /***
     * getAutophotosPath retrieves the path of the captured pictures (1 and 2)
     *
     * @param [num] the number of the picture
     * @return the path of the pictures
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    fun getAutophotosPath(num: Int): String = "$path/SmallBrother/autophoto$num.jpg"

    /***
     * refreshLog sets the log accordingly to the code parameter
     * @param [code] the code associated to the log message
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 17-01-2023)
     */
    fun refreshLog(code: Int) {
        var texte = "${getCurrentTime("HH:mm")} : "
        texte += when (code) {
            1 -> getString(R.string.log01)
            2 -> getString(R.string.log02)
            3 -> getString(R.string.log03)
            4 -> getString(R.string.log04)
            5 -> getString(R.string.log05)
            6 -> getString(R.string.log06)
            7 -> getString(R.string.log07)
            8 -> getString(R.string.log08)
            9 -> getString(R.string.log09)
            10 -> getString(R.string.log10)
            11 -> getString(R.string.aide_needs_help).replace("§%", this.nomPartner)
            12 -> getString(R.string.log12)
            13 -> {
                val particule = particule(this.nomPartner)
                getString(R.string.log11).replace("§%", "$particule${this.nomPartner}")
            }
            16 -> getString(R.string.log16)
            18 -> getString(R.string.log18)
            19 -> getString(R.string.log19).replace("N#", SmsReceiver.tempsrestant)
            20 -> getString(R.string.log20)
            else -> ""
        }
        log = texte
    }

    /***
     * subDelay subtracts a number from the remaining delay in private mode
     *
     * @param [sub] the time to subtract
     * @see [AideActivity.reloadLog] for usage
     */
    fun subDelay(sub: Long) {
        delay = delay.minus(sub)
    }

}