package csu.liutao.ffmpegdemo.ativities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.MediaMgr
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MediaRecordActivity : AppCompatActivity (){
    private var curFile : File? = null
    private var isSaved = false

    private lateinit var textureView : TextureView
    private lateinit var button : ImageButton

    private lateinit var mSurface : Surface

    private lateinit var cameraMgr: CameraManager

    private var cameraDevice : CameraDevice? = null

    private var cameraSession :CameraCaptureSession? = null

    private val reqCode = 10

    private val mainHandler = Handler()

    private val subThread = HandlerThread("MediaRecordActivity")

    private lateinit var subHandler : Handler

    private lateinit var imageReader : ImageReader

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            mSurface = Surface(surface)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
                packageManager.checkPermission(Manifest.permission.CAMERA, packageName) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), reqCode)
            } else {
                initCamera()
            }
        }
    }

    private val callback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            initCameraSeesion()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Utils.log("CameraDevice.StateCallback error")
        }
    }

    private fun initCameraSeesion() {
        initImageReader()
        val list = ArrayList<Surface>()
        list.add(mSurface)
        list.add(imageReader.surface)

        cameraDevice!!.createCaptureSession(list, object : CameraCaptureSession.StateCallback(){
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Utils.log("createCaptureSession failed")
            }

            override fun onConfigured(session: CameraCaptureSession) {
                cameraSession = session
                startPreview()
            }
        } ,subHandler)
    }

    private fun startPreview() {
        val requet = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        requet.addTarget(mSurface)
        cameraSession!!.setRepeatingRequest(requet.build(), null, subHandler)
    }

    private fun startRecord () {
        val requet = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        requet.addTarget(imageReader.surface)
        cameraSession!!.setRepeatingRequest(requet.build(), null, subHandler)
    }

    private fun initImageReader() {
        imageReader = ImageReader.newInstance(textureView.width, textureView.height, PixelFormat.RGBA_8888, 2)
        imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader?) {
                if (reader == null) return
                val image = reader!!.acquireNextImage()
                processImage(image)
                image!!.close()
                Utils.log("imageReader")
            }
        }, subHandler)
    }

    private fun processImage(image: Image?) {
        curFile?.delete()
    }

    @SuppressLint("MissingPermission")
    private fun initCamera() {
        cameraMgr = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraMgr.openCamera(CameraCharacteristics.LENS_FACING_FRONT.toString(), callback, mainHandler)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_record)
        textureView = findViewById(R.id.preview)
        button = findViewById(R.id.take)

        subThread.start()
        subHandler = Handler(subThread.looper)

        textureView.surfaceTextureListener = surfaceTextureListener
        button.setOnClickListener{
            if (!isSaved) {
                isSaved = true
                curFile = MediaMgr.instance.getNewFile()
                startRecord()
            } else {
                stopRecord()
            }
        }
    }

    private fun stopRecord() {
        cameraSession!!.stopRepeating()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (reqCode == reqCode) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) finish()
            initCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSession?.close()
        cameraSession = null

        subThread.quitSafely()
    }
}