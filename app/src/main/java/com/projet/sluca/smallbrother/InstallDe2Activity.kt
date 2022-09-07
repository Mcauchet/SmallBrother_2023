package com.projet.sluca.smallbrother

import android.content.Intent
//import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class InstallDe2Activity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installde.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installde2)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData
    }

    // --> Au clic que le bouton "Précédent".
    fun precedent(view: View?) {
        vibreur.vibration(this, 100)
        finish()
    }

    // --> Au clic que le bouton "Terminer".
    fun terminer(view: View?) {
        vibreur.vibration(this, 100)


        // > Récupération du contenu des inputs :

        // Email de l'Aidé :
        val etMyEmail = findViewById<EditText>(R.id.input_mymail)
        val myEmail = etMyEmail.text.toString()

        // Email :
        val etPassword = findViewById<EditText>(R.id.input_password)
        val password = etPassword.text.toString()


        // > Vérification de la validité des informations entrées :

        // Vérification 1 : l'adresse email n'est pas valide.
        val provider = myEmail.substring(myEmail.length - 10)
        if (!myEmail.matches("".toRegex()) && provider != "@gmail.com") // !myemail.contains ("@"))
        {
            message(getString(R.string.error02))
        } else if (myEmail.matches("".toRegex()) || password.matches("".toRegex())) {
            message(getString(R.string.error03))
        } else {
            // Récupération de la version de SB en cours.
            /*
            var version: String? = ""
            try {
                version =
                    this.packageManager.getPackageInfo(this.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
             */

            // Sauvegarde en globale des valeurs entrées.
            userData.role = "Aidé"
            userData.mymail = myEmail
            userData.password = password
            userData.saveData(this) // Sauvegarde des données d'utilisateur.

            // Rétablissement des boutons retour, au cas où désactivé par ReglagesActivity.
            userData.canGoBack = true

            // Transition vers l'activity suivante.
            val intent = Intent(this, AideActivity::class.java)
            startActivityForResult(intent, 1)
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