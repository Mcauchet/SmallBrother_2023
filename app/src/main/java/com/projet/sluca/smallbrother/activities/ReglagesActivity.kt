package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData

/***
 * ReglagesActivity manages the resets of aide's and/or aidant's information and aide's picture
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 15-12-22)
 */
class ReglagesActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_reglages.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reglages)

        val btnResetAidant: Button = findViewById(R.id.btn_reinit_1)
        val btnResetAide: Button = findViewById(R.id.btn_reinit_2)
        val btnResetPicture: Button = findViewById(R.id.btn_reinit_3)
        val btnBack: Button = findViewById(R.id.btn_retour)
        val btnHelp: Button = findViewById(R.id.btn_aide)
        val btnQRCode: Button = findViewById(R.id.btn_qr_code)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        btnResetAidant.setOnClickListener {
            vibreur.vibration(this, 330)

            // Demande de confirmation.
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.message02_titre))
            builder.setMessage(getString(R.string.message02_settings))
            builder.setPositiveButton(getString(R.string.oui))
            { _, _ ->
                // Si choix = "OUI" :
                vibreur.vibration(this, 100)
                userData.byeData() // Suppression des données de l'utilisateur.
                message(this, getString(R.string.message03A), vibreur) // toast de confirmation.

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

        btnResetAide.setOnClickListener {
            vibreur.vibration(this, 330)

            // Demande de confirmation.
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.message02_titre))
            builder.setMessage(getString(R.string.message02_texte))
            builder.setPositiveButton(getString(R.string.oui))
            { _, _ ->
                // Si choix = "OUI" :
                vibreur.vibration(this, 200)
                userData.loadData() // Raptatriement des données de l'utilisateur.

                // Concoction et envoi du SMS.
                var sms = getString(R.string.smsys01)
                sms = sms.replace("§%", userData.nom)
                this.getSystemService(SmsManager::class.java)
                    .sendTextMessage(userData.telephone, null, sms, sentPI(this), null)
                message(this, getString(R.string.message03B), vibreur) // toast de confirmation.
                userData.refreshLog(3) // message de Log adéquat.
            }
            builder.setNegativeButton(
                android.R.string.cancel
            ) { _, _ ->
                // Si choix = "ANNULER" :
                /* rien */
            }
            val dialog = builder.create()
            dialog.show()
        }

        btnResetPicture.setOnClickListener {
            vibreur.vibration(this, 200)

            // Changement d'activité.
            val mIntent = Intent(this, PicActivity::class.java)
            startActivity(mIntent)
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

        btnQRCode.setOnClickListener {
            vibreur.vibration(this, 100)

            val intent = Intent(this, QRCodeActivity::class.java)
            startActivity(intent)
        }
    }
}