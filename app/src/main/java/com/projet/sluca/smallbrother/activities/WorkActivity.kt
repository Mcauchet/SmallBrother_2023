package com.projet.sluca.smallbrother.activities

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.projet.sluca.smallbrother.*
import com.projet.sluca.smallbrother.libs.AccelerometerListener
import com.projet.sluca.smallbrother.libs.AccelerometerManager
import com.projet.sluca.smallbrother.models.UserData
import com.projet.sluca.smallbrother.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.sqrt

/**
 * class WorkActivity manages the capture of the audio record and motion information
 *
 * @author Maxime Caucheteur (with contribution of Sébastien Luca (Java version))
 * @version 1.2 (Updated on 10-04-2023)
 */
class WorkActivity : AppCompatActivity(), SensorEventListener, AccelerometerListener {

    var vibreur = Vibration()
    private lateinit var userData: UserData
    private lateinit var clef: String
    private var caller: String? = null
    private var magneto: MediaRecorder? = null
    var ambientLightLux: Float = 0.0f

    private var checkAcc1: Boolean = false
    private var checkAcc2: Boolean = false

    private var checkXYZ1: FloatArray? = null
    private var checkXYZ2: FloatArray? = null
    private var keepMove: FloatArray? = null

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

        userData = UserDataManager.getUserData(application)

        userData.esquive = true

        setAppBarTitle(userData, this)

        if (SmsReceiver.clef != null) clef = SmsReceiver.clef.toString()

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
            wakeup(window, this@WorkActivity)
            loading(tvLoading)
            alarm(this) //TODO test this

            CoroutineScope(Dispatchers.IO).launch {
                deactivateSmsReceiver(this@WorkActivity)
                registerLightSensor()
                registerMovementDetector()

                // --> [1] Records a 10 seconds audio of the aide's environment
                tvAction.text = getString(R.string.message12A)
                initMagneto()
                magneto?.start()
            }

            // 10 seconds countdown
            object : CountDownTimer(11000, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    // position captured at seconds 2 and 9 of the record
                    when (millisUntilFinished) {
                        in 8900..9000 -> {
                            checkAcc1 = userData.motion
                            checkXYZ1 = keepMove
                        }
                        in 1900..2000 -> {
                            checkAcc2 = userData.motion
                            checkXYZ2 = keepMove
                        }
                    }
                }

                override fun onFinish() {
                    resetMagneto()
                    val accInterpretation = interpretAcceleration(checkAcc1, checkAcc2)
                    val movementInterpretation = interpretMovement(checkXYZ1, checkXYZ2)
                    val intent = Intent(this@WorkActivity, Work2Activity::class.java)
                    intent.putExtra("light", ambientLightLux)
                    intent.putExtra("accInterpretation", accInterpretation)
                    intent.putExtra("movementInterpretation", movementInterpretation)
                    if(emergency) intent.putExtra("emergency", true)
                    unregisterListener(lightDetectorListener)
                    AccelerometerManager.stopListening()
                    startActivity(intent)
                }
            }.start()
        }
    }

    /**
     * Interpret the results of the motion capture based on the start and end of the audio record
     * @param checkAcc1 true if moving at second 2 of the record, false otherwise
     * @param checkAcc2 true if moving at second 9 of the record, false otherwise
     * @return the interpretation as a String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun interpretAcceleration(checkAcc1: Boolean, checkAcc2: Boolean): String {
        return when {
            checkAcc1 && checkAcc2 -> "En mouvement"
            checkAcc1 && !checkAcc2 -> "S'est arrêté"
            !checkAcc1 && checkAcc2 -> "Commence à bouger"
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

    /*-------------Functions related to the acceleration detection----------*/
    /**
     * Register a sensor event listener to detect the acceleration of the phone
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun registerMovementDetector() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        movementDetectorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
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

    private fun interpretMovement(checkXYZ1: FloatArray?, checkXYZ2: FloatArray?): Boolean =
        checkXYZ1.contentEquals(checkXYZ2)

    override fun onSensorChanged(event: SensorEvent) {}
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onShake(force: Float) {}
}