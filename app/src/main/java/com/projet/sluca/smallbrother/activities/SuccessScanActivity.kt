package com.projet.sluca.smallbrother.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.UserDataManager
import com.projet.sluca.smallbrother.utils.setAppBarTitle

/**
 * Prompts instructions to the "Aidé" to make it clear the next QR code has to be shown to the Aidant
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 25-05-2023)
 */
class SuccessScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_scan)

        val userData = UserDataManager.getUserData(application)
        setAppBarTitle(userData, this)

        findViewById<TextView>(R.id.textPostScan).text =
            getString(R.string.scan_termin_avec_succ_s_appuyez_sur_afficher_qr_code_pour_continuer)
                .replace("§%", userData.nomPartner)

        val qrCodeButton = findViewById<Button>(R.id.btn_qrcode)

        qrCodeButton.setOnClickListener {
            val intent = Intent(this, QRCodeInstallActivity::class.java)
            startActivity(intent)
        }
    }
}