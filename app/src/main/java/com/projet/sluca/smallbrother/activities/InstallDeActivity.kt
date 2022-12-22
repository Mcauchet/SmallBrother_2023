package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.message
import com.projet.sluca.smallbrother.models.UserData

/***
 * class InstallDeActivity manages the data of the Aidant in the Aide's app
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 22-12-2022)
 */
class InstallDeActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installde.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installde)

        val btnBack: Button = findViewById(R.id.btn_previous)
        val btnContinue: Button = findViewById(R.id.btn_suivant)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData
        Log.d("USERDATA", userData.toString())


        // Retrait du bouton retour, au cas où désactivé par ReglagesActivity.
        if (!userData.canGoBack) {
            btnBack.visibility = View.INVISIBLE
        }

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
        }

        btnContinue.setOnClickListener {
            vibreur.vibration(this, 100)
            continuer()
        }

        // Lancement des demandes de permissions.
        demandesPermissions()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun continuer() {
        // > Récupération du contenu des inputs :
        val etNom = findViewById<EditText>(R.id.input_nom)
        val nom = etNom.text.toString()

        val etNomPartner = findViewById<EditText>(R.id.input_partner)
        val nomPartner = etNomPartner.text.toString()

        val etTelephone = findViewById<EditText>(R.id.input_telephone)
        val telephone = etTelephone.text.toString()

        //Récupération de la version de SB en cours
        var version = ""
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                version = this.packageManager.getPackageInfo(
                    this.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName
            } else {
                @Suppress("DEPRECATION")
                version = this.packageManager.getPackageInfo(this.packageName, 0).versionName
            }
        } catch(e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        // > Vérification de la validité des informations entrées :
        when {
            telephone.length > 10 || !telephone.matches("".toRegex())
                    && !telephone.startsWith("04")
                -> message(this, getString(R.string.error01), vibreur)

            nom.matches("".toRegex()) || telephone.matches("".toRegex())
                -> message(this, getString(R.string.error03), vibreur)

            nomPartner.matches("".toRegex()) || telephone.matches("".toRegex())
            -> message(this, getString(R.string.error03), vibreur)

            else -> {
                // Sauvegarde en globale des valeurs entrées.
                userData.nom = nom
                userData.nomPartner = nomPartner
                userData.telephone = telephone
                userData.version = version

                // Transition vers l'activity suivante.
                val intent = Intent(this, InstallDe2Activity::class.java)
                intent.putExtra("nom", nom)
                intent.putExtra("telephone", telephone)
                intent.putExtra("version", version)
                intent.putExtra("nomPartner", nomPartner)
                startActivity(intent)
            }
        }
    }

    // --> DEMANDESPERMISSIONS() : Liste des permissions requises pour ce rôle.
    private fun demandesPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            @Suppress("DEPRECATION")
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,  // -> enregistrer un fichier.
                    Manifest.permission.READ_EXTERNAL_STORAGE,  // -> lire un fichier.
                    Manifest.permission.SEND_SMS,  // -> envoyer des SMS
                    Manifest.permission.CALL_PHONE,  // -> passer des appels
                    Manifest.permission.READ_SMS,  // -> lire les SMS
                    Manifest.permission.RECEIVE_SMS,  // -> recevoir des SMS
                    Manifest.permission.BROADCAST_SMS, // -> utiliser sms receiver
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,  // -> lancement d'activité
                    Manifest.permission.READ_PHONE_STATE,  // -> infos du téléphones
                    Manifest.permission.PROCESS_OUTGOING_CALLS,  // -> passer des appels
                    Manifest.permission.RECORD_AUDIO,  // -> enregistrer de l'audio.
                    Manifest.permission.CAMERA,  // -> utiliser l'appareil photo.
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,  // -> enregistrer un fichier.
                    Manifest.permission.SEND_SMS,  // -> envoyer des SMS
                    Manifest.permission.ACCESS_FINE_LOCATION, // -> localiser.
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}