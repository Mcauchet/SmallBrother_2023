package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.security.PublicKey

/***
 * class AidantActivity manages the actions the Aidant can make
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 18-01-2023)
 */
class AidantActivity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var tvLog: TextView

    private val logHandler: Handler = Handler(Looper.getMainLooper())

    private var successDl: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aidant)

        val btnSettings: Button = findViewById(R.id.btn_reglages)
        val btnPicture: Button = findViewById(R.id.btn_photo)
        val btnReduct: Button = findViewById(R.id.btn_reduire)

        val btnSmsAide: Button = findViewById(R.id.btn_sms_va_dant)
        val btnCall: Button = findViewById(R.id.btn_appel)

        val btnEmergency: Button = findViewById(R.id.btn_urgence)
        val btnFiles: Button = findViewById(R.id.btn_files)

        userData = application as UserData
        userData.loadData(this)
        check(userData.role == "Aidant")

        btnCall.text = getString(R.string.btn_appel).replace("§%", userData.nomPartner)

        btnFiles.text = getString(R.string.recuperer_les_donnees_de_l_aide)
            .replace("§%", particule(userData.nomPartner)+userData.nomPartner)

        btnEmergency.text = getString(R.string.btn_urgence)
            .replace("§%", particule(userData.nomPartner)+userData.nomPartner)

        tvLog = findViewById(R.id.log_texte)

        reloadLog.run()

        wakeup(window, this@AidantActivity)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        btnSettings.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, ReglagesActivity::class.java)
            startActivity(intent)
        }

        btnPicture.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, PhotoAide::class.java)
            startActivity(intent)
        }

        btnReduct.setOnClickListener {
            vibreur.vibration(this, 200)
            message(this, getString(R.string.message01), vibreur)
            moveTaskToBack(true)
        }

        btnSmsAide.setOnClickListener {
            vibreur.vibration(this, 200)
            val sms = getString(R.string.smsys02).replace("§%", userData.nom)
            if(sendSMS(this, sms, userData.telephone, vibreur)) {
                message(this, getString(R.string.message04), vibreur)
                userData.refreshLog(4)
            }
        }

        btnCall.setOnClickListener {
            vibreur.vibration(this, 200)
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:" + userData.telephone)
            startActivity(callIntent)
            message(this, getString(R.string.message05), vibreur)
            userData.refreshLog(7)
        }

        btnEmergency.setOnClickListener {
            vibreur.vibration(this, 330)
            createAndShowConfirmationAlertDialog()
        }

        btnFiles.setOnClickListener {
            if (userData.urlToFile != ""){
                message(this, "Téléchargement du fichier en cours...", vibreur)
                val client = initClient()
                val file = createDestinationFile()
                userData.urlToFile = if (intent.hasExtra("url"))
                    intent.getStringExtra("url").toString() else ""
                CoroutineScope(Dispatchers.IO).launch {
                    getDataOnServer(client, file)
                    Looper.prepare()
                    if(successDl) message(this@AidantActivity, "Téléchargement du fichier terminé, "
                            + "il se trouve dans votre dossier de téléchargement.", vibreur)
                    else message(this@AidantActivity, "Erreur lors du téléchargement. Veuillez " +
                            "réessayer ou capturer le contexte à nouveau.", vibreur)
                }
            } else {
                message(this, "Il n'y a pas de fichier appartenant à ${userData.nomPartner} " +
                        "sur le serveur, veuillez effectuer une capture de contexte.", vibreur)
            }
        }
    }

    /**
     * Sends two get request to the server and retrieve the encrypted data and aesKey.
     * It then decrypts the data and stores it in the file created in the Downloads directory
     * @param [client] the HttpClient to access the server
     * @param [file] the file to store the data in
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 08-01-2023)
     */
    private suspend fun getDataOnServer(client: HttpClient, file: File) {
        val zipDataByteArray: ByteArray = downloadFileRequest(client).body()
        val aesBody: String = client.get("$URLServer/aes/${userData.urlToFile}").body()
        val signature: String = client.get("$URLServer/sign/${userData.urlToFile}").body()
        val aesEncKey: ByteArray = Base64.decode(aesBody, Base64.NO_WRAP)
        if(!SecurityUtils.verifyFile(zipDataByteArray,
                SecurityUtils.loadPublicKey(userData.pubKey) as PublicKey,
                Base64.decode(signature, Base64.NO_WRAP))
        ) return
        Log.d("file verified", "ok")
        val decryptedData = SecurityUtils.decryptDataAes(zipDataByteArray, aesEncKey)
        file.writeBytes(decryptedData)
        successDl = true
    }

    /**
     * Sends a request to the server to download the zip file
     * @param [client] the HttpClient to communicate with the Ktor server
     * @return the HttpResponse from the server
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private suspend fun downloadFileRequest(client: HttpClient): HttpResponse {
        return client.get(
            "$URLServer/download/${userData.urlToFile}"
        ) {
            onDownload { bytesSentTotal, contentLength ->
                println("Receives $bytesSentTotal bytes from $contentLength")
            }
        }
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
        val file = File(dir, "SmallBrother_Aide_${userData.urlToFile}.zip")
        file.createNewFile()
        assert(file.exists())
        return file
    }

    /**
     * Create and initialize the HttpClient
     * @return the HttpClient
     */
    private fun initClient() : HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
        }
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
            .replace("§%", particule(userData.nomPartner)+userData.nomPartner))
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
                8 ->  userData.refreshLog(11)
                10 -> userData.refreshLog(13)
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