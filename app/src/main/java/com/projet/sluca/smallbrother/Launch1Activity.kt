package com.projet.sluca.smallbrother

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/***
 * class Launch1Activity is the starting point of the application.
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 10-10-2022)
 */
class Launch1Activity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_launch1.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch1)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData
        vibreur.vibration(this, 100)

        userdata.configurePath(this)

        // Réactivation du SmsReceiver (en cas de coupure inopinée de l'appli).
        //Without the comment, the app doesn't launch because "SmsReceiver component does not exist"
        /*val pm = this@Launch1Activity.packageManager
        val componentName = ComponentName(this@Launch1Activity, SmsReceiver::class.java)
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

         */

        // Vérification : tout premier démarrage ?

        // Cas 1 : data existant : redirection vers l'écran de rôle.
        if (userdata.loadData()) {
            when (userdata.role) {
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
        } else if (userdata.role != null) {
            // Désactivation des boutons retour (car suite de Reglages Activity).
            userdata.canGoBack = false
            userdata.refreshLog(2) // message de Log adéquat.
            when (userdata.role) {
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
            userdata.canGoBack = true // activation des boutons retour.
            userdata.refreshLog(1) // message de Log de commencement.
        }
    }

    // --> Au clic du bouton "Commencer".
    fun commencer(view: View) {
        vibreur.vibration(this, 100)

        // Transition vers l'activity suivante.
        val intent = Intent(this, Launch2Activity::class.java)
        startActivity(intent)
    }
}