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
import android.util.Log
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
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * class WorkActivity manages the capture of the audio record and motion information
 *
 * @author Maxime Caucheteur (with contribution of Sébastien Luca (Java version))
 * @version 1.2 (Updated on 27-04-2023)
 */
class WorkActivity : AppCompatActivity(), SensorEventListener, AccelerometerListener {

    val vibreur = Vibration()
    private lateinit var userData: UserData
    private lateinit var clef: String
    private var caller: String? = null
    private lateinit var magneto: MediaRecorder
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
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            CoroutineScope(Dispatchers.IO).launch {
                deactivateSmsReceiver(this@WorkActivity)
                //alarm(this@WorkActivity) //TODO test this
                registerLightSensor(sensorManager)
                registerMovementDetector(sensorManager)

                // --> [1] Records a 10 seconds audio of the aide's environment
                tvAction.text = getString(R.string.message12A)
                initMagneto()
                magneto.start()
            }

            // 10 seconds countdown
            object : CountDownTimer(11000, 100) {
                override fun onTick(millisUntilFinished: Long) {
                    // position captured at seconds 2 and 9 of the record
                    when (millisUntilFinished) {
                        in 8900..9000 -> {
                            checkAcc1 = userData.motion
                            Log.d("Acc1", checkAcc1.toString()) //TODO test this
                            checkXYZ1 = keepMove
                        }
                        in 1900..2000 -> {
                            checkAcc2 = userData.motion
                            Log.d("Acc2", checkAcc2.toString())
                            checkXYZ2 = keepMove
                        }
                    }
                }

                override fun onFinish() {
                    resetMagneto()
                    val accInterpretation = interpretAcceleration(checkAcc1, checkAcc2)
                    val movementInterpretation = interpretMovement(checkXYZ1, checkXYZ2)
                    val intent = Intent(this@WorkActivity, Work2Activity::class.java)
                    intent.putExtra("light", getLightScale(ambientLightLux))
                    intent.putExtra("accInterpretation", accInterpretation)
                    intent.putExtra("movementInterpretation", movementInterpretation)
                    if(emergency) intent.putExtra("emergency", true)
                    unregisterListener(lightDetectorListener, sensorManager)
                    AccelerometerManager.stopListening()
                    unregisterListener(movementDetectorListener, sensorManager)
                    startActivity(intent)
                }
            }.start()
        }
    }

    /**
     * Interpret the results of the motion capture based on the start and end of the audio record
     * @param checkAcc1 true if acceleration above threshold at second 2 of the record,
     * false otherwise
     * @param checkAcc2 true if acceleration above threshold at second 9 of the record,
     * false otherwise
     * @return the interpretation as a String
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 11-04-2023)
     */
    private fun interpretAcceleration(checkAcc1: Boolean, checkAcc2: Boolean) = when {
        checkAcc1 && checkAcc2 -> "En mouvement"
        checkAcc1 && !checkAcc2 -> "S'est arrêté"
        !checkAcc1 && checkAcc2 -> "Commence à bouger"
        else -> "À l'arrêt"
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
            magneto.prepare()
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
        magneto.setAudioSource(MediaRecorder.AudioSource.MIC)
        magneto.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        magneto.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        magneto.setOutputFile(path)
    }

    /**
     * Stop, release and reset the MediaRecorder
     *
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-01-2023)
     */
    private fun resetMagneto() {
        magneto.stop()
        magneto.release()
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
    private fun registerLightSensor(sensorManager: SensorManager) {
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

    /**
     * Gets the light sensor interpretation for the information file
     * @param level the results of the sensor
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 12-04-2023)
     */
    private fun getLightScale(level: Float) = when(level) {
        in 0f..50f -> "Très faible - $level"
        in 50f..200f -> "Faible - $level"
        in 200f..1000f -> "Normal - $level"
        in 1000f..10000f -> "Clair - $level"
        else -> "Très clair - $level"
    }

    /*-------------Functions related to the acceleration detection----------*/
    /**
     * Register a sensor event listener to detect the acceleration of the phone
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 13-04-2023)
     */
    private fun registerMovementDetector(sensorManager: SensorManager) {
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        movementDetectorListener = object : SensorEventListener { //TODO test new version
            private var now: Long = 0
            private var timeDiff: Long = 0
            private var lastUpdate: Long = 0
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f
            private var force = 0f
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                now = event.timestamp
                val accX = event.values?.get(0) ?: 0f
                val accY = event.values?.get(1) ?: 0f
                val accZ = event.values?.get(2) ?: 0f
                if(lastUpdate == 0L) {
                    lastUpdate = now
                    updateCoordinates(accX, accY, accZ)
                } else {
                    timeDiff = now - lastUpdate
                    if(timeDiff > 0) {
                        force = abs(accX + accY + accZ - lastX - lastY - lastZ)
                        val acc = sqrt(accX * accX + accY * accY + accZ*accZ)
                        Log.d("force", force.toString())
                        Log.d("acc", acc.toString())
                        userData.motion = force.compareTo(10.0f) > 0
                        Log.d("motion", userData.motion.toString())
                        updateCoordinates(accX, accY, accZ)
                        lastUpdate = now
                    }
                }
                /*val acceleration = sqrt(accX * accX + accY * accY + accZ * accZ)
                userData.motion = acceleration > 1.5*/
            }
            fun updateCoordinates(x: Float, y: Float, z: Float) {
                lastX = x
                lastY = y
                lastZ = z
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
    private fun unregisterListener(listener: SensorEventListener?, sensorManager: SensorManager) =
        listener?.let { sensorManager.unregisterListener(listener) }


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