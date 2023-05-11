package com.projet.sluca.smallbrother.libs

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraManager

/**
 * Abstract Picture Taking Service.
 * @author hzitoun (zitoun.hamed@gmail.com)
 * (written in Java by @author,
 * converted to Kotlin by Maxime Caucheteur on 07/09/22, updated on 11-05-2023)
 * https://github.com/hzitoun/android-camera2-secret-picture-taker for further info about
 * this library
 */
abstract class APictureCapturingService internal constructor(activity: Activity) {

    val context: Context = activity.applicationContext
    val manager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    /**
     * starts pictures capturing process.
     * @param listener picture capturing listener
     * @param context the context of the application
     */
    abstract fun startCapturing(listener: PictureCapturingListener?, context: Context?)

}