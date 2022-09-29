package com.projet.sluca.smallbrother

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

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
            message(getString(R.string.message03A)) // toast de confirmation.

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
            SmsManager.getDefault()
                .sendTextMessage(userdata.telephone, null, sms, null, null)
            message(getString(R.string.message03B)) // toast de confirmation.
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

    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    fun message(message: String?) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
        vibreur.vibration(this, 330)
    }
}