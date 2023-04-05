package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.message
import com.projet.sluca.smallbrother.utils.particule
import com.projet.sluca.smallbrother.utils.showPicture
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * PicActivity manages the re-take of a picture after installation process
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 04-04-2023)
 */
class PicActivity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var apercu: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic)

        val btnBack: Button = findViewById(R.id.btn_retour)
        val btnCapture: Button = findViewById(R.id.btn_capture)
        val btnSave: Button = findViewById(R.id.btn_save)
        val title: TextView = findViewById(R.id.reglages_intitule)

        userData = UserDataManager.getUserData(application)

        title.text = getString(R.string.btn_reinit_2)
            .replace("§%", particule(userData.nomPartner) +userData.nomPartner)

        apercu = findViewById(R.id.apercu)
        showPicture(apercu, userData)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
        }

        btnCapture.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("requestCode", 7) //see if needed
            startActivityForResult(intent, 7)
        }

        btnSave.setOnClickListener {
            vibreur.vibration(this, 100)
            message(this, getString(R.string.message09), vibreur)
            val intent = Intent(this, PhotoAideActivity::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 7 && resultCode == RESULT_OK) {
            val bitmap = data!!.extras!!["data"] as Bitmap?
            val image = userData.path + "/SmallBrother/photo_aide.jpg"
            try {
                bitmap!!.compress(CompressFormat.JPEG, 100, FileOutputStream(image))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            apercu.setImageBitmap(bitmap)
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}