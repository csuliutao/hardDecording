package csu.liutao.ffmpegdemo.medias

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Surface
import csu.liutao.ffmpegdemo.Utils
import java.lang.Exception

class Camera2Mgr private constructor() {
    private lateinit var cameraMgr: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var cameraSession: CameraCaptureSession? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val subThread = HandlerThread("Camera2Mgr")
    private lateinit var subHandler: Handler

    private var imageReader: ImageReader? = null

    private var isForPicture : Boolean = true

    private var isStop = false

    private lateinit var previewSurface : Surface

    private var imageListener : ImageListener? = null

    private val deviceCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Utils.log(TAG, "on error")
        }
    }

    private val captureCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Utils.log(TAG, "onConfigureFailed")
        }

        override fun onConfigured(session: CameraCaptureSession) {
            cameraSession = session
            preview()
        }
    }

    private val imageAvailableListener = object : ImageReader.OnImageAvailableListener{
        override fun onImageAvailable(reader: ImageReader?) {
            if (reader == null) return
            val image = reader!!.acquireNextImage()
            imageListener?.handleImage(image)
            image!!.close()
        }
    }

    init {
        subThread.start()
        subHandler = Handler(subThread.looper)
    }

    fun openCamera(context: Context, cameraId: String = CameraCharacteristics.LENS_FACING_FRONT.toString()) {
        if (context.packageManager.checkPermission(Manifest.permission.CAMERA, context.packageName)
            != PackageManager.PERMISSION_GRANTED
        ) throw Exception("has no permission use camera")

        cameraMgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraMgr.openCamera(cameraId, deviceCallback, mainHandler)
    }



    private fun preview(addImageReader: Boolean = false) {
        val format = if (isForPicture) CameraDevice.TEMPLATE_STILL_CAPTURE else CameraDevice.TEMPLATE_RECORD
        val requestBuilder = cameraDevice!!.createCaptureRequest(format)
        requestBuilder.addTarget(previewSurface)
        if (addImageReader) {
            requestBuilder.addTarget(imageReader!!.surface)
            requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90)
        }
        if (addImageReader && isForPicture) {
            cameraSession!!.capture(requestBuilder.build(), null, subHandler)
        } else {
            cameraSession!!.setRepeatingRequest(requestBuilder.build(), null, subHandler)
        }
    }

    private fun createSession() {
        val list = ArrayList<Surface>(2)
        list.add(previewSurface)
        list.add(imageReader!!.surface)
        cameraDevice!!.createCaptureSession(list, captureCallback, subHandler)
    }

    fun take() {
        isStop = false
        preview(true)
    }

    fun stop(stopPreview : Boolean = true) {
        if (stopPreview) cameraSession!!.stopRepeating()
        isStop = true
    }

    interface ImageListener {
        fun handleImage(image : Image)
    }

    fun release() {
        imageReader?.close()
        imageReader = null
        cameraSession?.close()
        cameraSession == null
        subThread.quitSafely()
    }

    class Builder {
        val camera2Mgr = Camera2Mgr()

        fun surface(surface: Surface, isForPicture : Boolean) : Builder{
            camera2Mgr.previewSurface = surface
            camera2Mgr.isForPicture = isForPicture
            return this
        }

        /**
         * 先调用surface方法后调用次方法
         */
        fun imageReader(width : Int, height : Int, listener: ImageListener) : Builder{
            camera2Mgr.imageListener = listener
            camera2Mgr.imageReader?.close()
            val format = if (camera2Mgr.isForPicture) ImageFormat.JPEG else ImageFormat.YUV_420_888
            camera2Mgr.imageReader = ImageReader.newInstance(width, height, format, MAX_IMAGES)
            camera2Mgr.imageReader!!.setOnImageAvailableListener(camera2Mgr.imageAvailableListener, camera2Mgr.subHandler)
            return this
        }

        fun build() : Camera2Mgr {
            return camera2Mgr
        }
    }

    companion object {
        private val TAG = "Camera2Mgr"
        private val MAX_IMAGES = 2
    }
}