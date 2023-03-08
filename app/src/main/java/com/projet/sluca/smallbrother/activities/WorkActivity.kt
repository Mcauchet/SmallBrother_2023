package com.projet.sluca.smallbrother.activities

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.models.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.sqrt

/***
 * class WorkActivity manages the capture of the audio record and motion information
 *
 * @author Maxime Caucheteur (with contribution of Sébastien Luca (Java version))
 * @version 1.2 (Updated on 20-02-2023)
 */
class WorkActivity : AppCompatActivity(), SensorEventListener /*AccelerometerListener*/ {

    var vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var clef: String
    private var caller: String? = null
    private var magneto: MediaRecorder? = null
    var ambientLightLux: Float = 0.0f

    private var checkMove1: Boolean = false
    private var checkMove2: Boolean = false

    private var emergency: Boolean = false

    private lateinit var tvLoading: TextView
    private lateinit var tvAction: TextView

    private var lightDetectorListener: SensorEventListener? = null
    private var movementDetectorListener: SensorEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        tvLoading = findViewById(R.id.loading)
        tvAction = findViewById(R.id.action)
        tvLoading.text = ""
        tvAction.text = ""

        userData = application as UserData

        userData.esquive = true

        setAppBarTitle(userData, this)

        if (SmsReceiver.clef != null) clef = SmsReceiver.clef.toString()

        //Set clef value if aide initiates the capture
        if (intent.hasExtra("clef")) {
            clef = intent.getStringExtra("clef").toString()
            emergency = true
        }
        caller = PhoneStatReceiver.catchCallNumber()
        if (caller?.startsWith("+32") == true) caller?.replace("+32", "0")
        PhoneStatReceiver.resetCallNumber()

        if (caller != "" && userData.telephone == caller) {
            userData.refreshLog(8)
            redirectRole(this@WorkActivity, userData)
        } else {
            when (clef) {
                "[#SB01]" -> {
                    vibreur.vibration(this, 330)
                    userData.refreshLog(3)
                    userData.byeData("donnees.txt")
                    // Checks if the donnees.txt file is gone before restarting the install process
                    if(!userData.loadData(this)){
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
                    wakeup(window, this@WorkActivity)
                    loading(tvLoading)

                    // Checks if mobile phone is connected to internet before making the
                    // context capture
                    CoroutineScope(Dispatchers.IO).launch {
                        if (isOnline(this@WorkActivity)) {
                            deactivateSmsReceiver(this@WorkActivity)
                            Log.d("motion userdata before reg", userData.motion.toString())
                            registerLightSensor()
                            registerMovementDetector()
                            Log.d("motion userdata after reg", userData.motion.toString())

                            // --> [1] Records a 10 seconds audio of the aide's environment
                            tvAction.text = getString(R.string.message12A)
                            initMagneto()
                            magneto?.start()
                            // =======================================================================
                        } else {
                            userData.refreshLog(12)

                            MediaPlayer.create(this@WorkActivity, R.raw.alarme).start()
                            vibreur.vibration(this@WorkActivity, 3000)

                            var sms = getString(R.string.smsys05)
                            sms = sms.replace("§%", userData.nom)
                            sendSMS(this@WorkActivity, sms, userData.telephone, vibreur)

                            val intent = Intent(this@WorkActivity, AideActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    // 10 seconds countdown
                    object : CountDownTimer(11000, 1) {
                        override fun onTick(millisUntilFinished: Long) {
                            // position captured at seconds 2 and 9 of the record
                            when (millisUntilFinished) {
                                in 8900..9000 -> checkMove1 = userData.motion
                                in 1900..2000 -> checkMove2 = userData.motion
                            }
                        }

                        override fun onFinish() {
                            resetMagneto()
                            val interpretation = interpretMovement(checkMove1, checkMove2)
                            val intent = Intent(this@WorkActivity, Work2Activity::class.java)
                            intent.putExtra("light", ambientLightLux)
                            intent.putExtra("interpretation", interpretation)
                            Log.d("interpretation", interpretation)
                            if(emergency) intent.putExtra("emergency", true)
                            unregisterListener(lightDetectorListener)
                            unregisterListener(movementDetectorListener)
                            startActivity(intent)
                        }
                    }.start()
                }
            }
        }
    }

    /**
     * Interpret the results of the motion capture based on the start and end of the audio record
     * @param checkMove1 true if moving at second 2 of the record, false otherwise
     * @param checkMove2 true if moving at second 9 of the record, false otherwise
     * @return the interpretation as a String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun interpretMovement(checkMove1: Boolean, checkMove2: Boolean): String {
        return when {
            checkMove1 && checkMove2 -> "En mouvement"
            checkMove1 && !checkMove2 -> "S'est arrêté"
            !checkMove1 && checkMove2 -> "Commence à bouger"
            else -> "À l'arrêt"
        }
    }

    /**
     * Init the MediaRecorder responsible for the audio capture
     *
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun initMagneto() {
        magneto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        configureMagneto()
        try{
            magneto?.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * configure the magneto's audio source, output format, audio encoder and output file
     *
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun configureMagneto() {
        val path = userData.path + "/SmallBrother/audio.ogg"

        magneto?.setAudioSource(MediaRecorder.AudioSource.MIC)
        magneto?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        magneto?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        magneto?.setOutputFile(path)
    }

    /**
     * Stop, release and reset the MediaRecorder
     *
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun resetMagneto() {
        magneto?.stop()
        magneto?.release()
        magneto = null
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            moveTaskToBack(false)
        }
    }

    /*-------------Functions related to the light sensor----------*/
    /**
     * Register a sensor event listener to get the ambient light level of the phone
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun registerLightSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as
                SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightDetectorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                ambientLightLux = event.values[0]
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(lightDetectorListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    /*-------------Functions related to the movement detection----------*/
    /**
     * Register a sensor event listener to detect the movement of the phone
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun registerMovementDetector() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        movementDetectorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //Do nothing
            }

            //TODO add more criteria to determine if moving or not
            override fun onSensorChanged(event: SensorEvent?) {
                val accX = event?.values?.get(0) ?: 0f
                val accY = event?.values?.get(1) ?: 0f
                val accZ = event?.values?.get(2) ?: 0f

                val acceleration = sqrt(accX * accX + accY * accY + accZ * accZ)

                userData.motion = acceleration > 1.5
            }
        }

        sensorManager.registerListener(movementDetectorListener, accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * Unregister the sensor event listener
     * @param listener the sensor event listener
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun unregisterListener(listener: SensorEventListener?) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        listener?.let { sensorManager.unregisterListener(listener) }
    }

    override fun onSensorChanged(event: SensorEvent) {}
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}