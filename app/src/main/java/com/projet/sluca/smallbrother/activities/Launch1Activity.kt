package com.projet.sluca.smallbrother.activities

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData

/***
 * class Launch1Activity is the starting point of the application.
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 03-01-2023)
 */
class Launch1Activity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch1)

        val btnStart: Button = findViewById(R.id.btn_commencer)

        userData = application as UserData

        userData.configurePath(this)

        // Activate SmsReceiver in case application crashes (as it's the launcher activity)
        val pm = this@Launch1Activity.packageManager
        val componentName = ComponentName(this@Launch1Activity, SmsReceiver::class.java)
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // Check if this is first launch of app
        // 1st case : data present in files, redirect to adequate activity
        if (userData.loadData()) {
            redirectRole(this, userData)
        } else if (userData.role != null) {
            // Deactivate back button (as previous activity is the settings (through data reset))
            userData.canGoBack = false
            userData.refreshLog(2)
            when (userData.role) {
                "Aidant" -> {
                    val intent = Intent(this, InstallDantActivity::class.java)
                    startActivity(intent)
                }
                "Aidé" -> {
                    val intent = Intent(this, InstallDeActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            userData.canGoBack = true
            userData.refreshLog(1)
        }

        btnStart.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, Launch2Activity::class.java)
            startActivity(intent)
        }
    }
}