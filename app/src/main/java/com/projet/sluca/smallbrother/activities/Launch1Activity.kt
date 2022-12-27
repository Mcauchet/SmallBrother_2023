package com.projet.sluca.smallbrother.activities

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import io.ktor.client.*
import io.ktor.client.engine.android.*

/***
 * class Launch1Activity is the starting point of the application.
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 27-12-2022)
 */
class Launch1Activity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_launch1.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch1)

        val btnStart: Button = findViewById(R.id.btn_commencer)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        userData.configurePath(this)

        // Réactivation du SmsReceiver (en cas de coupure inopinée de l'appli).
        val pm = this@Launch1Activity.packageManager
        val componentName = ComponentName(this@Launch1Activity, SmsReceiver::class.java)
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )


        // Vérification : tout premier démarrage ?

        // Cas 1 : data existant : redirection vers l'écran de rôle.
        if (userData.loadData()) {
            when (userData.role) {
                "Aidant" -> {
                    //Activité aidant
                    val intent = Intent(this, AidantActivity::class.java)
                    startActivity(intent)
                }
                "Aidé" -> {
                    //Activité aidé
                    val intent = Intent(this, AideActivity::class.java)
                    startActivity(intent)
                }
            }
        } else if (userData.role != null) {
            // Désactivation des boutons retour (car suite de Reglages Activity).
            userData.canGoBack = false
            userData.refreshLog(2) // message de Log adéquat.
            when (userData.role) {
                "Aidant" -> {
                    //Installation aidant
                    val intent = Intent(this, InstallDantActivity::class.java)
                    startActivity(intent)
                }
                "Aidé" -> {
                    //Installation aidé
                    val intent = Intent(this, InstallDeActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            userData.canGoBack = true // activation des boutons retour.
            userData.refreshLog(1) // message de Log de commencement.
        }

        btnStart.setOnClickListener {
            vibreur.vibration(this, 100)

            // Transition vers l'activity suivante.
            val intent = Intent(this, Launch2Activity::class.java)
            startActivity(intent)
        }
    }
}