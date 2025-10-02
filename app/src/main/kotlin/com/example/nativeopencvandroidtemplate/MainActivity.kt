package com.example.nativeopencvandroidtemplate

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import java.io.File

class MainActivity : Activity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )

        setContentView(R.layout.activity_main)

        mOpenCvCameraView = findViewById<CameraBridgeViewBase>(R.id.main_surface)
        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE
        mOpenCvCameraView!!.setCvCameraViewListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mOpenCvCameraView!!.setCameraPermissionGranted()
                } else {
                    val message = "Camera permission was not granted"
                    Log.e(TAG, message)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                Log.e(TAG, "Unexpected permission request")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onResume() {
        super.onResume()
        val libDir = applicationContext.applicationInfo.nativeLibraryDir
        val opencvSo = File(libDir, "libopencv_java4.so")
        Log.d(TAG, "Native library dir: $libDir")
        Log.d(TAG, "libopencv_java4.so exists=${opencvSo.exists()} size=${if (opencvSo.exists()) opencvSo.length() else -1}")
        if (!opencvSo.exists()) {
            Toast.makeText(this, "OpenCV .so missing. See log.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "libopencv_java4.so NOT FOUND. You must restore OpenCV SDK: expected at $libDir/libopencv_java4.so")
        }
        Log.d(TAG, "Attempting manual load of opencv_java4...")
        val libLoaded = try {
            System.loadLibrary("opencv_java4")
            Log.d(TAG, "Manual System.loadLibrary(opencv_java4) succeeded")
            true
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Manual load failed: ${e.message}")
            false
        }
        if (!libLoaded) {
            Log.d(TAG, "Trying OpenCVLoader.initLocal() fallback")
            if (!OpenCVLoader.initLocal()) {
                Log.e(TAG, "OpenCV initialization failed - neither manual load nor initLocal worked")
                Toast.makeText(this, "OpenCV init failed", Toast.LENGTH_LONG).show()
                return
            } else {
                Log.d(TAG, "OpenCVLoader.initLocal() fallback succeeded")
            }
        }
        try {
            val version = Core.getVersionString()
            Log.i(TAG, "OpenCV version: $version")
        } catch (t: Throwable) {
            Log.e(TAG, "Could not query OpenCV version: ${t.message}")
        }
        // Load native-lib (depends on OpenCV symbols)
        try {
            System.loadLibrary("native-lib")
            Log.d(TAG, "native-lib loaded")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native-lib: ${e.message}")
            Toast.makeText(this, "Failed to load native-lib", Toast.LENGTH_LONG).show()
            return
        }
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView!!.enableView()
            Log.d(TAG, "Camera view enabled after OpenCV load")
        } else {
            Log.e(TAG, "Camera view reference is null")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(frame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        // get current camera frame as OpenCV Mat object
        val mat = frame.gray()

        // native call to process current camera frame
        adaptiveThresholdFromJNI(mat.nativeObjAddr)

        // return processed frame for live preview
        return mat
    }

    private external fun adaptiveThresholdFromJNI(matAddr: Long)

    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_PERMISSION_REQUEST = 1
    }
}
