package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SecurityUtils
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData

/***
 * Manages the public key exchange between Aidant & Aidé
 *
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 14-12-2022)
 */
class InstallDant2Activity : AppCompatActivity() {

    var vibreur = Vibration()

    lateinit var bitmap: Bitmap

    lateinit var userdata: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install_dant2)

        val ivQrCode: ImageView = findViewById(R.id.ivqrcode)
        val btnEnd: Button = findViewById(R.id.btn_terminer)

        userdata = application as UserData
        qrEncoder(SecurityUtils.getPublicKey(), ivQrCode)

        btnEnd.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, AidantActivity::class.java)
            startActivity(intent)
        }
    }

    /***
     * Generate the QR code with the public key or the access to aide's files
     *
     * @param [msg] the message in the QR Code
     * @param [iv] the ImageView where the QR Code is shown
     *
     * @author androidmads (See github:https://github.com/androidmads/QRGenerator)
     */
    private fun qrEncoder(msg: String?, iv: ImageView) {
        val qrEncoder = QRGEncoder(msg, null, QRGContents.Type.TEXT, 400)

        qrEncoder.colorBlack = Color.LTGRAY
        qrEncoder.colorWhite = Color.BLACK

        try {
            bitmap = qrEncoder.bitmap
            iv.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}