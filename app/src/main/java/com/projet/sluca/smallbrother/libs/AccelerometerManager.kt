package com.projet.sluca.smallbrother.libs

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

/**
 * Defines the logic for the movement detection using the accelerometer listener
 * @author Maxime Caucheteur (with contribution of Sébastien Luca (Java version))
 * @version 1.2 (Updated on 05-04-2023)
 */
class AccelerometerManager {

    companion object {
        private var threshold = 10.0f
        private var interval = 200
        private var sensor: Sensor? = null
        private var sensorManager: SensorManager? = null
        private var listener: AccelerometerListener? = null
        private var supported: Boolean? = null
        private var isListening = false

        /**
         * Unregister the accelerometer listener
         * @author Maxime Caucheteur (with contribution of Sébastien Luca (Java version))
         * @version 1.2 (Updated on 05-04-2023)
         */
        fun stopListening() {
            isListening = false
            try {
                if (sensorManager != null) {
                    sensorManager!!.unregisterListener(sensorEventListener)
                }
            } catch (e: Exception) {
                Log.e("AccelerometerManager", "Error stopping the listener", e)
            }
        }

        /**
         * Checks if accelerometer sensor is available
         * @param context the context of the application
         * @return true if at least one sensor is available, false otherwise
         * @author Maxime Caucheteur (with contribution of Sébastien Luca (Java version))
         * @version 1.2 (Updated on 05-04-2023)
         */
        fun isSupported(context: Context): Boolean {
            if (supported == null) {
                sensorManager =
                    context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                val sensors = sensorManager!!.getSensorList(Sensor.TYPE_ACCELEROMETER)
                supported = sensors.size > 0
            }
            return supported!!
        }

        /**
         * Register the accelerometer listener
         * @param accelerometerListener the accelerometer listener to register
         * @param context the context of the application
         * @author Maxime Caucheteur (with contribution of Sébastien Luca (Java version))
         * @version 1.2 (Updated on 05-04-2023)
         */
        fun startListening(accelerometerListener: AccelerometerListener?, context: Context) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensors = sensorManager!!.getSensorList(
                Sensor.TYPE_ACCELEROMETER
            )
            if (sensors.size > 0) {
                sensor = sensors[0]
                isListening = sensorManager!!.registerListener(
                    sensorEventListener,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
                listener = accelerometerListener
            }
        }

        private val sensorEventListener: SensorEventListener = object : SensorEventListener {
            private var now: Long = 0
            private var timeDiff: Long = 0
            private var lastUpdate: Long = 0
            private var lastShake: Long = 0
            private var x = 0f
            private var y = 0f
            private var z = 0f
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f
            private var force = 0f
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                now = event.timestamp
                x = event.values[0]
                y = event.values[1]
                z = event.values[2]
                if (lastUpdate == 0L) {
                    lastUpdate = now
                    lastShake = now
                    lastX = x
                    lastY = y
                    lastZ = z
                } else {
                    timeDiff = now - lastUpdate
                    if (timeDiff > 0) {
                        force = abs(x + y + z - lastX - lastY - lastZ)
                        if (force.compareTo(threshold) > 0) {
                            if (now - lastShake >= interval) {
                                listener!!.onShake(force)
                            }
                            lastShake = now
                        }
                        lastX = x
                        lastY = y
                        lastZ = z
                        lastUpdate = now
                    }
                }
                listener!!.onAccelerationChanged(x, y, z)
            }
        }
    }
}