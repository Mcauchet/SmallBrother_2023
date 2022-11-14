package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

/***
 * class InstallDantPicActivity manages the capture of the aidé picture
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (updated on 14-11-2022)
 */
class InstallDantPicActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var apercu : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installdantpic.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installdantpic)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        val btnCapture: Button = findViewById(R.id.btn_capture)
        val btnBack: Button = findViewById(R.id.btn_previous)
        val btnEnd: Button = findViewById(R.id.btn_terminer)

        // Identification de l'aperçu.
        apercu = findViewById(R.id.apercu)

        // Par défaut : afficher la photo enregistrée, s'il y en a une.
        val fichier = userData.path + "/SmallBrother/photo_aide.jpg"
        val file = File(fichier)
        if (file.exists()) apercu.setImageURI(Uri.fromFile(file))

        btnCapture.setOnClickListener {
            vibreur.vibration(this, 100)

            // Lancement de l'activité de capture.
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 7)
        }

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
        }

        btnEnd.setOnClickListener {
            vibreur.vibration(this, 100)
            // Rétablissement des boutons retour, au cas où désactivé par ReglagesActivity.
            userData.canGoBack = true
            // Transition vers l'activity suivante.
            val intent = Intent(this, AidantActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // --> Au retour à la présente actvité, si une photo a été prise :
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 7 && resultCode == RESULT_OK) {
            val bitmap = data!!.extras!!["data"] as Bitmap? // Récupération de la photo

            // -> Sauvegarde de la photo.
            val image = userData.path + "/SmallBrother/photo_aide.jpg" // chemin de fichier globalisé.
            try {
                bitmap!!.compress(CompressFormat.JPEG, 100, FileOutputStream(image))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            apercu.setImageBitmap(bitmap) // Affichage de la photo dans l'ImageView "aperçu".
        }
    }

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}