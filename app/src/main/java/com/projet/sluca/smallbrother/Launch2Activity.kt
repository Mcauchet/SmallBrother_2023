package com.projet.sluca.smallbrother

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Launch2Activity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata :UserData // Liaison avec les données globales de l'utilisateur.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_launch2.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch2)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData
    }

    // --> Au clic que le bouton "Aidant".
    fun aidant(view: View) {
        vibreur.vibration(this, 100)
        userdata.role = "Aidant"

        // Transition vers l'activity suivante.
        val intent = Intent(this, InstallDantActivity::class.java)
        startActivity(intent)
    }

    // --> Au clic que le bouton "Aidé".
    fun aide(view: View) {
        vibreur.vibration(this, 100)
        userdata.role = "Aidé"

        // Transition vers l'activity suivante.
        val intent = Intent(this, InstallDeActivity::class.java)
        startActivity(intent)
    }
}