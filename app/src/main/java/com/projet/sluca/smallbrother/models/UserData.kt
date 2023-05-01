package com.projet.sluca.smallbrother.models

import android.app.Application
import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SmsReceiver
import com.projet.sluca.smallbrother.utils.getCurrentTime
import com.projet.sluca.smallbrother.utils.particule
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*

/**
 * creates an object UserData which contains all pieces of information about the user
 *
 * @property version: last version of SB
 * @property role: Aidant or Aidé
 * @property nom: self name
 * @property nomPartner: other one's name
 * @property telephone: other one's phone (if on aide's phone, telephone = aidant's phone number)
 * @property motion: Is the user moving
 * @property prive: Private mode
 * @property delay: time left in Private mode
 * @property esquive: avoid redundant handlers
 * @property log: log content
 * @property canGoBack: indicates if going back is possible
 * @property bit: on Sms received, change the log message
 * @property pubKey: the public key of the partner for encryption/signing purpose
 * @constructor creates a user with default properties
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 01-05-2023)
 */
data class UserData(
    var version: String = "", var role: String? = null, var nom: String = "",
    var nomPartner: String = "", var telephone: String = "", var motion: Boolean = false,
    var prive: Boolean = false, var delay: Long = 0, var esquive: Boolean = false,
    var log: String? = null, var canGoBack: Boolean = true, var bit: Int = 0,
    var pubKey: String = ""
) : Application() {

    var urlToFile: String = ""
    var path: String = ""
    private val file = "donnees.txt"

    /**
     * configurePath sets the path at runtime in Launch1Activity
     * @param [context] the context of the activity running
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 17-01-2023)
     */
    fun configurePath(context: Context) {
        path = context.filesDir.path
    }

    /**
     * saveData stores the user's data in a .txt file on the device
     * @param [context] the context of the application
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 17-01-2023)
     */
    fun saveData(context: Context) {
        val content = version + "\r" + role + "\r" + nom + "\r" + telephone + "\r" + pubKey + "\r" +
                nomPartner + "\r" + path
        try {
            val directory = File(context.filesDir, "SmallBrother")
            if (!directory.exists()) directory.mkdirs()
            val dataFile = File(directory, file)
            if(!dataFile.exists()) dataFile.createNewFile() else byeData("donnees.txt")
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
    fun writeDataInFile(dataFile: File, content: String, context: Context?) {
        val writer = BufferedWriter(FileWriter(dataFile, true))
        writer.write(content)
        writer.close()
        MediaScannerConnection.scanFile(context, arrayOf(dataFile.toString()), null, null)
    }

    /**
     * byeData deletes the donnees.txt file of the device
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    fun byeData(fileName: String) {
        val data = File("$path/SmallBrother/$fileName")
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

    /**
     * loadData fetches the donnees.txt file and sets the UserData properties accordingly
     *
     * @param [context] the context of the application
     * @return true if data loaded, false otherwise
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 27-04-2023)
     */
    fun loadData(context: Context?): Boolean {
        val data = File(context?.filesDir, "SmallBrother/$file")
        if (data.exists() && data.canRead()) {
            try {
                return retrieveData(data)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: java.lang.IllegalArgumentException) {
                Log.e("LOAD DATA", "File not found")
            }
        } else Log.d("LOAD DATA", "Can't read the file or doesn't exist")
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
     * Checks if installation process is finished and every property is set
     * @param context the context of the application
     * @return true if installation is completed, false otherwise
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 27-04-2023)
     */
    fun installCompleted(context: Context): Boolean {
        if (loadData(context)) {
            when {
                this.role == null -> return false
                this.nom == "" -> return false
                this.telephone == "" -> return false
                this.pubKey == "" -> return false
                this.nomPartner == "" -> return false
                this.path == "" -> return false
            }
            return true
        }
        return false
    }

    /**
     * Save the URL to access the context data file on the server
     * @param context the context of the activity
     * @param url the url to save in the file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 09-03-2023)
     */
    fun saveURL(context: Context?, url: String) {
        try {
            val urlFile = File(context?.filesDir, "SmallBrother/url.txt")
            if(!urlFile.exists()) urlFile.createNewFile() else byeData("url.txt")
            writeDataInFile(urlFile, url, context)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Loads the URL saved in the url.txt file to access the context data on the server
     * @param context the context of the activity
     * @return the url as a String, empty string if no url file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 28-03-2023)
     */
    fun loadURL(context: Context?): String {
        val urlFile = File(context?.filesDir, "SmallBrother/url.txt")
        if(urlFile.exists() && urlFile.canRead()) {
            try {
                val dataTab: Array<String> = readDataFile(urlFile)
                this.urlToFile = dataTab[0]
                return dataTab[0]
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ""
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

    /**
     * getAutophotosPath retrieves the path of the captured pictures (1 and 2)
     *
     * @param [num] the number of the picture
     * @return the path of the pictures
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    fun getAutophotosPath(num: Int): String = "$path/SmallBrother/autophoto$num.jpg"

    /**
     * refreshLog sets the log accordingly to the code parameter
     * @param [code] the code associated to the log message
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 01-05-2023)
     */
    fun refreshLog(code: Int) {
        var texte = "${getCurrentTime("HH:mm")}: "
        texte += when (code) {
            1 -> getString(R.string.log_install)
            2 -> getString(R.string.log_reset_aidant)
            3 -> getString(R.string.log_reset_aide)
                .replace("§%", particule(this.nomPartner) +this.nomPartner)
            4 -> getString(R.string.log_aidant_send_SMS).replace("§%", this.nomPartner)
            5 -> getString(R.string.log_aidant_receive_SMS).replace("§%", this.nomPartner)
            6 -> getString(R.string.log_aide_receive_SMS).replace("§%", this.nomPartner)
            7 -> getString(R.string.log_call).replace("§%", this.nomPartner)
            8 -> getString(R.string.log_called).replace("§%", this.nomPartner)
            9 -> getString(R.string.log_call_missed)
                .replace("§%", particule(this.nomPartner) +this.nomPartner)
            10 -> getString(R.string.log_aidant_requests_context)
                .replace("§%", particule(this.nomPartner) +this.nomPartner)
            11 -> getString(R.string.log_data_on_server)
                .replace("§%", particule(this.nomPartner) +this.nomPartner)
            12 -> getString(R.string.log_no_internet_aide).replace("§%", this.nomPartner)
            13 -> getString(R.string.log_no_internet_aidant).replace("§%", this.nomPartner)
            14 -> getString(R.string.log_aide_needs_help).replace("§%", this.nomPartner)
            15 -> getString(R.string.log_aide_send_SMS).replace("§%", this.nomPartner)
            16 -> getString(R.string.log_upload_failed)
            17 -> getString(R.string.log_download_ko).replace("§%", this.nomPartner)
            18 -> getString(R.string.log_private_expired)
            19 -> getString(R.string.log_private_warn).replace("N#", SmsReceiver.tempsrestant)
                .replace("§%", this.nomPartner)
            20 -> getString(R.string.log_private_on)
            21 -> getString(R.string.log_context_sent).replace("§%", this.nomPartner)
            22 -> getString(R.string.aidant_request_context_no_internet)
            else -> ""
        }
        log = texte
    }

    /**
     * subDelay subtracts a number from the remaining delay in private mode
     *
     * @param [sub] the time to subtract
     */
    fun subDelay(sub: Long) {
        delay = delay.minus(sub)
    }

}