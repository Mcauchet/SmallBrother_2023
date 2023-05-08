package com.projet.sluca.smallbrother.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.UserDataManager
import com.projet.sluca.smallbrother.utils.setAppBarTitle

/**
 * Prompts instructions to the "Aidé" to make it clear the next QR code has to be shown to the aidant
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 06-05-2023)
 */
class SuccessScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_scan)

        val userData = UserDataManager.getUserData(application)

        setAppBarTitle(userData, this)

        val qrCodeButton = findViewById<Button>(R.id.btn_qrcode)

        qrCodeButton.setOnClickListener {
            val intent = Intent(this, QRCodeInstallActivity::class.java)
            startActivity(intent)
        }
    }
}