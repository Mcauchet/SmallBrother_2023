package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/***
 * class InstallDeActivity manages the data of the Aidant in the Aide's app
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 05-01-2023)
 */
class InstallActivity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userData = application as UserData
        check(userData.role == "Aidant" || userData.role == "Aidé")

        if(userData.role == "Aidé") setContentView(R.layout.activity_installde)
        else setContentView(R.layout.activity_installdant)

        val btnBack: Button = findViewById(R.id.btn_previous)
        val btnContinue: Button = findViewById(R.id.btn_continue)

        if (!userData.canGoBack) btnBack.visibility = View.INVISIBLE

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
        }

        btnContinue.setOnClickListener {
            vibreur.vibration(this, 100)
            nextStep()
        }

        requestPermissions()

        CoroutineScope(Dispatchers.IO).launch {
            if(userData.role == "Aidant") SecurityUtils.getEncryptionKeyPair()
            else SecurityUtils.getSignKeyPair()
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    /**
     * Checks the input, save them and start next activity
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun nextStep() {
        val etName = findViewById<EditText>(R.id.input_nom)
        val name = etName.text.toString()

        val etNamePartner = findViewById<EditText>(R.id.input_partner)
        val namePartner = etNamePartner.text.toString()

        val etTelephone = findViewById<EditText>(R.id.input_telephone)
        val telephone = etTelephone.text.toString()

        checkInputs(name, namePartner, telephone, this, userData, vibreur)
    }

    /**
     * Request the permissions if not already granted
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if(userData.role == "Aidé") getArrayOfPermissionsAide()
            else getArrayOfPermissionsAidant()
        }
    }

    /**
     * Request the array of permissions needed for the aidé
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun getArrayOfPermissionsAide() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.BROADCAST_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }

    /**
     * Request the array of permissions needed for the aidant
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun getArrayOfPermissionsAidant() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS
        ), 1)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}