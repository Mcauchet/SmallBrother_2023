package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.UserDataManager
import com.projet.sluca.smallbrother.Vibration

/***
 * Launch2Activity class manages the role chosen for the device
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 11-04-2023)
 */
class Launch2Activity : AppCompatActivity() {

    val vibreur = Vibration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch2)

        val btnRoleAidant: Button = findViewById(R.id.btn_role1)
        val btnRoleAide: Button = findViewById(R.id.btn_role2)

        val userData = UserDataManager.getUserData(application)

        btnRoleAidant.setOnClickListener {
            userData.role = "Aidant"
            nextActivity()
        }

        btnRoleAide.setOnClickListener {
            userData.role = "Aidé"
            nextActivity()
        }
    }

    /**
     * Goes to next activity for the install
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun nextActivity() {
        vibreur.vibration(this, 100)
        val intent = Intent(this, InstallActivity::class.java)
        startActivity(intent)
    }
}