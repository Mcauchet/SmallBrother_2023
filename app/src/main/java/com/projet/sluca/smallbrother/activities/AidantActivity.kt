package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.runBlocking
import java.io.File

/***
 * class AidantActivity manages the actions the Aidant can make
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 28-12-2022)
 */
class AidantActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLog: TextView // Déclaration du TextView pour le Log.

    // Handler pour rafraîchissement log.
    private val logHandler: Handler = Handler(Looper.getMainLooper())

    private lateinit var flTiers: FrameLayout // Déclaration du FrameLayout pour le bouton Tiers.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_aidant.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aidant)

        val btnSettings: Button = findViewById(R.id.btn_reglages)
        val btnPicture: Button = findViewById(R.id.btn_photo)
        val btnReduct: Button = findViewById(R.id.btn_reduire)
        val btnSmsAidant: Button = findViewById(R.id.btn_sms_va_dant)
        val btnCall: Button = findViewById(R.id.btn_appel)
        val btnEmergency: Button = findViewById(R.id.btn_urgence)
        val btnFiles: Button = findViewById(R.id.btn_files)
        val btnTiers: Button = findViewById(R.id.btn_tiers)


        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        btnCall.text = getString(R.string.btn_appel).replace("§%", userData.nomPartner)

        btnFiles.text = getString(R.string.recuperer_les_donnees_de_l_aide)
            .replace("§%", particule(userData.nomPartner)+userData.nomPartner)

        btnEmergency.text = getString(R.string.btn_urgence)
            .replace("§%", particule(userData.nomPartner)+userData.nomPartner)

        // Liaison avec le TextView affichant le Log et ajout de sa valeur en cours.
        tvLog = findViewById(R.id.log_texte)

        // Liaison avec le FrameLayout affichant le bouton Tiers.
        flTiers = findViewById(R.id.contour5)

        // Lancement de l'activité en arrière-plan (rafraîchissement).
        reloadLog.run()

        // Sortie de veille du téléphone et mise en avant-plan de cette appli.
        wakeup(window, this@AidantActivity)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        btnSettings.setOnClickListener {
            vibreur.vibration(this, 100)

            // Transition vers la ReglagesActivity.
            val intent = Intent(this, ReglagesActivity::class.java)
            startActivity(intent)
        }

        btnPicture.setOnClickListener {
            vibreur.vibration(this, 100)

            // Transition vers la ReglagesActivity.
            val intent = Intent(this, PhotoAide::class.java)
            startActivity(intent)
        }

        btnReduct.setOnClickListener {
            vibreur.vibration(this, 200)
            message(this, getString(R.string.message01), vibreur) // Message d'avertissement.
            moveTaskToBack(true) // Mise de l'appli en arrière-plan.
        }

        btnSmsAidant.setOnClickListener {
            vibreur.vibration(this, 200)
            userData.loadData() // Raptatriement des données de l'utilisateur.

            // Concoction et envoi du SMS.
            var sms = getString(R.string.smsys02)
            sms = sms.replace("§%", userData.nom)

            sendSMS(this, sms, userData.telephone)

            message(this, getString(R.string.message04), vibreur) // toast de confirmation.
            userData.refreshLog(4) // rafraîchissement du Log.
        }

        btnCall.setOnClickListener {
            vibreur.vibration(this, 200)
            userData.loadData() // Raptatriement des données de l'utilisateur.

            // Lancement de l'appel.
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:" + userData.telephone)
            startActivity(callIntent)
            message(this, getString(R.string.message05), vibreur) // toast de confirmation.
            userData.refreshLog(7) // rafraîchissement du Log.
        }

        btnEmergency.setOnClickListener {
            vibreur.vibration(this, 330)

            // Demande de confirmation.
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.btn_urgence)
                .replace("§%", particule(userData.nomPartner)+userData.nomPartner))
            builder.setMessage(getString(R.string.message02_texte))
            builder.setPositiveButton(
                getString(R.string.oui)
            ) { _, _ ->
                // Si choix = "OUI" :
                vibreur.vibration(this, 200)
                userData.loadData() // Raptatriement des données de l'utilisateur.

                // Concoction et envoi du SMS.
                var sms = getString(R.string.smsys04)
                sms = sms.replace("§%", userData.nom)

                sendSMS(this, sms, userData.telephone)

                message(this, getString(R.string.message07), vibreur) // toast de confirmation.
                userData.refreshLog(10) // rafraîchissement du Log.
            }
            builder.setNegativeButton(
                android.R.string.cancel
            ) { _, _ ->
                // Si choix = "ANNULER" :
                /* rien */
            }
            val dialog = builder.create()
            dialog.show()
        }

        btnFiles.setOnClickListener {
            if (userData.urlToFile != ""){
                Toast.makeText(this, "Téléchargement du fichier en cours...", Toast.LENGTH_SHORT).show()
                val client = HttpClient(Android) {
                    install(ContentNegotiation) {
                        json()
                    }
                    install(HttpRequestRetry) {
                        retryOnServerErrors(maxRetries = 3)
                        exponentialDelay()
                    }
                }

                val dir =
                    Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .absolutePath
                val file = File(dir, "SmallBrother_Aide.zip")
                file.createNewFile()
                if(intent.hasExtra("url")) {
                    userData.urlToFile = intent.getStringExtra("url").toString()
                }
                runBlocking {
                    val httpResponse: HttpResponse = client.get(
                        "$URLServer/download/${userData.urlToFile}"
                    ) {
                        onDownload { bytesSentTotal, contentLength ->
                            println("Receives $bytesSentTotal bytes from $contentLength")
                        }
                    }
                    Log.d("urlToFile", userData.urlToFile)
                    val aesHttp: HttpResponse = client.get(
                        "$URLServer/aes/${userData.urlToFile}"
                    )

                    //retrieve AES encrypted KEY, decrypt it and use it to decrypt the data
                    val aesBody: String = aesHttp.body()

                    val aesDecKey = Base64.decode(aesBody, Base64.NO_WRAP)

                    //retrieve zip data ByteArray
                    val responseBody: ByteArray = httpResponse.body()

                    //decrypt data with the decrypted AES key
                    val decryptedData = SecurityUtils.decryptDataAes(responseBody, aesDecKey)
                    file.writeBytes(decryptedData)
                }
                Toast.makeText(this, "Téléchargement du fichier terminé, il se trouve dans votre " +
                        "dossier de téléchargement.", Toast.LENGTH_SHORT).show()
            }
        }

        btnTiers.setOnClickListener {
            //tiers()
        }
    }

    /*fun tiers() {
        vibreur.vibration(this, 200)
        userData.loadData() // Raptatriement des données de l'utilisateur.
    }*/

    // --> Rafraîchissement automatique toutes les 250 ms du TextView de Log et des boutons.
    private val reloadLog: Runnable = object : Runnable {
        override fun run() {
            when (userData.bit) {
                8 ->  userData.refreshLog(11)
                10 -> userData.refreshLog(13)
            }
            // Log :
            if (userData.log != null) {
                // Coloration en vert et mise en gras de la date (19 premiers caras).
                val sb = SpannableStringBuilder(userData.log)
                val fcs = ForegroundColorSpan(Color.rgb(57, 114, 26))
                val bss = StyleSpan(Typeface.BOLD)
                sb.setSpan(fcs, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                sb.setSpan(bss, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                tvLog.text = sb // affichage
            }

            // Bouton Tiers :
            /* TODO afficher le bouton Tiers si le document zip est présent dans les downloads */

            logHandler.postDelayed(this, 250) // rafraîchissement
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}