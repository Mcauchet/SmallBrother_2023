package com.projet.sluca.smallbrother

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.libs.AccelerometerListener
import com.projet.sluca.smallbrother.libs.AccelerometerManager
import java.io.IOException

class WorkActivity : AppCompatActivity(), SensorEventListener, AccelerometerListener {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLoading: TextView // Déclaration d'un objet TextView.

    private lateinit var tvAction: TextView // Déclaration du TextView pour l'action en cours.

    private lateinit var clef: String // Récupération d'un mot-clef reçu par SMS.

    private var appelant: String? = null // Récupération du numéro d'un appelant.

    private var magneto: MediaRecorder? = null // Création d'un recorder audio.


    // Variables pour déterminer l'état de mouvement.
    private lateinit var checkMove1: FloatArray
    private lateinit var checkMove2: FloatArray
    private lateinit var keepMove: FloatArray

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_work.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        // Liaison et remplissage des objets TextView.
        tvLoading = findViewById(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""

        // Etablissement de la liaison avec la classe UserData.
        userData = application as UserData

        // Déclaration d'un passage dans la WorkActivity pour éviter que, au retour dans
        // AideActivity, ne soit généré un doublon du Handler local.
        userData.esquive = true

        // Récupération d'un mot-clef reçu par SMS, s'il en est.
        if (SmsReceiver.catchClef() != null) clef = SmsReceiver.catchClef().toString()

        // Récupération du numéro de l'appelant, suite à un appel reçu.
        appelant = PhoneStatReceiver.catchcallNumber()
        appelant = appelant!!.replace("+32", "0")
        PhoneStatReceiver.resetCallNumber()

        // SI APPEL RECU :
        if (appelant != "" && userData.telephone == appelant) {
            // Si l'appelant est bien le partenaire : màj du Log.
            if (userData.telephone == appelant) userData.refreshLog(8)
            retour() // Retour à l'écran de rôle.
        } else {
            when (clef) {
                "[#SB01]" -> {
                    vibreur.vibration(this, 330)
                    userData.byeData() // Suppression des données de l'utilisateur.
                    userData.refreshLog(3) // message de Log adéquat.

                    // Redémarrage de l'appli.
                    val mIntent = Intent(this, Launch1Activity::class.java)
                    startActivity(mIntent)
                }
                "[#SB02]" -> {
                    userData.refreshLog(6) // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidé.
                    val intent = Intent(this, AideActivity::class.java)
                    startActivity(intent)
                }
                "[#SB03]" -> {
                    userData.refreshLog(5) // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    val intent = Intent(this, AidantActivity::class.java)
                    startActivity(intent)
                }
                "[#SB04]" -> {

                    // --> Vérification : l'appareil est bien connecté au Net.
                    if (checkInternet()) {
                        // Désactivation du SMSReceiver (pour éviter les cumuls de SMS).
                        val pm = this@WorkActivity.packageManager
                        val componentName = ComponentName(
                            this@WorkActivity,
                            SmsReceiver::class.java
                        )
                        pm.setComponentEnabledSetting(
                            componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                        loading() // Déclenchement de l'animation de chargement.

                        // ================== [ Constitution du dossier joint ] ==================

                        // --> [1] captation et enregistrement d'un extrait sonore de dix secondes.
                        //     en parallèle se détermine également si le téléphone est en mouvement.

                        // Affichage de l'action en cours.
                        tvAction.text = getString(R.string.message12A)

                        // Destination du futur fichier :
                        val fichier = userData.audioPath

                        // Configuration du recorder "magneto".
                        //TODO deprecated, change for cameraX
                        magneto = MediaRecorder()
                        magneto!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                        magneto!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        magneto!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                        magneto!!.setOutputFile(fichier)
                        try {
                            magneto!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        magneto!!.start() // Enregistrement lancé.

                        // Délai de 10 secondes :
                        object : CountDownTimer(10010, 1) {
                            override fun onTick(millisUntilFinished: Long) {
                                // Aux secondes 9 et 2 sont capturé la position du téléphone.
                                if (millisUntilFinished > 9000) {
                                    checkMove1 = keepMove
                                } else if (millisUntilFinished in 1001..1999) {
                                    checkMove2 = keepMove
                                }
                            }

                            override fun onFinish() // Fin du délai :
                            {
                                // Conclusion de l'enregistrement.
                                magneto!!.stop()
                                magneto!!.release()
                                magneto = null

                                // Déclaration : le téléphone est ou non en mouvement.
                                val suspens = checkMove1.contentEquals(checkMove2)
                                userData.motion = !suspens

                                // Suite des évènements dans une autre activity pour éviter les
                                // interférences entre les intents.
                                val intent = Intent(this@WorkActivity, Work2Activity::class.java)
                                startActivity(intent)
                            }
                        }.start()

                        // =======================================================================
                    } else  // Si pas de connexion :
                    {
                        userData.refreshLog(12) // message de Log adéquat.

                        // Alarme : son et vibrations
                        val sound: MediaPlayer = MediaPlayer.create(this, R.raw.alarme)
                        sound.start()
                        vibreur.vibration(this, 5000)

                        // L'Aidant est averti par SMS de l'échec.
                        var sms = getString(R.string.smsys05)
                        sms = sms.replace("§%", userData.nom)
                        this.getSystemService(SmsManager::class.java)
                            .sendTextMessage(userData.telephone, null, sms,
                                null, null)

                        // Retour à l'écran de rôle de l'Aidé.
                        val intent = Intent(this, AideActivity::class.java)
                        startActivity(intent)
                    }
                }
                "[#SB05]" -> {
                    userData.refreshLog(13) // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    val intent = Intent(this, AidantActivity::class.java)
                    startActivity(intent)
                }
                "[#SB06]" -> {
                    userData.refreshLog(14) // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    val intent = Intent(this, AidantActivity::class.java)
                    startActivity(intent)
                }
                "[#SB07]" -> {
                    userData.refreshLog(19) // message de Log adéquat.

                    // Retour à l'écran de rôle de l'Aidant.
                    val intent = Intent(this, AidantActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    // --> Animation des points de suspension en boucle de 2 secondes.
    fun loading() {
        // Sortie de veille du téléphone et mise en avant-plan de cette appli.
        val window = window
        //FLAG_DISMISS_KEYGUARD is deprecated since API 26, use setShowWhenLocked instead
        //https://developer.android.com/reference/android/app/Activity#setShowWhenLocked(boolean)
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

        // Animation de chargement.
        object : CountDownTimer(2000, 1) {
            override fun onTick(millisUntilFinished: Long) {
                // A chaque seconde passée, modifier le contenu l'objet TextView.
                if (millisUntilFinished > 1600) tvLoading.text =
                    "" else if (millisUntilFinished > 1200) tvLoading.text =
                    "." else if (millisUntilFinished > 800) tvLoading.text =
                    ".." else if (millisUntilFinished > 400) tvLoading.text =
                    "..."
            }

            override fun onFinish() {
                loading()
            }
        }.start()
    }

    // --> Retour à l'écran de rôle adéquat.
    fun retour() {
        if ((userData.role == "Aidant")) {
            // Envoie vers l'installation d'un Aidant.
            val intent = Intent(this, AidantActivity::class.java)
            startActivity(intent)
        } else if ((userData.role == "Aidé")) {
            // Envoie vers l'installation d'un Aidé.
            val intent = Intent(this, AideActivity::class.java)
            startActivity(intent)
        }
    }

    // --> CHECKINTERNET() : Renvoie vrai si l'appareil est connecté au Net.
    private fun checkInternet(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return (exitValue == 0)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }

    // --> Par sécurité : retrait du retour en arrière dans cette activity.
    override fun onBackPressed() {
        moveTaskToBack(false)
    }

    // Fonctions relatives à la consultation de l'accéléromètre (mouvement).
    override fun onResume() {
        super.onResume()
        if (AccelerometerManager.isSupported(this) && (userData.role == "Aidé")) {
            AccelerometerManager.startListening(this)
        }
    }

    override fun onAccelerationChanged(x: Float, y: Float, z: Float) {
        // Récupération des coordonnées de position du téléphone.
        // Imposition marge d'erreur (int val*10) pour contrer grande sensibilité capteurs.
        val tmp = floatArrayOf(
            (x.toInt() * 10).toFloat(),
            (y.toInt() * 10).toFloat(),
            (z.toInt() * 10).toFloat()
        )
        keepMove = tmp
    }

    override fun onSensorChanged(event: SensorEvent) {}
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onShake(force: Float) {}
}