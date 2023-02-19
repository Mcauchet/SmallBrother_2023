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
import com.projet.sluca.smallbrother.libs.AccelerometerListener
import com.projet.sluca.smallbrother.libs.AccelerometerManager
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
 * @version 1.2 (Updated on 19-02-2023)
 */
class WorkActivity : AppCompatActivity(), SensorEventListener, AccelerometerListener {

    var vibreur = Vibration()
    lateinit var userData: UserData
    private lateinit var clef: String
    private var caller: String? = null // variable for caller's phone number
    private var magneto: MediaRecorder? = null
    var ambientLightLux: Float = 0.0f

    private var checkMove1: FloatArray? = null
    private var checkMove2: FloatArray? = null
    private var keepMove: FloatArray? = null

    private var emergency: Boolean = false

    private lateinit var tvLoading: TextView
    private lateinit var tvAction: TextView

    private var sensorEventListener: SensorEventListener? = null
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
                    userData.byeData()
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
                            vibreur.vibration(this@WorkActivity, 5000)

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
                                in 8900..9000 -> checkMove1 = keepMove
                                in 1900..2000 -> checkMove2 = keepMove
                            }
                        }

                        override fun onFinish() {
                            resetMagneto()
                            //userData.motion = !(checkMove1.contentEquals(checkMove2))
                            val intent = Intent(this@WorkActivity, Work2Activity::class.java)
                            intent.putExtra("light", ambientLightLux)
                            if(emergency) intent.putExtra("emergency", true)
                            Log.d("motion userdata before unreg", userData.motion.toString())
                            unregisterLightSensor()
                            unregisterMovementDetector()
                            Log.d("motion userdata after unreg", userData.motion.toString())
                            startActivity(intent)
                        }
                    }.start()
                }
            }
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

    private fun userIsMoving(checkMove1: FloatArray, checkMove2: FloatArray): Boolean {

        return true
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
    /**
     * Register a sensor event listener to get the ambient light level of the phone
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun registerLightSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as
                SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                ambientLightLux = event.values[0]
                Log.d("light sensor", lightSensor.toString())
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(sensorEventListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * Unregister the sensor event listener
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 19-02-2023)
     */
    private fun unregisterLightSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorEventListener?.let { sensorManager.unregisterListener(it) }
    }

    /*-------------Functions related to the movement detection----------*/
    private fun registerMovementDetector() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        movementDetectorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //Do nothing
            }

            override fun onSensorChanged(event: SensorEvent?) {
                val accX = event?.values?.get(0) ?: 0f
                val accY = event?.values?.get(1) ?: 0f
                val accZ = event?.values?.get(2) ?: 0f

                val acceleration = sqrt(accX * accX + accY * accY + accZ * accZ)

                if (acceleration > 1.5) {
                    Log.d("motion userdata", userData.motion.toString())
                    userData.motion = true
                }
            }
        }

        sensorManager.registerListener(movementDetectorListener, accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unregisterMovementDetector() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(movementDetectorListener)
    }
}