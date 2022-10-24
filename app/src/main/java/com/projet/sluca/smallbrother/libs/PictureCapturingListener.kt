package com.projet.sluca.smallbrother.libs

import java.util.*

/**
 * Picture capturing listener
 * @author hzitoun (zitoun.hamed@gmail.com)
 * (written in Java by @author, converted to Kotlin by Maxime Caucheteur on 07/09/22)
 *
 * https://github.com/hzitoun/android-camera2-secret-picture-taker for further info about
 * this library
 */
interface PictureCapturingListener {
    /**
     * a callback called when we've done taking a picture from a single camera
     * (use this method if you don't want to wait for ALL taken pictures to be ready @see onDoneCapturingAllPhotos)
     *
     * @param pictureUrl  taken picture's location on the device
     * @param pictureData taken picture's data as a byte array
     */
    fun onCaptureDone(pictureUrl: String?, pictureData: ByteArray?)

    /**
     * a callback called when we've done taking pictures from ALL AVAILABLE cameras
     * OR when NO camera was detected on the device
     *
     * @param picturesTaken : a  Map<PictureUrl></PictureUrl>, PictureData>
     */
    fun onDoneCapturingAllPhotos(picturesTaken: TreeMap<String, ByteArray>?)
}