package com.projet.sluca.smallbrother.activities

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
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.libs.AccelerometerListener
import com.projet.sluca.smallbrother.libs.AccelerometerManager
import com.projet.sluca.smallbrother.models.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

/***
 * class WorkActivity
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 27-12-2022)
 */
class WorkActivity : AppCompatActivity(), SensorEventListener, AccelerometerListener {

    var vibreur = Vibration() // Instanciation d'un vibreur.
    lateinit var userData: UserData // Liaison avec les données globales de l'utilisateur.
    private lateinit var tvLoading: TextView // Déclaration d'un objet TextView.

    private lateinit var tvAction: TextView // Déclaration du TextView pour l'action en cours.

    private lateinit var clef: String // Récupération d'un mot-clef reçu par SMS.

    private var appelant: String? = null // Récupération du numéro d'un appelant.

    private var magneto: MediaRecorder? = null // Création d'un recorder audio.

    // Variables pour déterminer l'état de mouvement.
    private var checkMove1: FloatArray? = null
    private var checkMove2: FloatArray? = null
    private var keepMove: FloatArray? = null

    private var emergency: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Etablissement de la liaison avec la vue res/layout/activity_work.xml.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

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
        if (SmsReceiver.clef != null) clef = SmsReceiver.clef.toString()

        //Set clef value if aide initiates the capture
        if (intent.hasExtra("clef")) {
            clef = intent.getStringExtra("clef").toString()
            emergency = true
        }

        // Récupération du numéro de l'appelant, suite à un appel reçu.
        appelant = PhoneStatReceiver.catchCallNumber()
        if (appelant?.startsWith("+32") == true) appelant?.replace("+32", "0")
        PhoneStatReceiver.resetCallNumber()

        // SI APPEL RECU :
        if (appelant != "" && userData.telephone == appelant) {
            // Si l'appelant est bien le partenaire : màj du Log.
            userData.refreshLog(8)
            retour() // Retour à l'écran de rôle.
        } else {
            when (clef) {
                "[#SB01]" -> {
                    vibreur.vibration(this, 330)
                    userData.refreshLog(3) // message de Log adéquat.
                    userData.byeData() // Suppression des données de l'utilisateur.

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
                    Toast.makeText(this, "This is an emergency", Toast.LENGTH_LONG).show()
                    Log.d("SB04", "EXEC emergency")

                    // Sortie de veille du téléphone et mise en avant-plan de cette appli.
                    wakeup(window, this@WorkActivity)
                    loading(tvLoading) // Déclenchement de l'animation de chargement.

                    // --> Vérification : l'appareil est bien connecté au Net.
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d("CHK INT", "WAITING")
                        if (isOnline(this@WorkActivity)) {
                            Log.d("CHK INT", "OK")
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
                            Log.d("AFTER PKG MNG", "OK")



                            // ================== [ Constitution du dossier joint ] ==================

                            // --> [1] captation et enregistrement d'un extrait sonore de dix secondes.
                            //     en parallèle se détermine également si le téléphone est en mouvement.

                            // Affichage de l'action en cours.
                            tvAction.text = getString(R.string.message12A)

                            // Destination du futur fichier :
                            val path = userData.path + "/SmallBrother/audio.ogg"

                            // Configuration du recorder "magneto".
                            //TODO deprecated, change for cameraX
                            Log.d("MAGNETO", "INIT")
                            magneto = MediaRecorder()
                            magneto?.setAudioSource(MediaRecorder.AudioSource.MIC)
                            magneto?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                            magneto?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                            magneto?.setOutputFile(path)
                            try {
                                magneto?.prepare()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            Log.d("MAGNETO", "MAGNETO STARTS")
                            magneto?.start() // Enregistrement lancé.

                            // =======================================================================
                        } else  // Si pas de connexion :
                        {
                            userData.refreshLog(12) // message de Log adéquat.

                            // Alarme : son et vibrations
                            val sound: MediaPlayer = MediaPlayer.create(this@WorkActivity, R.raw.alarme)
                            sound.start()
                            vibreur.vibration(this@WorkActivity, 5000)

                            // L'Aidant est averti par SMS de l'échec.
                            var sms = getString(R.string.smsys05)
                            sms = sms.replace("§%", userData.nom)
                            sendSMS(this@WorkActivity, sms, userData.telephone)

                            // Retour à l'écran de rôle de l'Aidé.
                            val intent = Intent(this@WorkActivity, AideActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    // Délai de 10 secondes :
                    object : CountDownTimer(10010, 1) {
                        override fun onTick(millisUntilFinished: Long) {
                            // Aux secondes 9 et 2 sont capturé la position du téléphone.
                            when {
                                millisUntilFinished > 9000
                                -> checkMove1 = keepMove
                                millisUntilFinished in 1001..1999
                                -> checkMove2 = keepMove
                            }
                        }

                        override fun onFinish() // Fin du délai :
                        {
                            // Conclusion de l'enregistrement.
                            magneto?.stop()
                            Log.d("MAGNETO", "MAGNETO STOPS")
                            magneto?.release()
                            magneto = null

                            // Déclaration : le téléphone est ou non en mouvement.
                            val suspens = checkMove1.contentEquals(checkMove2)
                            userData.motion = !suspens
                            Log.d("MOTION", userData.motion.toString())

                            // Suite des évènements dans une autre activity pour éviter les
                            // interférences entre les intents.
                            val intent = Intent(this@WorkActivity, Work2Activity::class.java)
                            if(emergency) intent.putExtra("emergency", true)
                            startActivity(intent)
                        }
                    }.start()
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


    // --> Retour à l'écran de rôle adéquat.
    fun retour() {
        when (userData.role) {
            "Aidant" -> {
                val intent = Intent(this, AidantActivity::class.java)
                startActivity(intent)
            }
            "Aidé" -> {
                val intent = Intent(this, AideActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }

    /* -------------- Functions related to the Accelerometer -------------- */
    override fun onResume() {
        super.onResume()
        if (AccelerometerManager.isSupported(this) && (userData.role == "Aidé")) {
            AccelerometerManager.startListening(this, this)
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