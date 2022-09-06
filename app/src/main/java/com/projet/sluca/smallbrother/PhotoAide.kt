package com.projet.sluca.smallbrother

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import java.io.File

// --> Affichage de la photo de l'Aidé.
class PhotoAide : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    var userdata = UserData() // Liaison avec les données globales de l'utilisateur.

    // Eléments d'affichage (photo et légende);
    private lateinit var ivApercu: ImageView
    private lateinit var tvLegende: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_photo.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData

        // Identification des éléments d'affichage.
        ivApercu = findViewById(R.id.apercu)
        tvLegende = findViewById(R.id.legende)

        // Gestion de l'affichage, selon qu'un fichier existe ou non.
        val fichier = userdata.photoIdentPath
        val file = File(fichier)
        if (file.exists()) ivApercu.setImageURI(Uri.fromFile(file))
        else tvLegende.text = getString(R.string.nophoto)
    }

    // --> Au clic que le bouton "Retour".
    fun retour() {
        vibreur.vibration(this, 100)

        // Transition vers la AidantActivity.
        val intent = Intent(this, AidantActivity::class.java)
        startActivityForResult(intent, 1)
    }
}
