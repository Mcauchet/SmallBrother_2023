package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData

/***
 * Launch2Activity class manages the role chosen for the device
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 04-01-2023)
 */
class Launch2Activity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData : UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch2)

        val btnRoleAidant: Button = findViewById(R.id.btn_role1)
        val btnRoleAide: Button = findViewById(R.id.btn_role2)

        userData = application as UserData

        btnRoleAidant.setOnClickListener {
            vibreur.vibration(this, 100)
            userData.role = "Aidant"

            val intent = Intent(this, InstallDantActivity::class.java)
            startActivity(intent)
        }

        btnRoleAide.setOnClickListener {
            vibreur.vibration(this, 100)
            userData.role = "Aidé"

            val intent = Intent(this, InstallDeActivity::class.java)
            startActivity(intent)
        }
    }
}