package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData

/***
 * ReglagesActivity manages the resets of aide's and aidant's information and aide's picture
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 02-01-2023)
 */
class ReglagesActivity : AppCompatActivity() {

    var vibreur = Vibration()
    lateinit var userData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reglages)

        val btnResetAidant: Button = findViewById(R.id.btn_reinit_1)
        val btnResetPicture: Button = findViewById(R.id.btn_reinit_3)
        val btnBack: Button = findViewById(R.id.btn_retour)
        val btnHelp: Button = findViewById(R.id.btn_aide)

        userData = application as UserData

        btnResetAidant.setOnClickListener {
            vibreur.vibration(this, 330)

            // Ask for confirmation
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.message02_titre))
            builder.setMessage(getString(R.string.message02_settings))
            builder.setPositiveButton(getString(R.string.oui))
            { _, _ ->
                // Si choix = "OUI" :
                vibreur.vibration(this, 100)

                userData.loadData() // Load userData information

                // Send reset SMS to Aide
                var sms = getString(R.string.smsys01)
                sms = sms.replace("§%", userData.nom)
                sendSMS(this, sms, userData.telephone)

                //Delete Aidant's picture
                userData.deletePicture()

                // Delete Aidant's data
                userData.byeData()
                message(this, getString(R.string.message03A), vibreur)

                // Redémarrage de l'appli.
                val mIntent = Intent(this, Launch1Activity::class.java)
                startActivity(mIntent)
            }
            builder.setNegativeButton(android.R.string.cancel)
            { _, _ ->
                // Si choix = "ANNULER" :
                /* rien */
            }
            val dialog = builder.create()
            dialog.show()
        }

        btnResetPicture.setOnClickListener {
            vibreur.vibration(this, 100)

            // Changement d'activité.
            val intent = Intent(this, PicActivity::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            vibreur.vibration(this, 100)
            finish()
        }

        btnHelp.setOnClickListener {
            vibreur.vibration(this, 100)

            // Ouverture de l'aide.
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(userData.url + userData.help)
            )
            startActivity(browserIntent)
        }
    }
}