package com.projet.sluca.smallbrother.activities

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SmsReceiver
import com.projet.sluca.smallbrother.SslSettings
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/***
 * class Launch1Activity is the starting point of the application.
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 01-11-2022)
 */
class Launch1Activity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_launch1.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch1)

        val btnStart: Button = findViewById(R.id.btn_commencer)

        //This code access the Ktor client successfully
        //the { engine {...} } part is for SSL, might change in time
        val client = HttpClient(Android) {
            engine {
                sslManager = { httpsUrlConnection ->
                    httpsUrlConnection.sslSocketFactory = SslSettings.getSslContext()?.socketFactory
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.get {
                    url("http://10.0.2.2:8080/aideData")
                }
                client.close()
                Log.d("BODY", response.body())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData
        vibreur.vibration(this, 100)

        userdata.configurePath(this)

        // Réactivation du SmsReceiver (en cas de coupure inopinée de l'appli).
        val pm = this@Launch1Activity.packageManager
        Log.d("PACKAGE MNG", pm.toString())
        val componentName = ComponentName(this@Launch1Activity, SmsReceiver::class.java)
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )


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

        btnStart.setOnClickListener {
            vibreur.vibration(this, 100)

            // Transition vers l'activity suivante.
            val intent = Intent(this, Launch2Activity::class.java)
            startActivity(intent)
        }
    }
}