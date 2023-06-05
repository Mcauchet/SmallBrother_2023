package com.projet.sluca.smallbrother.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.budiyev.android.codescanner.*
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.message
import com.projet.sluca.smallbrother.utils.particule
import com.projet.sluca.smallbrother.utils.setAppBarTitle

/**
 * Opens a qr code scanner to get the public key of the partner
 *
 * @author Maxime Caucheteur (with help of https://github.com/yuriy-budiyev/code-scanner)
 * @version 1.2 (Updated on 04-06-2023)
 */
class QRCodeScannerInstallActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner

    lateinit var userData: UserData
    val vibreur = Vibration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_scanner_install)

        userData = UserDataManager.getUserData(application)

        setAppBarTitle(userData, this)

        val textScan: TextView = findViewById(R.id.textScan)

        textScan.text = getString(R.string.installScan)
            .replace("§%", particule(userData.nomPartner) +userData.nomPartner)

        val scannerView: CodeScannerView = findViewById(R.id.qr_scanner)

        codeScanner = CodeScanner(this, scannerView)

        configureCodeScanner()

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                message(this, "Clé récupérée, vous pouvez continuer.", vibreur)
                userData.pubKey = it.text
                processScanResult()
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
     * Configure the code scanner with different options
     * @author Maxime Caucheteur (with help of https://github.com/yuriy-budiyev/code-scanner)
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun configureCodeScanner() {
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
    }

    /**
     * Save the public key and redirect based on user's role
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun processScanResult() {
        userData.saveData(this)
        userData.canGoBack = true
        lateinit var intent: Intent
        if (userData.role == "Aidé") {
            intent = Intent(this, SuccessScanActivity::class.java)
        } else if (userData.role == "Aidant") {
            intent = Intent(this, AidantActivity::class.java)
        }
        startActivity(intent)
    }
}