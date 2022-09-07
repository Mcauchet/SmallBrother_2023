package com.projet.sluca.smallbrother

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.telephony.SmsManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class AidantActivity : AppCompatActivity() {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userdata: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLog: TextView // Déclaration du TextView pour le Log.

    private var logHandler: Handler? = null // Handler pour rafraîchissement log.

    private lateinit var flTiers: FrameLayout // Déclaration du FrameLayout pour le bouton Tiers.


    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_aidant.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aidant)

        // Etablissement de la liaison avec la classe UserData.
        userdata = application as UserData

        // Liaison avec le TextView affichant le Log et ajout de sa valeur en cours.
        tvLog = findViewById(R.id.log_texte)

        // Liaison avec le FrameLayout affichant le bouton Tiers.
        flTiers = findViewById(R.id.contour4)

        // Lancement de l'activité en arrière-plan (rafraîchissement).
        logHandler = Handler()
        reloadLog.run()

        // Sortie de veille du téléphone et mise en avant-plan de cette appli.
        wakeup()
    }

    // --> Au clic que le bouton "Réduire".
    fun reduire(view: View?) {
        vibreur.vibration(this, 200)
        message(getString(R.string.message01)) // Message d'avertissement.
        moveTaskToBack(true) // Mise de l'appli en arrière-plan.
    }

    // --> Au clic que le bouton "Réglages".
    fun reglages(view: View?) {
        vibreur.vibration(this, 100)

        // Transition vers la ReglagesActivity.
        val intent = Intent(this, ReglagesActivity::class.java)
        startActivity(intent)
    }

    // --> Au clic que le bouton "Photo".
    fun photo(view: View?) {
        vibreur.vibration(this, 100)

        // Transition vers la ReglagesActivity.
        val intent = Intent(this, PhotoAide::class.java)
        startActivity(intent)
    }

    // --> Au clic que le bouton "SMS : Tout va bien ?".
    fun smsAidant(view: View?) {
        vibreur.vibration(this, 200)
        userdata.loadData() // Raptatriement des données de l'utilisateur.

        // Concoction et envoi du SMS.
        var sms = getString(R.string.smsys02)
        sms = sms.replace("§%", userdata.nom)
        SmsManager.getDefault().sendTextMessage(userdata.telephone, null, sms, null, null)
        message(getString(R.string.message04)) // toast de confirmation.
        userdata.refreshLog(4) // rafraîchissement du Log.
    }

    // --> Au clic que le bouton "Appel".
    fun appel(view: View?) {
        vibreur.vibration(this, 200)
        userdata.loadData() // Raptatriement des données de l'utilisateur.

        // Lancement de l'appel.
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:" + userdata.telephone)
        startActivity(callIntent)
        message(getString(R.string.message05)) // toast de confirmation.
        userdata.refreshLog(7) // rafraîchissement du Log.
    }

    // --> Au clic que le bouton "Demander un email d'urgence".
    fun urgence(view: View?) {
        vibreur.vibration(this, 330)

        // Demande de confirmation.
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.btn_urgence))
        builder.setMessage(getString(R.string.message02_texte))
        builder.setPositiveButton(
            getString(R.string.oui)
        ) { _, _ ->

            // Si choix = "OUI" :
            vibreur.vibration(this, 200)
            userdata.loadData() // Raptatriement des données de l'utilisateur.

            // Concoction et envoi du SMS.
            var sms = getString(R.string.smsys04)
            sms = sms.replace("§%", userdata.nom)
            SmsManager.getDefault()
                .sendTextMessage(userdata.telephone, null, sms, null, null)
            message(getString(R.string.message07)) // toast de confirmation.
            userdata.refreshLog(10) // rafraîchissement du Log.
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

    // --> Au clic que le bouton "Envoyer les infos à ...".
    fun tiers(view: View?) {
        vibreur.vibration(this, 200)
        userdata.loadData() // Raptatriement des données de l'utilisateur.

        // Préparation d'un email avec fichier joint.
        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        emailIntent.type = "plain/text"
        val listUri = ArrayList<Uri>()
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Ajout de la fiche de l'Aidé.
        val uri = Uri.fromFile(File(userdata.fichePath))
        listUri.add(uri)

        // Ajout de la photo de l'Aidé, s'il y en a une.
        val photoident = File(userdata.photoIdentPath)
        if (photoident.exists()) {
            val uri2 = Uri.fromFile(File(userdata.photoIdentPath))
            listUri.add(uri2)
        }

        // Appel du choix des services mail disponibles.
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listUri)
        startActivity(Intent.createChooser(emailIntent, "Quel service email utiliser ?"))
    }

    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    fun message(message: String?) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
        vibreur.vibration(this, 330)
    }

    // --> Rafraîchissement automatique toutes les 250 ms du TextView de Log et des boutons.
    private val reloadLog: Runnable = object : Runnable {
        override fun run() {
            // Log :
            if (userdata.log != null) {
                // Coloration en vert et mise en gras de la date (19 premiers caras).
                val sb = SpannableStringBuilder(userdata.log)
                val fcs = ForegroundColorSpan(Color.rgb(57, 114, 26))
                val bss = StyleSpan(Typeface.BOLD)
                sb.setSpan(fcs, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                sb.setSpan(bss, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                tvLog.text = sb // affichage
            }

            // Bouton Tiers :
            if (userdata.pleineFiche()) {
                flTiers.visibility = View.VISIBLE
            } else {
                flTiers.visibility = View.GONE
            }
            logHandler!!.postDelayed(this, 250) // rafraîchissement
        }
    }

    // --> WAKEUP() : Sortie de veille du téléphone et mise en avant-plan de cette appli.
    fun wakeup() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            (WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        )
    }

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    override fun onBackPressed() {
        moveTaskToBack(false)
    }
}