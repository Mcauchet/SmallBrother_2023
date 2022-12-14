package com.projet.sluca.smallbrother

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.projet.sluca.smallbrother.activities.AidantActivity
import com.projet.sluca.smallbrother.models.UserData
import java.io.File

/***
 * class PhotoAide allows the Aidant to show the picture of the Aide
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 04-12-2022)
 */
class PhotoAide : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.

    // Eléments d'affichage (photo et légende);
    private lateinit var ivApercu: ImageView
    private lateinit var tvLegende: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_photo.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        // Identification des éléments d'affichage.
        ivApercu = findViewById(R.id.apercu)
        tvLegende = findViewById(R.id.legende)
        val btnBack: Button = findViewById(R.id.btn_retour)

        // Gestion de l'affichage, selon qu'un fichier existe ou non.
        val path = userData.path + "/SmallBrother/photo_aide.jpg"
        val file = File(path)
        if(file.exists()) ivApercu.setImageURI(Uri.fromFile(file))
        else tvLegende.text = getString(R.string.nophoto)

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, AidantActivity::class.java)
            startActivity(intent)
        }
    }
}
