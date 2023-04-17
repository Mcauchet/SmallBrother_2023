package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.message
import com.projet.sluca.smallbrother.utils.particule
import com.projet.sluca.smallbrother.utils.sendSMS
import com.projet.sluca.smallbrother.utils.setAppBarTitle

/**
 * SettingsActivity manages the resets of aide's and aidant's information and aide's picture
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 16-04-2023)
 */
class SettingsActivity : AppCompatActivity() {

    val vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reglages)

        val btnResetAidant: Button = findViewById(R.id.btn_reinit_1)
        val btnResetPicture: Button = findViewById(R.id.btn_reinit_2)
        val btnBack: Button = findViewById(R.id.btn_retour)
        val btnHelp: Button = findViewById(R.id.btn_aide)

        userData = UserDataManager.getUserData(application)

        setAppBarTitle(userData, this)

        btnResetPicture.text = getString(R.string.btn_reinit_2)
            .replace("§%", particule(userData.nomPartner) +userData.nomPartner)

        btnResetAidant.setOnClickListener {
            vibreur.vibration(this, 330)
            val builder = AlertDialog.Builder(this)
            configureAlertDialog(builder)
            builder.create().show()
        }

        btnResetPicture.setOnClickListener {
            vibreur.vibration(this, 100)
            val intent = Intent(this, PicActivity::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
        }

        //TODO Do we keep this page available ? Then we need to update it but I have to contact
        // Sebastien for that
        btnHelp.setOnClickListener {
            vibreur.vibration(this, 100)
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(userData.url + userData.help)
            )
            startActivity(browserIntent)
        }
    }

    /**
     * Configure the Alert Dialog
     * @param [builder] the AlertDialog Builder
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun configureAlertDialog(builder: AlertDialog.Builder) {
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.message02_titre))
        builder.setMessage(getString(R.string.message02_settings))
        configureAlertDialogButtons(builder)
    }

    /**
     * Configure the Alert Dialog buttons
     * @param [builder] the AlertDialog Builder
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun configureAlertDialogButtons(builder: AlertDialog.Builder) {
        builder.setPositiveButton(getString(R.string.oui)) { _, _ ->
            vibreur.vibration(this, 100)
            val sms = getString(R.string.smsys01).replace("§%", userData.nom)
            sendSMS(this, sms, userData.telephone, vibreur)
            userData.deletePicture()
            userData.byeData("donnees.txt")
            message(this, getString(R.string.message03A), vibreur)
            val mIntent = Intent(this, Launch1Activity::class.java)
            startActivity(mIntent)
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            /* window closes */
        }
    }
}