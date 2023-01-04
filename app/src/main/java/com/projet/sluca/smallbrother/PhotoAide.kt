package com.projet.sluca.smallbrother

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.models.UserData

/***
 * class PhotoAide allows the Aidant to show the picture of the Aide
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 04-01-2023)
 */
class PhotoAide : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData

    private lateinit var ivApercu: ImageView
    private lateinit var tvLegende: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        userData = application as UserData

        ivApercu = findViewById(R.id.apercu)
        tvLegende = findViewById(R.id.legende)
        val btnBack: Button = findViewById(R.id.btn_retour)

        showPicture(ivApercu, userData)

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, AidantActivity::class.java)
            startActivity(intent)
        }
    }
}
