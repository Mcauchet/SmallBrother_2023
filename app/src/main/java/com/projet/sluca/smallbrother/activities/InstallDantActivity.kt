package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
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
 * class InstallDantActivity manages the installation for the aidant
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (updated on 14-11-22)
 */
class InstallDantActivity : AppCompatActivity() {
    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installdant.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installdant)

        val btnBack: Button = findViewById(R.id.btn_previous)
        val btnContinue: Button = findViewById(R.id.btn_continue)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData
        Log.d("USERDATA", userdata.toString())

        // Retrait du bouton retour, au cas où désactivé par ReglagesActivity.
        if (!userdata.canGoBack) {
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
        val setSmsAppIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)


        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun continuer() {
        // > Récupération du contenu des inputs :
        // Nom :
        val etNom = findViewById<EditText>(R.id.input_nom)
        val nom = etNom.text.toString()

        // Téléphone :
        val etTelephone = findViewById<EditText>(R.id.input_telephone)
        val telephone = etTelephone.text.toString()

        // > Vérification de la validité des informations entrées :
        when {
            telephone.length > 10 || !telephone.matches("".toRegex()) && !telephone.startsWith("04")
                -> message(this, getString(R.string.error01), vibreur)

            nom.matches("".toRegex()) || telephone.matches("".toRegex())
                -> message(this, getString(R.string.error03), vibreur)

            else -> {
                // Récupération de la version de SB en cours.
                var version: String? = ""
                try {
                    version = this.packageManager.getPackageInfo(this.packageName, 0).versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                // Sauvegarde en globale des valeurs entrées.
                userdata.role = "Aidant"
                userdata.version = version!!
                userdata.nom = nom
                userdata.telephone = telephone

                // Enregistrement de la DB.
                userdata.saveData(this)

                // Création de la fiche de l'Aidé.
                userdata.createFiche(this)

                // Transition vers l'activity suivante.
                val intent = Intent(this, InstallDantPicActivity::class.java)
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
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,  // -> enregistrer un fichier.
                    Manifest.permission.READ_EXTERNAL_STORAGE,  // -> lire un fichier.
                    Manifest.permission.CAMERA,  // -> utiliser l'appareil photo.
                    Manifest.permission.SEND_SMS,  // -> envoyer des SMS, si on passe à Signal, plus besoin de ça
                    Manifest.permission.CALL_PHONE,  // -> passer des appels
                    Manifest.permission.READ_SMS,  // -> lire les SMS, ni ça
                    Manifest.permission.RECEIVE_SMS,  // -> recevoir des SMS, et ça
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,  // -> lancement d'activité
                    Manifest.permission.READ_PHONE_STATE,  // -> infos du téléphones
                    Manifest.permission.PROCESS_OUTGOING_CALLS // -> passer des appels
                ), 1
            )
        }
    }

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}