package com.projet.sluca.smallbrother.activities

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SecurityUtils
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData

/***
 * Manages the creation of the QR Codes for public key (aide's installation)
 * and files transfer to the police
 *
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 28-11-2022)
 */
class QRCodeActivity : AppCompatActivity() {

    var vibreur = Vibration()

    lateinit var bitmap: Bitmap

    lateinit var userdata: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        val ivQrCode: ImageView = findViewById(R.id.ivqrcode)
        val btnQrCodeAide: Button = findViewById(R.id.btn_qr_code)
        val btnQrCodePolice: Button = findViewById(R.id.btn_qr_code_police)
        val btnBack: Button = findViewById(R.id.btn_retour)
        val tvExplication: TextView = findViewById(R.id.QrCode_explication)

        userdata = application as UserData

        btnQrCodeAide.setOnClickListener {
            vibreur.vibration(this, 100)
            tvExplication.text = getString(R.string.explication_qr_aide)
            qrEncoder(SecurityUtils.getPublicKey(userdata.keyPair), ivQrCode)
        }

        btnQrCodePolice.setOnClickListener {
            vibreur.vibration(this, 100)
            tvExplication.text = getString(R.string.explication_qr_police)
            //TODO
            qrEncoder("This has to be implemented", ivQrCode)
        }

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
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