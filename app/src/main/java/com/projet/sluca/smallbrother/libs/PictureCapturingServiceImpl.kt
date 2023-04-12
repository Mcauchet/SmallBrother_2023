package com.projet.sluca.smallbrother.libs

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CaptureRequest.Builder
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * The aim of this service is to secretly take pictures (without preview or opening device's
 * camera app) from all available cameras using Android Camera 2 API
 *
 * @author hzitoun (zitoun.hamed@gmail.com)
 * (converted in Kotlin by Maxime Caucheteur on 07-09-2022, updated on 09-03-2023)
 *
 * https://github.com/hzitoun/android-camera2-secret-picture-taker for further info about
 * this library
 */
class PictureCapturingServiceImpl
/**
 * private constructor, meant to force the use of [.getInstance]  method
 */
private constructor(activity: Activity) : APictureCapturingService(activity) {
    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null

    /**
     * camera ids queue.
     */
    private lateinit var cameraIds: Queue<String>
    private var currentCameraId: String? = null
    private var cameraClosed = false

    /**
     * stores a sorted map of (pictureUrlOnDisk, PictureData).
     */
    private var picturesTaken: TreeMap<String, ByteArray>? = null
    private var capturingListener: PictureCapturingListener? = null
    private var numeroImage = 0

    private var pathFile: String? = null

    /**
     * Starts pictures capturing treatment.
     *
     * @param listener picture capturing listener
     * @param context the context of the application
     */
    override fun startCapturing(listener: PictureCapturingListener?, context: Context?) {
        pathFile = context?.filesDir?.path
        numeroImage = 1 // [SL:] au lancement, réinitialiser numeroImage.
        picturesTaken = TreeMap()
        capturingListener = listener
        cameraIds = LinkedList()
        try {
            val cameraIds = manager.cameraIdList
            if (cameraIds.isNotEmpty()) {
                this.cameraIds.addAll(listOf(*cameraIds))
                currentCameraId = this.cameraIds.poll()
                openCamera()
            } else {
                //No camera detected!
                capturingListener!!.onDoneCapturingAllPhotos(picturesTaken)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Exception occurred while accessing the list of cameras", e)
        }
    }

    private fun openCamera() {
        Log.d(
            TAG,
            "opening camera $currentCameraId"
        )
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                manager.openCamera(currentCameraId!!, stateCallback, null)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, " exception occurred while opening camera $currentCameraId", e)
        }
    }

    private val captureListener: CaptureCallback = object : CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession, request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            if (picturesTaken!!.lastEntry() != null) {
                capturingListener!!.onCaptureDone(
                    picturesTaken!!.lastEntry()!!.key,
                    picturesTaken!!.lastEntry()!!.value
                )
                Log.i(TAG, "done taking picture from camera " + cameraDevice!!.id)
            }
            closeCamera()
        }
    }
    private val onImageAvailableListener = OnImageAvailableListener { imReader: ImageReader ->
        val image = imReader.acquireLatestImage()
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer[bytes]
        saveImageToDisk(bytes)
        image.close()
    }
    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraClosed = false
            Log.d(TAG, "camera " + camera.id + " opened")
            cameraDevice = camera
            Log.i(TAG, "Taking picture from camera " + camera.id)
            //Take the picture after some delay. It may resolve getting a black dark photos.
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    takePicture()
                } catch (e: CameraAccessException) {
                    Log.e(TAG, " exception occurred while taking picture from $currentCameraId", e)
                }
            }, 1000)
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, " camera " + camera.id + " disconnected")
            if (cameraDevice != null && !cameraClosed) {
                cameraClosed = true
                cameraDevice!!.close()
            }
        }

        override fun onClosed(camera: CameraDevice) {
            cameraClosed = true
            Log.d(TAG, "camera " + camera.id + " closed")
            //once the current camera has been closed, start taking another picture
            if (!cameraIds.isEmpty()) takeAnotherPicture()
            else capturingListener!!.onDoneCapturingAllPhotos(picturesTaken)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "camera in error, int code $error")
            if (cameraDevice != null && !cameraClosed) cameraDevice!!.close()
        }
    }

    @Throws(CameraAccessException::class)
    private fun takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            throw IllegalStateException("cameraDevice is null")
        }
        val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
        var jpegSizes: Array<Size>? = null
        val streamConfigurationMap =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (streamConfigurationMap != null)
            jpegSizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG)
        val jpegSizesNotEmpty = jpegSizes != null && jpegSizes.isNotEmpty()
        val width = if (jpegSizesNotEmpty) jpegSizes!![0].width else 640
        val height = if (jpegSizesNotEmpty) jpegSizes!![0].height else 480
        val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
        val outputSurfaces: MutableList<Surface> = ArrayList()
        outputSurfaces.add(reader.surface)
        val captureBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        configureCaptureBuilder(captureBuilder, reader)
        reader.setOnImageAvailableListener(onImageAvailableListener, null)
        createCaptureSession(outputSurfaces, captureBuilder)
    }

    /**
     * Configure the capture builder with settings for pictures
     * @param captureBuilder the CaptureRequest Builder
     * @param reader the ImageReader
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 12-04-2023)
     */
    private fun configureCaptureBuilder(captureBuilder: Builder, reader: ImageReader) {
        //TODO test several parameters to get the best option
        captureBuilder.addTarget(reader.surface)
        //captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true)
        captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 10)
        captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        //captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (200_000_000L)) //Exposure Time
        //captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 1600) //ISO
    }

    /**
     * Creates the capture session to take picture
     * @param outputSurfaces
     * @param captureBuilder the CaptureRequest Builder
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 12-04-2023)
     */
    private fun createCaptureSession(outputSurfaces: MutableList<Surface>,
                                     captureBuilder: Builder) {
        cameraDevice!!.createCaptureSession(outputSurfaces,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, null)
                    } catch (e: CameraAccessException) {
                        Log.e(TAG, " exception occurred while accessing $currentCameraId", e)
                    }
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, null)
    }

    private fun saveImageToDisk(bytes: ByteArray) {
        val file = File("$pathFile/SmallBrother/autophoto$numeroImage.jpg")
        numeroImage++
        try {
            FileOutputStream(file).use { output ->
                output.write(bytes)
                picturesTaken!!.put(file.path, bytes)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Exception occurred while saving picture to external storage ", e)
        }
    }

    private fun takeAnotherPicture() {
        currentCameraId = cameraIds.poll()
        openCamera()
    }

    private fun closeCamera() {
        Log.d(TAG, "closing camera " + cameraDevice!!.id)
        if (null != cameraDevice && !cameraClosed) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    companion object {
        private val TAG = PictureCapturingServiceImpl::class.java.simpleName

        /**
         * @param activity the activity used to get the app's context and the display manager
         * @return a new instance
         */
        fun getInstance(activity: Activity): APictureCapturingService {
            return PictureCapturingServiceImpl(activity)
        }
    }
}