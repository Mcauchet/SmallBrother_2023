package com.projet.sluca.smallbrother

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.projet.sluca.smallbrother.libs.*
import java.io.File
import java.util.*

/***
 * class Work2Activity manages the captures of pictures if requested by the aidant
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 28-10-2022)
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
        pictureService = PictureCapturingServiceImpl.getInstance(this)
        pictureService.startCapturing(this)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Suite du processus après que les photos soient prises :
    override fun onDoneCapturingAllPhotos(picturesTaken: TreeMap<String, ByteArray>?) {
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
        val paquet = arrayOfNulls<String>(3) // liste des fichiers à zipper.
        var numCell = 0 // marqueur numérique incrémentable.
        val fichier1 = userData.path + "/SmallBrother/audio.ogg"
        val file1 = File(fichier1)
        if (file1.exists()) // Enregistrement audio.
        {
            paquet[numCell] = fichier1
            numCell++
        }
        val fichier2 = userData.getAutophotosPath(1)
        val file2 = File(fichier2)
        if (file2.exists()) // Autophoto 1.
        {
            paquet[numCell] = fichier2
            numCell++
        }
        val fichier3 = userData.getAutophotosPath(2)
        val file3 = File(fichier3)
        if (file3.exists()) // Autophoto 2.
        {
            paquet[numCell] = fichier3
        }

        // Chemin de la future archive.
        val ziPath = userData.zipath

        // Lancement de la compression.
        val c = Compress(paquet, ziPath)
        c.zip()

        // --> [5] niveau de batterie.

        // Affichage de l'action en cours.
        tvAction.text = getString(R.string.message12E)
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        batterie = "$level%"

        // --> [6] déterminer si en mouvement.
        val motion = if (userData.motion) "Oui" else "Non"

        // --> [7] envoi de l'email d'urgence (avec Libs/Sender.java).

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
        val url = " <a href=\"$urlGoogleMap\">ouvrir dans GoogleMap</a>"
        val message = "<br><b>Localisation " + particule + nomAide + " :</b> " + url +
                "<br><br>" +
                "<b>Niveau de batterie :</b> " + batterie +
                "<br><br>" +
                "<b>En mouvement :</b> " + motion +
                "<br><br>"
        object : Thread() {
            override fun run() {
                try {
                    //TODO Envoi des données sur le serveur
                    Log.d("DATA SEND", "data")
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
                if(checkInternet()) userData.refreshLog(11)
                else userData.refreshLog(15)

                // Concoction et envoi du SMS à l'Aidant.
                var sms = getString(R.string.smsys06)
                sms = sms.replace("§%", userData.nom)

                this@Work2Activity.getSystemService(SmsManager::class.java)
                    .sendTextMessage(userData.telephone, null, sms,
                        sentPI(this@Work2Activity), null)

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

        // =======================================================================
    }

    override fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?) {}

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}