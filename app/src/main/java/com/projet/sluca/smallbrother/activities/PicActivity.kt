package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.PhotoAide
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.message
import com.projet.sluca.smallbrother.models.UserData
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

/***
 * PicActivity manages the re-take of a picture after installation process
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 27-12-2022)
 */
class PicActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var apercu: ImageView // Instanciation de l'aperçu.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installdantpic.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic)

        val btnBack: Button = findViewById(R.id.btn_retour)
        val btnCapture: Button = findViewById(R.id.btn_capture)
        val btnSave: Button = findViewById(R.id.btn_save)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        // Identification de l'aperçu.
        apercu = findViewById(R.id.apercu)

        // Par défaut : afficher la photo enregistrée, s'il y en a une.
        val path = userData.path + "/SmallBrother/photo_aide.jpg"
        val file = File(path)
        if (file.exists()) apercu.setImageURI(Uri.fromFile(file))

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)

            // Transition vers la AidantActivity.
            val intent = Intent(this, ReglagesActivity::class.java)
            startActivity(intent)
        }

        btnCapture.setOnClickListener {
            vibreur.vibration(this, 100)

            // Lancement de l'activité de capture.
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("requestCode", 7) //see if needed
            startActivityForResult(intent, 7)
        }

        btnSave.setOnClickListener {
            vibreur.vibration(this, 100)
            // toast de confirmation.
            message(this, getString(R.string.message09), vibreur)

            // Transition vers l'activity suivante.
            val intent = Intent(this, PhotoAide::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("result code", resultCode.toString())
        if (requestCode == 7 && resultCode == RESULT_OK) {
            val bitmap = data!!.extras!!["data"] as Bitmap? // Récupération de la photo

            // -> Sauvegarde de la photo.
            val image =
                userData.path + "/SmallBrother/photo_aide.jpg" // chemin de fichier globalisé.
            try {
                bitmap!!.compress(CompressFormat.JPEG, 100, FileOutputStream(image))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            apercu.setImageBitmap(bitmap) // Affichage de la photo dans l'ImageView "aperçu".
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}