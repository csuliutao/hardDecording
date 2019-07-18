package csu.liutao.ffmpegdemo.ativities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import csu.liutao.ffmpegdemo.PictureMgr
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PictureActivity :AppCompatActivity() {
    private lateinit var surfaceView :SurfaceView
    private lateinit var capture : Button

    private var cameraDevice : CameraDevice? = null
    private lateinit var imageReader : ImageReader

    private lateinit var cameraMgr : CameraManager

    private lateinit var subHandler : Handler

    private val subThread = HandlerThread("pciture_acitivity")

    private val mainHandler = Handler()

    private var cameraCaptureSession: CameraCaptureSession? = null


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
            val captureBuild = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureBuild.addTarget(surfaceView.holder.surface)
            cameraCaptureSession!!.setRepeatingRequest(captureBuild.build(), null, subHandler)

            Utils.log("onConfigured")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        surfaceView = findViewById(R.id.picture)
        capture = findViewById(R.id.capture)

        subThread.start()
        subHandler = Handler(subThread.looper)

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                releaseCamera()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (Utils.checkCameraPermission(this@PictureActivity)) initCamera()
            }
        })

        capture.setOnClickListener{
            takePicture()
        }


    }

    private fun takePicture() {
        PictureMgr.instance.initDir(this)
        val captureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequest.addTarget(imageReader.surface)
        captureRequest.set(CaptureRequest.JPEG_ORIENTATION, 90)
        cameraCaptureSession!!.capture(captureRequest.build(),null , subHandler)
    }

    private fun releaseCamera() {
        cameraCaptureSession?.close()
        cameraCaptureSession = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            cameraDevice?.close()
        }
        cameraDevice = null

        subThread.quitSafely()
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

    private fun initImageReader() {
        imageReader = ImageReader.newInstance(surfaceView.width, surfaceView.height, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener({
            savePicAndShow(it)
        }, subHandler)
    }

    private fun savePicAndShow(it : ImageReader) {
        val image = it.acquireNextImage()
        val byteBuffer = image.planes[0].buffer
        val file = PictureMgr.instance.getFile()

        val fileWrite = FileOutputStream(file)
        val channel = fileWrite.channel
        if (byteBuffer.hasRemaining()) {
            Utils.log("bytebuffer times")
            channel.position(0)
            channel.write(byteBuffer)
            fileWrite.flush()
        }

        channel.close()
        fileWrite.close()
        it.close()
        Utils.log("imageReader img available")
        finish()
        startActivity(Intent(this, SurfaceImgActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Utils.CAMERA_REQUESE_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            } else {
                initCamera()
            }
        }
    }
}