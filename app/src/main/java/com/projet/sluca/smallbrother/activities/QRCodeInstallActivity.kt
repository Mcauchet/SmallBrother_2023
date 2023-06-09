package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.SecurityUtils
import com.projet.sluca.smallbrother.utils.particule
import com.projet.sluca.smallbrother.utils.setAppBarTitle

/**
 * Manages the public key exchange between Aidé and Aidant through QR Code
 *
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 04-06-2023)
 */
class QRCodeInstallActivity : AppCompatActivity() {

    val vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_install)

        val ivQrCode: ImageView = findViewById(R.id.ivqrcode)
        val btnEnd: Button = findViewById(R.id.btn_terminer)
        val textQR: TextView = findViewById(R.id.textQR)

        userData = UserDataManager.getUserData(application)

        textQR.text = getString(R.string.installQR)
            .replace("§%", particule(userData.nomPartner) +userData.nomPartner)

        setAppBarTitle(userData, this)

        if(userData.role == "Aidant") qrEncoder(SecurityUtils.getEncPublicKey(), ivQrCode)
        else if(userData.role == "Aidé") qrEncoder(SecurityUtils.getSignPublicKey(), ivQrCode)

        btnEnd.setOnClickListener {
            btnEnd()
        }
    }

    /**
     * Manage clicks on the end button
     */
    private fun btnEnd() {
        vibreur.vibration(this, 100)
        if(userData.role == "Aidant") {
            val intent = Intent(this, QRCodeScannerInstallActivity::class.java)
            startActivity(intent)
        } else if(userData.role == "Aidé") {
            val intent = Intent(this, AideActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Generate the QR code with the public key
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
            val bitmap = qrEncoder.bitmap
            iv.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}