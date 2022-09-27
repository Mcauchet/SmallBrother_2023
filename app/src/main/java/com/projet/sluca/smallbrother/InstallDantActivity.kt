package com.projet.sluca.smallbrother

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class InstallDantActivity : AppCompatActivity() {
    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installdant.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installdant)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData
        Log.d("USERDATA", userdata.toString())

        // Retrait du bouton retour, au cas où désactivé par ReglagesActivity.
        if (!userdata.canGoBack) {
            val btn = findViewById<Button>(R.id.btn_previous)
            btn.visibility = View.INVISIBLE
        }

        // Lancement des demandes de permissions.
        demandesPermissions()
    }

    // --> Au clic que le bouton "Précédent".
    fun precedent(view: View?) {
        vibreur.vibration(this, 100)
        finish()
    }

    // --> Au clic que le bouton "Continuer".
    fun continuer(view: View?) {
        vibreur.vibration(this, 100)


        // > Récupération du contenu des inputs :

        // Nom :
        val etNom = findViewById<EditText>(R.id.input_nom)
        val nom = etNom.text.toString()

        // Téléphone :
        val etTelephone = findViewById<EditText>(R.id.input_telephone)
        val telephone = etTelephone.text.toString()


        // > Vérification de la validité des informations entrées :

        // Vérification 1 : le numéro de téléphone n'a pas une structure vraisemblable.
        if (telephone.length > 10 || !telephone.matches("".toRegex()) && !telephone.startsWith("04")) {
            message(getString(R.string.error01))
        } else if (nom.matches("".toRegex()) || telephone.matches("".toRegex())) {
            message(getString(R.string.error03))
        } else {
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
            startActivityForResult(intent, 1)
        }
    }

    // --> DEMANDESPERMISSIONS() : Liste des permissions requises pour ce rôle.
    fun demandesPermissions() {
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
                    Manifest.permission.SEND_SMS,  // -> envoyer des SMS
                    Manifest.permission.CALL_PHONE,  // -> passer des appels
                    Manifest.permission.READ_SMS,  // -> lire les SMS
                    Manifest.permission.RECEIVE_SMS,  // -> recevoir des SMS
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,  // -> lancement d'activité
                    Manifest.permission.READ_PHONE_STATE,  // -> infos du téléphones
                    Manifest.permission.PROCESS_OUTGOING_CALLS // -> passer des appels
                ), 1
            )
        }
    }

    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    fun message(message: String?) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
        vibreur.vibration(this, 330)
    }

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    override fun onBackPressed() {
        moveTaskToBack(false)
    }
}