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
 * @version 1.2 (Updated on 14-11-22)
 */
class Launch2Activity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData : UserData // Liaison avec les données globales de l'utilisateur.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_launch2.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch2)

        val btnRoleAidant: Button = findViewById(R.id.btn_role1)
        val btnRoleAide: Button = findViewById(R.id.btn_role2)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        btnRoleAidant.setOnClickListener {
            vibreur.vibration(this, 100)
            userData.role = "Aidant"

            // Transition vers l'activity suivante.
            val intent = Intent(this, InstallDantActivity::class.java)
            startActivity(intent)
        }

        btnRoleAide.setOnClickListener {
            vibreur.vibration(this, 100)
            userData.role = "Aidé"

            // Transition vers l'activity suivante.
            val intent = Intent(this, InstallDeActivity::class.java)
            startActivity(intent)
        }
    }
}