package com.projet.sluca.smallbrother

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AideActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLog: TextView // Déclaration du TextView pour le Log.

    private lateinit var tvDelai: TextView // Déclaration du TextView pour le délai.

    private lateinit var tvIntituleDelai: TextView // Déclaration du TextView pour l'intitulé du délai.

    //TODO Look up SwitchCompat
    //https://developer.android.com/reference/androidx/appcompat/widget/SwitchCompat
    private var btnDeranger: Switch? = null // Déclaration du bouton ON/OFF.

    private lateinit var ivLogo: ImageView // Déclaration de l'ImageView du logo.

    private var logHandler: Handler? = null // Handler pour rafraîchissement log.


    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_aide.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aide)

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData
        userData.loadData()

        // Liaison avec les TextViews du délai.
        tvDelai = findViewById(R.id.decompte)
        tvIntituleDelai = findViewById(R.id.intituleDecompte)

        // Liaison avec le switch ON/OFF et écoute de son état.
        btnDeranger = findViewById(R.id.btn_deranger)
        btnDeranger!!.setOnCheckedChangeListener(this)

        // Liaison avec l'ImageView du logo.
        ivLogo = findViewById(R.id.logo)

        // Sortie de veille du téléphone et mise en avant-plan de cette appli.
        wakeup()

        // Rafraîchissement de l'affichage.
        refresh()

        // Réinitialisation de l'indicateur "Bit".
        SmsReceiver.bit = 0

        // Liaison avec le TextView affichant le Log et ajout de sa valeur en cours.
        tvLog = findViewById(R.id.log_texte)

        // Lancement de l'activité en arrière-plan (décompte) en évitant les doublons.
        /*
        if(this.logHandler == null ) // && !userdata.getEsquive())
        {
            this.logHandler = new Handler();
            reloadLog.run();
        }
        */
        logHandler = Handler()
        reloadLog.run()
    }

    // --> Au clic que le bouton "Aide".
    fun aide(view: View?) {
        vibreur.vibration(this, 100)

        // Ouverture de l'aide.
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(userData.url + userData.help)
        )
        startActivity(browserIntent)
    }

    // --> Au clic que le bouton "Réduire".
    fun reduire(view: View?) {
        vibreur.vibration(this, 200)
        message(getString(R.string.message01)) // Message d'avertissement.
        moveTaskToBack(true) // Mise de l'appli en arrière-plan.
    }

    // --> Au clic que le bouton "SMS : Tout va bien ?".
    fun smsAide(view: View?) {
        vibreur.vibration(this, 200)
        userData.loadData() // Raptatriement des données de l'utilisateur.

        // Concoction et envoi du SMS.
        var sms = getString(R.string.smsys03)
        sms = sms.replace("§%", userData.nom)
        SmsManager.getDefault().sendTextMessage(userData.telephone, null, sms, null, null)
        message(getString(R.string.message04)) // toast de confirmation.
        userData.refreshLog(16) // rafraîchissement du Log.
    }

    // --> Au clic que le bouton "Appel".
    fun appel(view: View?) {
        vibreur.vibration(this, 200)
        userData.loadData() // Raptatriement des données de l'utilisateur.

        // Balance contre l'interférence de l'Intent ci-dessous dans l'équilibre Work-Aide activity.
        //userdata.setEsquive(false);

        // Lancement de l'appel.
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:" + userData.telephone)
        startActivity(callIntent)
        message(getString(R.string.message05)) // toast de confirmation.
        userData.refreshLog(7) // rafraîchissement du Log.
    }

    // --> Traitement des postions ON/OFF du bouton "Mode Privé".
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (isChecked && !userData.prive) // Si "Mode Privé" demandé.
        {
            vibreur.vibration(this, 200)

            // Instanciaition d'une boîte de dialogue.
            val li = LayoutInflater.from(this)
            val promptsView = li.inflate(R.layout.popup1, null)
            val alertDialogBuilder = AlertDialog.Builder(this)

            // Appel du layout "popup1" :
            alertDialogBuilder.setView(promptsView)

            // Récupération du contenu de l'input.
            val input = promptsView.findViewById<View>(R.id.input_delai) as EditText

            // Affichage de la boîte de dialogue.
            alertDialogBuilder
                .setCancelable(false) // Si "Valider" :
                .setPositiveButton(
                    getString(R.string.btn_valider)
                ) { _, _ -> // Récupération du délai entré + sécurité si vaut null ou 0.
                    val valinput: Editable
                    var valeur: Long = 1
                    if (input.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                        valinput = input.text
                        var valnum: String = valinput.toString()
                        if (java.lang.Long.valueOf(valnum) == 0L) valnum = "1"
                        valeur = java.lang.Long.valueOf(valnum)
                    }

                    // Délai max imposé.
                    if (valeur > 120) valeur = 120

                    // Création du toast de confirmation.
                    var duree = "$valeur minute"
                    if (valeur > 1) duree += "s"
                    val biscotte = getString(R.string.message10).replace(
                        "§%",
                        (duree)
                    )
                    message(biscotte)

                    // Détermination du délai.
                    valeur *= 60000
                    userData.delai = valeur

                    // Passage en Mode Privé.
                    userData.prive = true
                    SmsReceiver.bit = 1 // Cookie : Mode Privé ON.
                    refresh()
                    vibreur.vibration(this, 330)
                } // Si "Annuler" :
                .setNegativeButton(
                    getString(R.string.btn_annuler)
                ) { dialog, _ ->
                    vibreur.vibration(this, 100)
                    changeSwitch() // Changer la position du bouton.
                    dialog.cancel()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
        if (!isChecked && userData.prive) // Si arrêt du "Mode Privé".
        {
            message(getString(R.string.message11)) // Toast de confirmation.
            userData.prive = false // Arrêt du Mode Privé.
            SmsReceiver.bit = 0 // Cookie : Mode Privé OFF.
            userData.delai = 0
            refresh()
            vibreur.vibration(this, 330)
        }
    }

    // -->
    fun changeSwitch() {
        vibreur.vibration(this, 250)
        refresh()
    }

    // --> Rafraîchissement de l'affichage en fonction de l'état du Mode Privé.
    fun refresh() {
        if (userData.prive) // Si actif.
        {
            btnDeranger!!.setTextColor(Color.parseColor("#b30000"))
            ivLogo.setImageResource(R.drawable.logoff)
            btnDeranger!!.isChecked = true
        } else  // Si inactif.
        {
            btnDeranger!!.setTextColor(Color.parseColor("#597854"))
            ivLogo.setImageResource(R.drawable.logo2)
            btnDeranger!!.isChecked = false

            // Retrait du décompte.
            tvDelai.text = " "
            tvIntituleDelai.text = " "
        }
    }

    // --> Rafraîchissement automatique du TextView de Log toutes les 250 ms.
    private val reloadLog: Runnable = object : Runnable {
        override fun run() {
            // -> Vérification des actions parallèles (appels et sms reçus) :
            val bit: Int = SmsReceiver.bit
            if (bit > 1) {
                vibreur.vibration(this@AideActivity, 660)
                if (bit == 2) // Cas : SMS "tout va bien" reçu.
                {
                    userData.refreshLog(6) // message de Log adéquat.
                }
                if (bit == 3) // Cas : appel reçu.
                {
                    userData.refreshLog(8) // message de Log adéquat.
                }
                if (bit == 4) // Cas : demande d'email d'urgence.
                {
                    userData.refreshLog(12) // message de Log adéquat.

                    // Sonnerie de notification.
                    val sound: MediaPlayer = MediaPlayer
                        .create(this@AideActivity, R.raw.notification)
                    sound.start()

                    // L'Aidant est averti par SMS de l'action du Mode Privé (+ reçoit tmp restant).
                    var sms = getString(R.string.smsys07)
                    sms = sms.replace("§%", userData.nom)
                    val restencore: Int = ((userData.delai / 60000)+1).toInt()
                    val waitage = restencore.toString()
                    sms = sms.replace("N#", waitage)
                    SmsManager.getDefault()
                        .sendTextMessage(userData.telephone, null, sms, null, null)
                }
                tvLog.text = userData.log // affichage.
                SmsReceiver.bit = 1 // retour à décompte normal.
            }

            // -> Gestion du Log :
            if (userData.log != null) {
                // Coloration en vert et mise en gras de la date (19 premiers caras).
                val sb = SpannableStringBuilder(userData.log)
                val fcs = ForegroundColorSpan(Color.rgb(57, 114, 26))
                val bss = StyleSpan(Typeface.BOLD)
                sb.setSpan(fcs, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                sb.setSpan(bss, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                tvLog.text = sb // affichage
            }

            // -> Gestion du délai du Mode Privé :
            if (userData.prive) // Si le Mode Privé est actif.
            {
                userData.subDelai(250) // délai moins le temps écoulé

                // Si le délai  est dépassé :
                if (userData.delai <= 0) {
                    wakeup() // sortie de veille.
                    vibreur.vibration(this@AideActivity, 1000)

                    // Sonnerie de notification.
                    val sound: MediaPlayer = MediaPlayer
                        .create(this@AideActivity, R.raw.notification)
                    sound.start()

                    // Retrait du décompte.
                    tvDelai.text = " "
                    tvIntituleDelai.text = " "
                    userData.refreshLog(18) // rafraîchissement du Log.
                    userData.prive = false // changement d'état.

                    // Reboot complet de l'activity.
                    val intent = Intent(this@AideActivity, AideActivity::class.java)
                    startActivity(intent)
                } else {
                    // Mise à jour du décompte.
                    val min = (userData.delai / 60000).toInt() // calcul des minutes
                    val sec = (userData.delai / 1000).toInt() - min * 60 // calcul des secondes
                    tvIntituleDelai.text = getString(R.string.intitule_delai)
                    var secSTG = sec.toString()
                    if (sec < 10) secSTG = "0$secSTG"
                    tvDelai.text = " $min\'$secSTG"
                }
            }

            // Relance du Handler SI vérifications remplies pour éviter qu'il se duplique.
            if (!userData.esquive) logHandler!!.postDelayed(this, 250) // rafraîchissement
            else userData.esquive = false
        }
    }

    // --> MESSAGE() : affiche en Toast le string entré en paramètre.
    fun message(message: String?) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
        vibreur.vibration(this, 330)
    }

    // --> WAKEUP() : Sortie de veille du téléphone et mise en avant-plan de cette appli.
    fun wakeup() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
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