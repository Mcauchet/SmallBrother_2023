package com.projet.sluca.smallbrother.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.budiyev.android.codescanner.*
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.message
import com.projet.sluca.smallbrother.models.UserData

/***
 * Opens a qr code scanner to get the public key of the partner
 *
 * @author Maxime Caucheteur (with help of https://github.com/yuriy-budiyev/code-scanner)
 * @version 1.2 (Updated on 04-01-2023)
 */
class QRCodeScannerInstallActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner

    lateinit var userData: UserData

    var vibreur = Vibration()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner_install)

        userData = application as UserData

        if (userData.role == "Aidé") {
            fetchAideData()
        }

        val scannerView: CodeScannerView = findViewById(R.id.qr_scanner)

        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS

        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                message(this, "Scan result: ${it.text}", vibreur)
                userData.pubKey = it.text
                userData.saveData(this)
                userData.canGoBack = true
                lateinit var intent: Intent
                if (userData.role == "Aidé") {
                    intent = Intent(this, QRCodeInstallActivity::class.java)
                } else if (userData.role == "Aidant") {
                    intent = Intent(this, AidantActivity::class.java)
                }
                startActivity(intent)
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                message(this, "Camera initialization error: ${it.message}", vibreur)
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    /**
     * Gets the aide's data present in the intent extras.
     *
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun fetchAideData() {
        userData.nom = intent.getStringExtra("nom").toString()
        userData.nomPartner = intent.getStringExtra("nomPartner").toString()
        userData.telephone = intent.getStringExtra("telephone").toString()
        userData.version = intent.getStringExtra("version").toString()
    }
}