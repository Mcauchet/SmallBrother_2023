package com.projet.sluca.smallbrother.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.SecurityUtils
import com.projet.sluca.smallbrother.utils.getAppVersion
import com.projet.sluca.smallbrother.utils.message
import com.projet.sluca.smallbrother.utils.setAppBarTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * class InstallActivity manages the data of the Aidant in the Aide's app
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 11-04-2023)
 */
class InstallActivity : AppCompatActivity() {

    val vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userData = UserDataManager.getUserData(application)

        setAppBarTitle(userData, this)

        if(userData.role != "Aidé" && userData.role != "Aidant") finish()

        when(userData.role) {
            "Aidé" -> setContentView(R.layout.activity_installde)
            "Aidant" -> setContentView(R.layout.activity_installdant)
        }

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
     * Checks if inputs are valid
     * @param [name] self name
     * @param [namePartner] the name of the partner
     * @param [telephone] the phone number of the partner
     * @param [context] the Context of the application
     * @param [userData] the user's data
     * @param [vibreur] the phone Vibration system
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun checkInputs(name: String, namePartner: String, telephone: String, context: Context,
                    userData: UserData, vibreur: Vibration) {
        when {
            telephone.length > 10 || telephone.matches("".toRegex()) || !telephone.startsWith("04")
            -> message(context, context.getString(R.string.error01), vibreur)
            name.matches("".toRegex()) || telephone.matches("".toRegex())
                    || namePartner.matches("".toRegex())
            -> message(context, context.getString(R.string.error03), vibreur)
            else -> registerData(name, namePartner, telephone, userData, context)
        }
    }

    /**
     * Save the user's data in a file
     * @param [name] the name of the user
     * @param [namePartner] the name of the partner
     * @param [telephone] the phone number of the partner
     * @param [userData] the user's data
     * @param [context] the Context of the application
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun registerData(name: String, namePartner: String, telephone: String, userData: UserData,
                             context: Context
    ) {
        require(name.isNotBlank() && telephone.isNotBlank() && namePartner.isNotBlank())
        userData.version = getAppVersion(context)
        userData.nom = name
        userData.nomPartner = namePartner
        userData.telephone = telephone
        userData.saveData(context)
        redirectAfterRegister(userData, context)
    }

    /**
     * Redirects the user after registering his datas
     * @param [userData] the user's data
     * @param [context] the Context of the application
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun redirectAfterRegister(userData: UserData, context: Context) {
        check(userData.role == "Aidant" || userData.role == "Aidé")
        if (userData.role == "Aidant") {
            val intent = Intent(context, InstallDantPicActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        } else {
            val intent = Intent(context, QRCodeScannerInstallActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    /**
     * Request the permissions if not already granted
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 26-01-2023)
     */
    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if(userData.role == "Aidé") {
                getArrayOfPermissionsAide()
                getSpecialPermission()
            }
            else getArrayOfPermissionsAidant()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if((grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED))
                requestPermissions()
        }
    }

    /**
     * Request the array of permissions needed for the aidé
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 05-04-2023)
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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }

    /**
     * Target specific permissions according to the API version
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 26-02-2023)
     */
    private fun getSpecialPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_PHONE_NUMBERS
            ), 1)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1)
        }
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