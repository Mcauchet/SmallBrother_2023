package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.*

/**
 * class AideActivity manages the actions available to the "Aidé".
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (updated on 04-06-2023)
 */
class AideActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    val vibreur = Vibration()
    lateinit var userData: UserData

    private lateinit var tvLog: TextView
    private lateinit var tvDelay: TextView
    private lateinit var tvIntituleDelay: TextView
    lateinit var btnPrivate: Switch
    private lateinit var ivLogo: ImageView

    private var logHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aide)

        val btnSmsAidant: Button = findViewById(R.id.btn_sms_va_dant)
        val btnCall: Button = findViewById(R.id.btn_appel)
        val btnEmergency: Button = findViewById(R.id.btn_urgence)

        userData = UserDataManager.getUserData(application)

        setAppBarTitle(userData, this)

        btnCall.text = getString(R.string.btn_appel).replace("§%", userData.nomPartner)

        tvDelay = findViewById(R.id.decompte)
        tvIntituleDelay = findViewById(R.id.intituleDecompte)

        btnPrivate = findViewById(R.id.btn_deranger)
        btnPrivate.setOnCheckedChangeListener(this)

        ivLogo = findViewById(R.id.logo)

        wakeup(window, this@AideActivity)

        refreshUI()

        tvLog = findViewById(R.id.log_texte)

        reloadLog.run()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        btnSmsAidant.setOnClickListener {
            btnSmsAidant()
        }

        btnCall.setOnClickListener {
            btnCall()
        }

        btnEmergency.setOnClickListener {
            btnEmergency()
        }
    }

    /**
     * Sends an SMS to the Aidant
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-06-2023)
     */
    private fun btnSmsAidant() {
        updateBitOnAction()
        vibreur.vibration(this, 100)
        val sms = getString(R.string.smsys03).replace("§%", userData.nom)
        if(sendSMS(this, sms, userData.telephone, vibreur)) {
            message(this, getString(R.string.message04), vibreur)
            userData.refreshLog(15)
        }
    }

    /**
     * Calls the Aidant
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-06-2023)
     */
    private fun btnCall() {
        updateBitOnAction()
        vibreur.vibration(this, 100)
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:" + userData.telephone)
        }
        startActivity(callIntent)
        message(this, getString(R.string.message05), vibreur)
        userData.refreshLog(7)
    }

    /**
     * Captures the context and send it to the server
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-06-2023)
     */
    private fun btnEmergency() {
        userData.prive = false
        userData.delay = 0
        changeSwitch()
        vibreur.vibration(this, 100)
        val sms = getString(R.string.smsys08).replace("§%", userData.nom)
        aideAskForHelp(sms)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (isChecked && !userData.prive) {
            val promptsView = LayoutInflater.from(this).inflate(R.layout.popup1, null)
            val alertDialogBuilder = AlertDialog.Builder(this).setView(promptsView)
            val input = promptsView.findViewById<View>(R.id.input_delai) as EditText
            configureAlertDialog(alertDialogBuilder, input)
            alertDialogBuilder.create().show()
        }
        if (!isChecked && userData.prive) {
            message(this, getString(R.string.message11), vibreur)
            userData.delay = 0
            updateAideInfo()
        }
    }

    /**
     * Manages when the aide presses the Emergency button
     * @param sms the message sent to the aidant
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 31-05-2023)
     */
    private fun aideAskForHelp(sms: String) {
        if(sendSMS(this, sms, userData.telephone, vibreur)) {
            if(isOnline(this)) {
                val workIntent = Intent(this, WorkActivity::class.java)
                    .putExtra("clef", "[#SB04]")
                startActivity(workIntent)
            } else {
                userData.refreshLog(22)
                warnAidantNoInternet(this, vibreur, userData)
            }
        }
    }

    /**
     * Configure the Alert Dialog window
     * @param [alertDialogBuilder] the Builder for the Alert Dialog
     * @param [input] the input zone for the delay
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun configureAlertDialog(alertDialogBuilder: AlertDialog.Builder, input: EditText) {
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton(getString(R.string.btn_valider)) { _, _ ->
                val privateTime = getPrivateModeDuration(input)
                val txtConfirm = getString(R.string.message10).replace("§%", privateTime.toString())
                message(this, txtConfirm, vibreur)
                userData.delay = privateTime * 60000
                updateAideInfo()
            }
            .setNegativeButton(getString(R.string.btn_annuler)) { dialog, _ ->
                changeSwitch()
                dialog.cancel()
            }
    }

    /**
     * Manage the UI when the switch is changed
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 28-03-2023)
     */
    private fun changeSwitch() {
        vibreur.vibration(this, 200)
        refreshUI()
    }

    /**
     * Update Aide info when changing the switch
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun updateAideInfo() {
        userData.prive = !userData.prive
        userData.bit = if(userData.bit==1) 0 else 1
        if (userData.bit == 1) userData.refreshLog(20)
        refreshUI()
        vibreur.vibration(this, 330)
    }

    /**
     * Gets the duration of private mode chosen by the Aide
     * @param [input] the EditText which contains the value
     * @return the duration as a Long
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 08-01-2023)
     */
    private fun getPrivateModeDuration(input: EditText): Long {
        var privateTime: Long = 1
        val maxPrivateTime: Long = 120
        if (input.text.toString().trim { it <= ' ' }.isNotEmpty()) {
            var valnum: String = input.text.toString()
            if (java.lang.Long.valueOf(valnum) == 0L) valnum = "1"
            privateTime = java.lang.Long.valueOf(valnum)
        }
        if (privateTime > maxPrivateTime) privateTime = maxPrivateTime
        return privateTime
    }

    /**
     * Refresh UI based on Private mode state
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun refreshUI() {
        if(userData.prive) {
            tvIntituleDelay.text = getString(R.string.intitule_delai)
            btnPrivate.setTextColor(Color.parseColor("#b30000"))
            ivLogo.setImageResource(R.drawable.logoff)
            btnPrivate.isChecked = true
        } else {
            btnPrivate.setTextColor(Color.parseColor("#597854"))
            ivLogo.setImageResource(R.drawable.logo2)
            btnPrivate.isChecked = false
            tvDelay.text = " "
            tvIntituleDelay.text = " "
        }
    }

    /**
     * Send an SMS to the Aidant to inform that Aide is in private mode and the time left
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun warnAidantOfPrivateMode() {
        var sms = getString(R.string.smsys07)
        sms = sms.replace("§%", userData.nom)
        val restencore: Int = ((userData.delay / 60000)+1).toInt()
        val waitage = restencore.toString()
        sms = sms.replace("N#", waitage)
        sendSMS(this@AideActivity, sms, userData.telephone, vibreur)
    }

    /**
     * Update status, TextView, wake the app when the private mode delay is over
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 08-01-2023)
     */
    private fun exitPrivateMode() {
        wakeup(window, this@AideActivity)
        userData.prive = false
        vibreur.vibration(this@AideActivity, 1000)
        val sound: MediaPlayer = MediaPlayer.create(this@AideActivity, R.raw.notification)
        sound.start()
        tvDelay.text = " "
        tvIntituleDelay.text = " "
        userData.refreshLog(18)
        val intent = Intent(this@AideActivity, AideActivity::class.java)
        startActivity(intent)
    }

    /**
     * Updates the private mode timer
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 16-01-2023)
     */
    private fun updatePrivateTimer() {
        val min = (userData.delay / 60000).toInt()
        val sec = (userData.delay / 1000).toInt() - min * 60
        var secSTG = sec.toString()
        if (sec < 10) secSTG = "0$secSTG"
        val txt = "$min\'$secSTG"
        tvDelay.text = txt
    }

    /**
     * Checks the timer of the private mode and updates accordingly
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 08-01-2023)
     */
    private fun updatePrivateMode() {
        if (!userData.prive) return
        userData.subDelay(250)
        if (userData.delay <= 0) exitPrivateMode()
        else updatePrivateTimer()
    }

    /**
     * Updates the log text when Private mode is on and an SMS or context capture is received
     * @param [bit] integer to know if it is an SMS, a context capture or something else
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun updateLogPrivate(bit: Int) {
        vibreur.vibration(this@AideActivity, 660)
        when (bit) {
            2 -> userData.refreshLog(6)
            3 -> userData.refreshLog(8)
            4 -> {
                userData.refreshLog(12)
                val sound: MediaPlayer = MediaPlayer.create(this@AideActivity, R.raw.notification)
                sound.start()
                warnAidantOfPrivateMode()
            }
        }
        tvLog.text = userData.log
        userData.bit = 1
    }

    /**
     * Update bit value based on actions taken by aidé
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-04-2023)
     */
    private fun updateBitOnAction() {
        if (userData.bit == 1) userData.bit = 1 else userData.bit = 0
    }

    private val reloadLog: Runnable = object : Runnable {
        override fun run() {
            if (userData.bit > 1)  updateLogPrivate(userData.bit) // Means private mode is ON
            if (userData.log != null) setLogAppearance(userData, tvLog)
            updatePrivateMode()
            // Avoid duplication of logHandler
            when (userData.esquive) {
                true -> userData.esquive = false
                false -> logHandler.postDelayed(this, 250)
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }
}