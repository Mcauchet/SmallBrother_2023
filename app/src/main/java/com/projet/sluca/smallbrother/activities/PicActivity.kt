package com.projet.sluca.smallbrother.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.PhotoAide
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.message
import com.projet.sluca.smallbrother.models.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

// --> Refaire la photo de l'Aidé.
/***
 * PicActivity manages the re-take of a picture after installation process
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 17-11-2022)
 */
class PicActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var apercu: ImageView // Instanciation de l'aperçu.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_installdantpic.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic)

        val btnBack: Button = findViewById(R.id.btn_retour)
        val btnCapture: Button = findViewById(R.id.btn_capture)
        val btnSave: Button = findViewById(R.id.btn_save)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData

        // Identification de l'aperçu.
        apercu = findViewById(R.id.apercu)

        // Par défaut : afficher la photo enregistrée, s'il y en a une.
        val fichier = userdata.path + "/SmallBrother/photo_aide.jpg"
        val file = File(fichier)
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
            val intent = Intent(this@PicActivity, MediaStore.ACTION_IMAGE_CAPTURE::class.java)
            getResult.launch(intent)
        }

        btnSave.setOnClickListener {
            vibreur.vibration(this, 100)
            message(this, getString(R.string.message09), vibreur) // toast de confirmation.

            // Transition vers l'activity suivante.
            val intent = Intent(this, PhotoAide::class.java)
            startActivity(intent)
        }
    }

    private val getResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bitmap = it.data?.extras?.get("data") as Bitmap

            val image = userdata.path + "/SmallBrother/photo_aide.jpg"
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        bitmap.compress(CompressFormat.JPEG, 100, FileOutputStream(image))
                    }
                }
            } catch (e:FileNotFoundException) {
                e.printStackTrace()
            }
            apercu.setImageBitmap(bitmap)
        }
    }

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}