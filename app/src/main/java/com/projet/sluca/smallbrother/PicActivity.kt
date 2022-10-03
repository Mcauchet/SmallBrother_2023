package com.projet.sluca.smallbrother

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

// --> Refaire la photo de l'Aidé.
/***
 * PicActivity manages the re-take of a picture after installation process
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 03-10-2022)
 */
class PicActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var apercu: ImageView // Instanciation de l'aperçu.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installdantpic.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData

        // Identification de l'aperçu.
        apercu = findViewById(R.id.apercu)

        // Par défaut : afficher la photo enregistrée, s'il y en a une.
        val fichier = userdata.path + "/SmallBrother/photo_aide.jpg"
        val file = File(fichier)
        if (file.exists()) apercu.setImageURI(Uri.fromFile(file))

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // --> Au clic que le bouton "Capture".
    fun capture(view:View) {
        vibreur.vibration(this, 100)

        // Lancement de l'activité de capture.
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 7)
    }

    // --> Au retour à la présente actvité, si une photo a été prise :
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 7 && resultCode == RESULT_OK) {
            val bitmap = data!!.extras!!["data"] as Bitmap? // Récupération de la photo

            // -> Sauvegarde de la photo.
            val image = userdata.path + "/SmallBrother/photo_aide.jpg" // chemin de fichier globalisé.
            try {
                bitmap!!.compress(CompressFormat.JPEG, 100, FileOutputStream(image))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            apercu.setImageBitmap(bitmap) // Affichage de la photo dans l'ImageView "aperçu".
        }
    }

    // --> Au clic que le bouton "Retour".
    fun retour(view: View?) {
        vibreur.vibration(this, 100)

        // Transition vers la AidantActivity.
        val intent = Intent(this, ReglagesActivity::class.java)
        startActivity(intent)
    }

    // --> Au clic que le bouton "Terminer".
    fun terminer(view: View?) {
        vibreur.vibration(this, 100)
        message(this, getString(R.string.message09), vibreur) // toast de confirmation.

        // Transition vers l'activity suivante.
        val intent = Intent(this, PhotoAide::class.java)
        startActivity(intent)
    }

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}