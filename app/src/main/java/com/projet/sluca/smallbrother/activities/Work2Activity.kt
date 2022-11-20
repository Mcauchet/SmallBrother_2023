package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.libs.*
import com.projet.sluca.smallbrother.models.AideData
import com.projet.sluca.smallbrother.models.UserData

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.io.File
import java.util.*

/***
 * class Work2Activity manages the captures of pictures if requested by the aidant
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 18-11-2022)
 */
class Work2Activity : AppCompatActivity(), PictureCapturingListener,
    OnRequestPermissionsResultCallback {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLoading: TextView // Déclaration d'un objet TextView.
    private lateinit var tvAction: TextView // Déclaration du TextView pour l'action en cours.
    private var urlGoogleMap: String? = null // Retiendra l'url vers la carte avec positionnement.
    private var batterie: String? = null // Retiendra le niveau de batterie restant.

    // Attribut de permission pour l'appel aux méthodes de "APictureCapturingService".
    // Must not be nullable in Kotlin in order for it to work
    private lateinit var pictureService: APictureCapturingService

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_work.xml (même écran).
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        // Liaison et remplissage des objets TextView.
        tvLoading = findViewById(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData
        loading(tvLoading) // Déclenchement de l'animation de chargement.

        // ================== [ Constitution du dossier joint ] ==================

        // --> [2] prise de deux photos automatiquement.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12B)

        // Lancement de la capture.
        Log.d("PIC SERVICE", "PIC SERVICE STARTS")
        pictureService = PictureCapturingServiceImpl.getInstance(this)
        pictureService.startCapturing(this)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Suite du processus après que les photos soient prises :
    override fun onDoneCapturingAllPhotos(picturesTaken: TreeMap<String, ByteArray>?) {
        Log.d("PIC SERVICE", "PIC SERVICE STOPS")
        // --> [3] localisation de l'Aidé.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12C)
        val latitude: String // Retiendra la coordonnée de latitiude.
        val longitude: String // Retiendra la coordonnée de longitude.

        // Vérification obligatoire des permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return //TODO check this

        // Récupération des données de latitude et longitude de l'appareil.
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            // Mémorisation des coordonnées récupérées.
            latitude = location.latitude.toString() // latitude
            longitude = location.longitude.toString() // longitude

            // Construction de l'URL GoogleMap avec les coordonnées.
            urlGoogleMap = "http://maps.google.com/maps?q=$latitude,$longitude"
        }

        // --> [4] assemblage d'une archive ZIP.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12D)

        // Récupération des différents fichiers :
        //TODO check what we keep between paquet & test if we store a zip
        val paquet = arrayOfNulls<String>(3) // liste des fichiers à zipper.
        val test = arrayOfNulls<File>(3)
        var numCell = 0 // marqueur numérique incrémentable.
        val fichier1 = userData.path + "/SmallBrother/audio.ogg"
        val file1 = File(fichier1)
        if (file1.exists()) // Enregistrement audio.
        {
            paquet[numCell] = fichier1
            test[numCell] = file1
            numCell++
        }
        val fichier2 = userData.getAutophotosPath(1)
        val file2 = File(fichier2)
        if (file2.exists()) // Autophoto 1.
        {
            paquet[numCell] = fichier2
            test[numCell] = file2
            numCell++
        }
        val fichier3 = userData.getAutophotosPath(2)
        val file3 = File(fichier3)
        if (file3.exists()) // Autophoto 2.
        {
            paquet[numCell] = fichier3
            test[numCell] = file3
        }

        val pcq = paquet.toString()
        val tst = test.toString()

        Log.d("FILES TO UPLOAD", pcq)
        Log.d("FILES TO UPLOAD WITH FILES", tst)

        //TEST
        sendSMS(this@Work2Activity, "PAQUET: $pcq\n TEST: $tst", userData.telephone)

        // Chemin de la future archive.
        val ziPath = userData.zipath

        // Lancement de la compression.
        val compressedFile = Compress(paquet, ziPath)
        compressedFile.zip()

        // --> [5] niveau de batterie.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12E)
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        batterie = "$level%"

        // --> [6] déterminer si en mouvement.
        val motion = if (userData.motion) "Oui" else "Non"

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12F)

        // Détermine la synthaxe du message selon la première lettre du nom de l'Aidé.
        val nomAide = userData.nom
        var particule = nomAide[0].toString()
        val voyelles = arrayOf(
            "A",
            "E",
            "Y",
            "U",
            "I",
            "O",
            "É",
            "È",
            "Œ",
            "a",
            "e",
            "y",
            "u",
            "i",
            "o",
            "é",
            "è"
        )

        particule =
            if (listOf(*voyelles).contains(particule)) " d'" else " de "

        val message = "Localisation $particule$nomAide : $urlGoogleMap\n" +
                "Niveau de batterie : $batterie\n" +
                "En mouvement : $motion.\n"

        //send localisation, battery and moving or not
        sendSMS(this@Work2Activity, message, userData.telephone)

        //TODO create AideData object here, and then send it in object.run.try{}
        object : Thread() {
            override fun run() {
                try {
                    //TODO Envoi des données sur le serveur (2 photos, 1 fichier audio)
                    val client = HttpClient(Android) {
                        install(ContentNegotiation) {
                            json()
                        }
                    }
                    Log.d("CLIENT", client.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = client.post("$URLServer/aideData") {
                                contentType(ContentType.Application.Json)
                                //sets the data to send to the server
                                setBody(AideData(
                                    randomString(12),
                                    randomString(13),
                                    randomString(8),
                                    userData.motion,
                                    level,
                                    randomString(32),
                                ))
                            }
                            client.close()
                            Log.d("BODY", response.toString())
                            Log.d("DATA SENT", "data")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (_: Exception) {
                }

                // Suppression des captures.
                file1.delete()
                file2.delete()
                file3.delete()

                // Suppression du fichier ZIP.
                val fileZ = File(ziPath)
                fileZ.delete()

                // Rafraîchissement du Log en fonction de la réussite du processus.
                CoroutineScope(Dispatchers.IO).launch {
                    if(isOnline(this@Work2Activity)) userData.refreshLog(11) else userData.refreshLog(15)
                }

                // Concoction et envoi du SMS à l'Aidant.
                var sms = getString(R.string.smsys06)
                sms = sms.replace("§%", userData.nom)

                sendSMS(this@Work2Activity, sms, userData.telephone)

                vibreur.vibration(this@Work2Activity, 330) // vibration.

                // Réactivation du SmsReceiver.
                val pm = this@Work2Activity.packageManager
                val componentName = ComponentName(this@Work2Activity, SmsReceiver::class.java)
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                // Retour à l'écran de rôle de l'Aidé.
                val intent = Intent(this@Work2Activity, AideActivity::class.java)
                startActivity(intent)
            }
        }.start() // Envoi !
    }

    /***
     * random string generator for testing purpose
     */
    fun randomString(length: Int): String {
        val validChars: List<Char> =
            ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return CharArray(length) { validChars.random() }.concatToString()
    }

    override fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?) {}

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}