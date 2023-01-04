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
 * class InstallDantActivity manages the installation for the aidant
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 04-01-2023)
 */
class InstallDantActivity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installdant)

        val btnBack: Button = findViewById(R.id.btn_previous)
        val btnContinue: Button = findViewById(R.id.btn_continue)

        userData = application as UserData

        if (!userData.canGoBack) {
            btnBack.visibility = View.INVISIBLE
        }

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
            SecurityUtils.getKeyPair()
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

        val etNamePartner = findViewById<EditText>(R.id.input_nom_Aide)
        val namePartner = etNamePartner.text.toString()

        val etTelephone = findViewById<EditText>(R.id.input_telephone)
        val telephone = etTelephone.text.toString()

        checkInputs(name, namePartner, telephone)
    }

    /**
     * Checks if inputs are valid
     * @param [name] the name of the aidant
     * @param [namePartner] the name of the aidé
     * @param [telephone] the phone number of the aidé
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun checkInputs(name: String, namePartner: String, telephone: String) {
        when {
            telephone.length > 10 || !telephone.matches("".toRegex()) && !telephone.startsWith("04")
            -> message(this, getString(R.string.error01), vibreur)

            name.matches("".toRegex()) || telephone.matches("".toRegex())
            -> message(this, getString(R.string.error03), vibreur)

            namePartner.matches("".toRegex()) || telephone.matches("".toRegex())
            -> message(this, getString(R.string.error03), vibreur)

            else -> registerData(name, namePartner, telephone, userData, this)
        }
    }

    /**
     * Request the permissions if not already granted
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            getArrayOfPermissions()
        }
    }

    /**
     * Request the array of permissions needed for the aidant
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun getArrayOfPermissions() {
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
            ), 1
        )
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}