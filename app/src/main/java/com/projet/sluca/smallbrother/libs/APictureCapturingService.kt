package com.projet.sluca.smallbrother.libs

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.SparseIntArray
import android.view.Surface

/**
 * Abstract Picture Taking Service.
 * @author hzitoun (zitoun.hamed@gmail.com)
 * (written in Java by @author, converted to Kotlin by Maxime Caucheteur on 07/09/22)
 * https://github.com/hzitoun/android-camera2-secret-picture-taker for further info about
 * this library
 */
abstract class APictureCapturingService internal constructor(private val activity: Activity) {
    companion object {
        private val ORIENTATIONS = SparseIntArray()

        /*init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }*/

        init {
            //TODO test with other orientations (and test by replacing 90 by 0)
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
        }
    }

    val context: Context = activity.applicationContext
    val manager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    /**
     * @return  orientation
     */
    val orientation: Int
        get() {
            val rotation = activity.windowManager.defaultDisplay.rotation
            return ORIENTATIONS[rotation]
        }

    /**
     * starts pictures capturing process.
     * @param listener picture capturing listener
     * @param context the context of the application
     */
    abstract fun startCapturing(listener: PictureCapturingListener?, context: Context?)

}