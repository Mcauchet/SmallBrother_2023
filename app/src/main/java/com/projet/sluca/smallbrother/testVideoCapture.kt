package com.projet.sluca.smallbrother

import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var camera: Camera
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var surfaceView: SurfaceView
    private lateinit var textureView: TextureView
    private lateinit var surfaceHolder: SurfaceHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        //surfaceView = findViewById(R.id.surface_view)
        //textureView = findViewById(R.id.texture_view)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            camera = Camera.open()
            try {
                camera.setPreviewDisplay(holder)
                camera.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Do nothing
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera.stopPreview()
        camera.release()
    }

    private fun startRecording() {
        camera.unlock()
        mediaRecorder = MediaRecorder().apply {
            setCamera(camera)
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.CAMERA)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile("/path/to/output/file.mp4")
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoSize(640, 480)
            setVideoFrameRate(30)
            setVideoEncodingBitRate(3000000)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            prepare()
        }
        mediaRecorder.start()
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaRecorder.release()
        camera.lock()
    }
}
