package com.projet.sluca.smallbrother.activities

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.redirectRole

/**
 * class Launch1Activity is the starting point of the application.
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 31-05-2023)
 */
class Launch1Activity : AppCompatActivity() {

    val vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch1)

        val btnStart: Button = findViewById(R.id.btn_commencer)

        userData = UserDataManager.getUserData(application)

        userData.configurePath(this)

        val packageManager = this@Launch1Activity.packageManager
        val componentName = ComponentName(this@Launch1Activity, SmsReceiver::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        checkFirstLaunch()

        btnStart.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, Launch2Activity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Checks if this is the first launch of the app (looks for existing data)
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 31-05-2023)
     */
    private fun checkFirstLaunch() {
        when {
            userData.installCompleted(this) -> redirectRole(this, userData)
            userData.role != null && userData.pubKey != "" -> {
                userData.canGoBack = false
                val intent = Intent(this, InstallActivity::class.java)
                startActivity(intent)
            }
            else -> {
                userData.canGoBack = true
                userData.refreshLog(1)
            }
        }
    }
}