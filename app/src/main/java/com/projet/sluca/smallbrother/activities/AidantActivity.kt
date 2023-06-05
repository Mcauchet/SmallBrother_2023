package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.AideData
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.security.PublicKey
import java.util.zip.ZipInputStream

/**
 * class AidantActivity manages the actions the Aidant can make
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 04-06-2023)
 */
class AidantActivity : AppCompatActivity() {

    val vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var tvLog: TextView

    private val logHandler: Handler = Handler(Looper.getMainLooper())

    private var successDl: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aidant)

        val btnSettings: Button = findViewById(R.id.btn_reglages)
        val btnFolder: Button = findViewById(R.id.btn_downloadFolder)
        val btnSmsAide: Button = findViewById(R.id.btn_sms_va_dant)
        val btnCall: Button = findViewById(R.id.btn_appel)
        val btnEmergency: Button = findViewById(R.id.btn_urgence)
        val btnFiles: Button = findViewById(R.id.btn_files)
        tvLog = findViewById(R.id.log_texte)

        userData = UserDataManager.getUserData(application)
        setAppBarTitle(userData, this)

        btnCall.text = getString(R.string.btn_appel).replace("§%", userData.nomPartner)
        btnEmergency.text = getString(R.string.btn_urgence)
            .replace("§%", particule(userData.nomPartner) +userData.nomPartner)

        wakeup(window, this@AidantActivity)
        reloadLog.run()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if(intent.hasExtra("url")){
            if(!isDNDModeOn(this)) alarm(this)
            intent.getStringExtra("url")?.let { url ->
                userData.saveURL(this, url)
                userData.urlToFile = url
            }
            if(isOnline(this)) CoroutineScope(Dispatchers.Main).launch {
                getContextCapture()
            }
            else message(this, "Veuillez vous connecter pour récupérer le contexte.", vibreur)
        }

        btnSettings.setOnClickListener {
            btnSettings()
        }

        btnFolder.setOnClickListener {
            btnFolder()
        }

        btnSmsAide.setOnClickListener {
            btnSmsAide()
        }

        btnCall.setOnClickListener {
            btnCall()
        }

        btnEmergency.setOnClickListener {
            btnEmergency()
        }

        btnFiles.setOnClickListener {
            btnFiles()
        }
    }

    /**
     * Manage clicks on the settings button
     */
    private fun btnSettings() {
        vibreur.vibration(this, 200)
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    /**
     * Manage clicks on the folder button
     */
    private fun btnFolder() {
        vibreur.vibration(this, 200)
        openDownloadDirectory()
    }

    /**
     * Manage clicks on the SMS button
     */
    private fun btnSmsAide() {
        vibreur.vibration(this, 200)
        val sms = getString(R.string.smsys02).replace("§%", userData.nom)
        if(sendSMS(this, sms, userData.telephone, vibreur)) {
            userData.bit = 0
            message(this, getString(R.string.message04), vibreur)
            userData.refreshLog(2)
        }
    }

    /**
     * Manage clicks on the call button
     */
    private fun btnCall() {
        userData.bit = 0
        vibreur.vibration(this, 200)
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:" + userData.telephone)
        startActivity(callIntent)
        message(this, getString(R.string.message05), vibreur)
        userData.refreshLog(7)
    }

    /**
     * Manage clicks on the emergency button
     */
    private fun btnEmergency() {
        vibreur.vibration(this, 200)
        userData.bit = 0
        createAndShowConfirmationAlertDialog()
    }

    /**
     * Manage clicks on the re-download button
     */
    private fun btnFiles() {
        userData.urlToFile = userData.loadURL(this)
        getContextCapture()
    }

    /**
     * Fetch the context file on the server by initiating a client, creating a destination file
     * and launching a request to the server.
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 01-05-2023)
     */
    private fun getContextCapture() {
        if (userData.urlToFile != "") {
            message(this, "Téléchargement du fichier en cours...", vibreur)
            val client = initClient()
            val file = createDestinationFile()
            fetchDataAndShowResult(client, file)
        } else userData.bit = 7
    }

    /**
     * Fetch the data on server and show result of download
     * @param client the HttpClient to access the API
     * @param file the file to write the data on
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 01-05-2023)
     */
    private fun fetchDataAndShowResult(client: HttpClient, file: File) {
        CoroutineScope(Dispatchers.Default).launch {
            getDataOnServer(client, file)
            client.close()
            Looper.prepare()
            showResultDownload()
        }
    }


    /**
     * Extract the content of the .zip archive into a directory saved in the downloads directory
     * @param file The zip file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 05-05-2023)
     */
    private fun extractArchive(file: File) {
        if (!file.exists()) return
        val time = getCurrentTime("HH'h'mm")
        val targetDir = File(Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
        "Situation_${userData.nomPartner}_${time}_${userData.urlToFile}".replace(".zip", ""))
        targetDir.mkdirs()
        ZipInputStream(FileInputStream(file)).use { zis ->
            runThroughArchive(zis, targetDir)
        }
        file.delete()
    }

    /**
     * Run through the zip archive in a ZipInputStream to save the file in the targetDir
     * @param zis the ZipInputStream
     * @param targetDir the directory to save the files in
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 01-05-2023)
     */
    private fun runThroughArchive(zis: ZipInputStream, targetDir: File) {
        var entry = zis.nextEntry
        while(entry != null) {
            val entryFile = File(targetDir, entry.name)
            if(entry.isDirectory) entryFile.mkdirs()
            else {
                entryFile.parentFile?.mkdirs()
                FileOutputStream(entryFile).use { fos ->
                    zis.copyTo(fos)
                    fos.close()
                }
            }
            entry = zis.nextEntry
        }
        zis.close()
    }

    /**
     * Open the download directory to show the context capture
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 01-05-2023)
     */
    private fun openDownloadDirectory() {
        val downloadDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse(downloadDirectory.absolutePath+(userData.urlToFile).replace(".zip", "")),
            "vnd.android.document/directory"
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * Show result of the context download in a Toast
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 01-05-2023)
     */
    private suspend fun showResultDownload() {
        if(successDl) withContext(Dispatchers.Main) {
            message(this@AidantActivity,
                "Téléchargement du fichier terminé, il se trouve dans votre " +
                        "dossier de téléchargement.", vibreur)
            userData.bit = 10
            openDownloadDirectory()
        } else  withContext(Dispatchers.Main) {
            message(this@AidantActivity, "Erreur lors du téléchargement. Veuillez réessayer " +
                    "ou vérifier la situation à nouveau.", vibreur)
        }
    }

    /**
     * Sends two GET requests to the server and retrieve the encrypted data and aesKey.
     * It then decrypts the data and stores it in the file created in the Downloads directory
     * @param [client] the HttpClient to access the server
     * @param [file] the file to store the data in
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 31-05-2023)
     */
    private suspend fun getDataOnServer(client: HttpClient, file: File) {
        val zipDataByteArray: ByteArray = downloadFileRequest(client)?.body() ?: return
        val aideData: AideData = client.get("$URLServer/aideData/${userData.urlToFile}").body()
        val aesBody = aideData.aesKey
        val signature = aideData.signature
        val iv = aideData.iv
        val aesEncKey: ByteArray = Base64.decode(aesBody, Base64.NO_WRAP)
        if(!SecurityUtils.verifyFile(zipDataByteArray, SecurityUtils.loadPublicKey(userData.pubKey)
                    as PublicKey, Base64.decode(signature, Base64.NO_WRAP))) return
        val decryptedData = SecurityUtils.decryptDataAes(zipDataByteArray, aesEncKey, iv)
        file.writeBytes(decryptedData)
        extractArchive(file)
        successDl = true
    }

    /**
     * Sends a request to the server to download the zip file
     * @param [client] the HttpClient to communicate with the Ktor server
     * @return the HttpResponse from the server
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 01-05-2023)
     */
    private suspend fun downloadFileRequest(client: HttpClient): HttpResponse? {
        try {
            return client.get(
                "$URLServer/download/${userData.urlToFile}"
            ) {
                onDownload { bytesSentTotal, contentLength ->
                    println("Receives $bytesSentTotal bytes from $contentLength")
                }
            }
        } catch (exception: Exception) {
            message(this, "Téléchargement échoué.", vibreur)
        }
        return null
    }

    /**
     * Get the Download directory and create a file in it which stores the zip data from the server
     * @return the created file
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun createDestinationFile(): File {
        val dir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .absolutePath
        val file = File(dir, "SmallBrother_Aide_${userData.urlToFile}")
        file.createNewFile()
        assert(file.exists())
        return file
    }


    /**
     * Instantiate the builder, configure it and show it
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun createAndShowConfirmationAlertDialog() {
        val builder = Builder(this)
        configureAlertDialog(builder)
        setAlertDialogButtons(builder)
        builder.create().show()
    }

    /**
     * Configure the Alert Dialog
     * @param [builder] the AlertDialog builder
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun configureAlertDialog(builder: Builder) {
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.btn_urgence)
            .replace("§%", particule(userData.nomPartner) +userData.nomPartner))
        builder.setMessage(getString(R.string.message02_texte))
    }

    /**
     * Configure positive and negative buttons of the AlertDialog
     * @param [builder] the AlertDialog builder
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun setAlertDialogButtons(builder: Builder) {
        builder.setPositiveButton(getString(R.string.oui)) { _, _ ->
            vibreur.vibration(this, 200)
            val sms = getString(R.string.smsys04).replace("§%", userData.nom)
            sendSMS(this, sms, userData.telephone, vibreur)
            message(this, getString(R.string.message07), vibreur)
            userData.refreshLog(10)
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            /* dialog window closes */
        }
    }

    private val reloadLog: Runnable = object : Runnable {
        override fun run() {
            when (userData.bit) {
                5 -> userData.refreshLog(5)
                7 -> userData.refreshLog(17)
                8 ->  userData.refreshLog(14)
                9 -> userData.refreshLog(16)
                10 -> userData.refreshLog(11)
                13 -> userData.refreshLog(13)
                19 -> userData.refreshLog(19)
            }
            if (userData.log != null) setLogAppearance(userData, tvLog)
            logHandler.postDelayed(this, 250)
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}