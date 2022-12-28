package com.projet.sluca.smallbrother.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.SecurityUtils
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.qrEncoder

/***
 * Manages the public key exchange between Aidé and Aidant through QR Code
 *
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 28-12-2022)
 */
class QRCodeInstallActivity : AppCompatActivity() {

    var vibreur = Vibration()

    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_install)

        val ivQrCode: ImageView = findViewById(R.id.ivqrcode)
        val btnEnd: Button = findViewById(R.id.btn_terminer)

        userData = application as UserData
        qrEncoder(SecurityUtils.getPublicKey(), ivQrCode)

        btnEnd.setOnClickListener {
            vibreur.vibration(this, 100)
            lateinit var intent: Intent
            if(userData.role == "Aidant") {
                intent = Intent(this, QRCodeScannerInstallActivity::class.java)
            } else if(userData.role == "Aidé") {
                intent = Intent(this, AideActivity::class.java)
            }
            startActivity(intent)
        }
    }
}