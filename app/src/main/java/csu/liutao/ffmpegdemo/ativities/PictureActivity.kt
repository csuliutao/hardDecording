package csu.liutao.ffmpegdemo.ativities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PictureActivity :AppCompatActivity() {
    private lateinit var surfaceView :SurfaceView
    private lateinit var capture : Button

    private var cameraDevice : CameraDevice? = null
    private lateinit var imageReader : ImageReader

    private lateinit var cameraMgr : CameraManager

    private lateinit var subHandler : Handler


    private var isCameraSessionReady = false
    private val mainHandler = Handler()

    private val CAMERA_REQUESE_CODE = 5

    private lateinit var cameraCaptureSession: CameraCaptureSession

    private var isTakePicture = false


    private val callback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            initCameraSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            releaseCamera()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Utils.log("camera state error")
        }
    }

    private val sessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Utils.log("createCaptureSession failed")
        }

        override fun onConfigured(session: CameraCaptureSession) {
            cameraCaptureSession = session
            if (!isCameraSessionReady) {
                isCameraSessionReady = true
                takePreview()
            }

            Utils.log("onConfigured")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        surfaceView = findViewById(R.id.picture)
        capture = findViewById(R.id.capture)

        val thread = HandlerThread("pciture_acitivity")
        thread.start()
        subHandler = Handler(thread.looper)

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                releaseCamera()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                initCamera()            }
        })

        capture.setOnClickListener{
            isTakePicture = true
            takePicture()
        }

        if (packageManager.checkPermission(Manifest.permission.CAMERA, packageName) != PackageManager.PERMISSION_GRANTED
            && Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUESE_CODE)
        }
    }

    private fun takePicture() {
        val captureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequest.addTarget(imageReader.surface)
        cameraCaptureSession.capture(captureRequest.build(),null , subHandler)
    }

    private fun releaseCamera() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            cameraDevice?.close()
        }
        cameraDevice = null
    }

    @SuppressLint("MissingPermission")
    private fun initCamera() {
        cameraMgr = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraMgr.openCamera(CameraCharacteristics.LENS_FACING_FRONT.toString(), callback, mainHandler)
    }

    private fun initCameraSession() {
        initImageReader()
        val list = ArrayList<Surface>(2)
        list.add(surfaceView.holder.surface)
        list.add(imageReader.surface)
        cameraDevice!!.createCaptureSession(list, sessionCallback, subHandler)
    }


    private fun takePreview() {
        val captureBuild = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureBuild.addTarget(surfaceView.holder.surface)
        while (!isTakePicture) {
            cameraCaptureSession.capture(captureBuild.build(), null, subHandler)
        }

    }

    private fun initImageReader() {
        imageReader = ImageReader.newInstance(surfaceView.width, surfaceView.height, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener({
            Utils.log("imageReader img available")
        }, subHandler)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUESE_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) finish()
        }
    }
}