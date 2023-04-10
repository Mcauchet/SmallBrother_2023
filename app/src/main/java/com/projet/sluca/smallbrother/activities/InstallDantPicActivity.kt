package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.setAppBarTitle
import com.projet.sluca.smallbrother.utils.showPicture
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

/**
 * class InstallDantPicActivity manages the capture of the aidé picture
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 16-01-2023)
 */
class InstallDantPicActivity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var apercu : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installdantpic)

        userData = UserDataManager.getUserData(application)

       setAppBarTitle(userData, this)

        val btnCapture: Button = findViewById(R.id.btn_capture)
        val btnBack: Button = findViewById(R.id.btn_previous)
        val btnEnd: Button = findViewById(R.id.btn_continue)

        apercu = findViewById(R.id.apercu)
        try {
            showPicture(apercu, userData)
        } catch (e: java.lang.IllegalStateException) {
            userData.configurePath(this)
            recreate()
        }

        btnCapture.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("requestCode", 7)
            assert(intent.hasExtra("requestCode"))
            startActivityForResult(intent, 7)
        }

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
        }

        btnEnd.setOnClickListener {
            vibreur.vibration(this, 100)
            userData.canGoBack = true
            val intent = Intent(this, QRCodeInstallActivity::class.java)
            startActivity(intent)
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("result code", resultCode.toString())
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