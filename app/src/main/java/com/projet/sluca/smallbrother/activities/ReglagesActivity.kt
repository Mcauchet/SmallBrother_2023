package com.projet.sluca.smallbrother.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.R
import com.projet.sluca.smallbrother.Vibration
import com.projet.sluca.smallbrother.message
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.sentPI

/***
 * ReglagesActivity manages the resets of aide's and/or aidant's information
 *
 * @author Sébastien Luca & Maxime Caucheteur
 * @version 1.2 (Updated on 07-11-22)
 */
class ReglagesActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_reglages.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reglages)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData
    }

    // --> Au clic que le bouton "Retour".
    fun retour(view: View?) {
        vibreur.vibration(this, 100)

        // Transition vers la AidantActivity.
        val intent = Intent(this, AidantActivity::class.java)
        startActivity(intent)
    }

    // --> Au clic que le bouton "Aide".
    fun aide(view: View?) {
        vibreur.vibration(this, 100)

        // Ouverture de l'aide.
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(userdata.url + userdata.help)
        )
        startActivity(browserIntent)
    }

    // --> Au clic que le bouton "btn_reinit_1".
    fun reinitAidant(view: View?) {
        vibreur.vibration(this, 330)

        // Demande de confirmation.
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.message02_titre))
        builder.setMessage(getString(R.string.message02_texte))
        builder.setPositiveButton(getString(R.string.oui))
        { _, _ ->
            // Si choix = "OUI" :
            vibreur.vibration(this, 100)
            userdata.byeData() // Suppression des données de l'utilisateur.
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

    // --> Au clic que le bouton "btn_reinit_2".
    fun reinitAide(view: View?) {
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
            userdata.loadData() // Raptatriement des données de l'utilisateur.

            // Concoction et envoi du SMS.
            var sms = getString(R.string.smsys01)
            sms = sms.replace("§%", userdata.nom)
            this.getSystemService(SmsManager::class.java)
                .sendTextMessage(userdata.telephone, null, sms, sentPI(this), null)
            message(this, getString(R.string.message03B), vibreur) // toast de confirmation.
            userdata.refreshLog(3) // message de Log adéquat.
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

    // --> Au clic que le bouton "btn_reinit_3".
    fun refairePhoto(view: View?) {
        vibreur.vibration(this, 200)

        // Changement d'activité.
        val mIntent = Intent(this, PicActivity::class.java)
        startActivity(mIntent)
    }
}