package com.projet.sluca.smallbrother

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/***
 * class InstallDe2Activity manages the data of the aide.
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 10-10-2022)
 */
class InstallDe2Activity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installde.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installde2)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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
        when {
            !myEmail.matches("".toRegex()) && provider != "@gmail.com"
                -> message(this, getString(R.string.error02), vibreur)

            myEmail.matches("".toRegex()) || password.matches("".toRegex())
                -> message(this, getString(R.string.error03), vibreur)

            else -> {
                // Récupération de la version de SB en cours.
                // (une fois est nécessaire, pas besoin dans InstallDeActivity)
                var version = ""
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        version =
                            this.packageManager
                                .getPackageInfo(
                                    this.packageName,
                                    PackageManager.PackageInfoFlags.of(0)
                                ).versionName
                    } else {
                        @Suppress("DEPRECATION")
                        version =
                            this.packageManager.getPackageInfo(this.packageName, 0).versionName
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }


                if(intent.hasExtra("nom")){
                    userData.nom = intent.getStringExtra("nom").toString()
                }
                if(intent.hasExtra("mail")){
                    userData.email = intent.getStringExtra("mail").toString()
                }
                if(intent.hasExtra("telephone")){
                    userData.telephone = intent.getStringExtra("telephone").toString()
                }

                // Sauvegarde en globale des valeurs entrées.
                userData.version = version
                userData.role = "Aidé"
                userData.mymail = myEmail
                userData.password = password
                userData.saveData(this) // Sauvegarde des données d'utilisateur.

                // Rétablissement des boutons retour, au cas où désactivé par ReglagesActivity.
                userData.canGoBack = true

                // Transition vers l'activity suivante.
                val intent = Intent(this, AideActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}