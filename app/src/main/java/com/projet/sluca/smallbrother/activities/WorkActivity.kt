package com.projet.sluca.smallbrother.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlinx.coroutines.runBlocking
import java.io.IOException

/***
 * class WorkActivity manages the capture of the audio record and motion information
 *
 * @author Maxime Caucheteur (with contribution of Sébatien Luca (Java version))
 * @version 1.2 (Updated on 02-01-2023)
 */
class WorkActivity : AppCompatActivity(), SensorEventListener, AccelerometerListener {

    var vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var tvLoading: TextView

    private lateinit var tvAction: TextView

    private lateinit var clef: String

    private var appelant: String? = null // variable for caller's phone number

    private var magneto: MediaRecorder? = null // Declare MediaRecorder

    var ambientLightLux: Float = 0.0f

    // Variables for movement determination
    private var checkMove1: FloatArray? = null
    private var checkMove2: FloatArray? = null
    private var keepMove: FloatArray? = null

    private var emergency: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        tvLoading = findViewById(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""

        userData = application as UserData

        // To avoid, on return to AideActivity, the creation of a new Handler
        userData.esquive = true

        // fetch the code in the SMS, if present
        if (SmsReceiver.clef != null) clef = SmsReceiver.clef.toString()

        //Set clef value if aide initiates the capture
        if (intent.hasExtra("clef")) {
            clef = intent.getStringExtra("clef").toString()
            emergency = true
        }

        // fetch caller's phone number
        appelant = PhoneStatReceiver.catchCallNumber()
        if (appelant?.startsWith("+32") == true) appelant?.replace("+32", "0")
        PhoneStatReceiver.resetCallNumber()

        // On phone call
        if (appelant != "" && userData.telephone == appelant) {
            // If caller is the partner, update log
            userData.refreshLog(8)
            retour()
        } else {
            when (clef) {
                "[#SB01]" -> {
                    vibreur.vibration(this, 330)
                    userData.refreshLog(3)
                    userData.byeData() // Delete user's data file

                    while(userData.loadData()) Log.d("file", "still present")
                    // Checks if the donnees.txt file is gone before restarting the install process
                    if(!userData.loadData()){
                        val mIntent = Intent(this, Launch1Activity::class.java)
                        startActivity(mIntent)
                    }
                }
                "[#SB02]" -> {
                    userData.refreshLog(6)

                    val intent = Intent(this, AideActivity::class.java)
                    startActivity(intent)
                }
                "[#SB04]" -> {
                    Toast.makeText(this, "This is an emergency", Toast.LENGTH_LONG).show()
                    Log.d("SB04", "EXEC emergency")

                    // Put the app on foreground
                    wakeup(window, this@WorkActivity)
                    loading(tvLoading)

                    // Checks if mobile phone is connected to internet before making the
                    // context capture
                    CoroutineScope(Dispatchers.IO).launch {
                        if (isOnline(this@WorkActivity)) {
                            Log.d("isOnline", "true")
                            // De-activate SMSReceiver to avoid conflict
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

                            // ================== [ Constitution du dossier joint ] ==================

                            // --> [1] Records a 10 seconds audio of the aide's environment

                            tvAction.text = getString(R.string.message12A)

                            audioCapture()

                            // =======================================================================
                        } else  // Device not connected to internet
                        {
                            userData.refreshLog(12)

                            // Vibrate (and emit a sound if phone not in silent mode)
                            MediaPlayer.create(this@WorkActivity, R.raw.alarme).start()
                            vibreur.vibration(this@WorkActivity, 5000)
                            message(
                                this@WorkActivity,
                                "Veuillez vous connecter à Internet.",
                                vibreur
                            )

                            // Aidant is notified that Aide is not connected.
                            var sms = getString(R.string.smsys05)
                            sms = sms.replace("§%", userData.nom)
                            sendSMS(this@WorkActivity, sms, userData.telephone)

                            val intent = Intent(this@WorkActivity, AideActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    // 10 seconds countdown
                    object : CountDownTimer(10010, 1) {
                        override fun onTick(millisUntilFinished: Long) {
                            // position captured at seconds 2 and 9 of the record
                            when {
                                millisUntilFinished > 9000
                                -> checkMove1 = keepMove
                                millisUntilFinished in 1001..1999
                                -> checkMove2 = keepMove
                            }
                        }

                        override fun onFinish()
                        {
                            // Release MediaRecorder
                            magneto?.stop()
                            Log.d("MAGNETO", "MAGNETO STOPS")
                            magneto?.release()
                            magneto = null

                            // Determine if Aide's phone is moving or not
                            val suspens = checkMove1.contentEquals(checkMove2)
                            userData.motion = !suspens
                            Log.d("MOTION", userData.motion.toString())

                            // Capture light level
                            registerLightSensor()

                            val intent = Intent(this@WorkActivity, Work2Activity::class.java)
                            intent.putExtra("light", ambientLightLux)
                            if(emergency) intent.putExtra("emergency", true)
                            startActivity(intent)
                        }
                    }.start()
                }
            }
        }
    }

    /**
     * Manages the capture of an audio
     *
     * @author Maxime Caucheteur (with contribution of Sébastien Luca (java version))
     * @version 1.2 (Updated on 29-12-2022)
     */
    private fun audioCapture() {
        // File path
        val path = userData.path + "/SmallBrother/audio.ogg"

        // Init and configure MediaRecorder
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
        magneto?.start()
    }

    // Redirect to adequate activity
    /**
     * Redirects to the adequate activity
     *
     * @author Maxime Caucheteur (with contribution of Sébastien Luca (java version))
     * @version 1.2 (Updated on 02-01-2023)
     */
    private fun retour() {
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
        // Fetch phone's coordinates
        // Error margin (*10) to compensate the accelerometer high sensibility
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

    /*-------------Functions related to the light sensor----------*/
    private fun registerLightSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as
                SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                ambientLightLux = event.values[0]
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }
}