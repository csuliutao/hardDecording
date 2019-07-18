package csu.liutao.ffmpegdemo.ativities

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaFormat
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
import java.io.FileOutputStream
import java.lang.Math.min

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MediaRecordActivity : AppCompatActivity (){
    private lateinit var curFile : File

    private var fos : FileOutputStream? = null

    private var isStarted = false

    private lateinit var textureView : TextureView
    private lateinit var button : ImageButton

    private lateinit var mSurface : Surface

    private lateinit var cameraMgr: CameraManager

    private var cameraDevice : CameraDevice? = null

    private var cameraSession :CameraCaptureSession? = null

    private var h264Encode : MediaCodec? = null

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
            if (Utils.checkCameraPermission(this@MediaRecordActivity)) initCamera()
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

//    private lateinit var encodeCallback : EncodeCallback

    private lateinit var encodeSuface : Surface

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
        requet.addTarget(imageReader.surface)
        cameraSession!!.setRepeatingRequest(requet.build(),null , subHandler)
    }

    fun initEncode() {
        val format = MediaMgr.instance.getH264CodecFromat(textureView.width, textureView.height)
        h264Encode = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        h264Encode!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        /*encodeSuface = h264Encode.createInputSurface()
        encodeCallback = EncodeCallback()
        h264Encode.setCallback(encodeCallback)*/
        h264Encode!!.start()
    }

    private fun initImageReader() {
        Utils.log("width =" + textureView.width + ", h = "+ textureView.height)
        imageReader = ImageReader.newInstance(textureView.width / 2, textureView.height / 2, ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader?) {
                if (reader == null) return
                val image = reader!!.acquireNextImage()
                if (isStarted) {
                    writeYUVImagToFile(image)
                }
                image.close()
            }
        }, subHandler)
    }

    private fun writeYUVImagToFile(image: Image) {
        val plants = image.planes
        val size = plants.size
        if (size != 3) throw Exception("image data is wrong")
        val yBuffer = plants[0].buffer
        val ySize = yBuffer.remaining()
        val uBuffer = plants[1].buffer
        val uSize = uBuffer.remaining()
        val vBuffer = plants[2].buffer
        val vSize = vBuffer.remaining()

        val allSize = ySize + uSize + vSize
        val srcByte = ByteArray(allSize)

        Utils.log("y ="+ ySize+",v="+vSize+"u="+uSize)
        //nV21
        yBuffer.get(srcByte, 0, ySize)
        uBuffer.get(srcByte, ySize, uSize)
        vBuffer.get(srcByte, ySize + uSize, vSize)

        /*var curSize = 0
        while (curSize < allSize) {
            val size = min(MediaMgr.MAX_INPUT_SIZE, allSize - curSize)
            encodeToFile(srcByte, curSize, size)
            curSize += MediaMgr.MAX_INPUT_SIZE
        }*/
        encodeToFile(srcByte, 0, allSize)
    }

    private fun encodeToFile(srcByte: ByteArray, offset: Int, size: Int) {
        val iindex = h264Encode!!.dequeueInputBuffer(-1)
        if(iindex > -1) {
            val inBuffer = h264Encode!!.getInputBuffer(iindex)
            inBuffer.clear()
            inBuffer.put(srcByte, offset, size)
            h264Encode!!.queueInputBuffer(iindex, 0, size, 0, 0)
        }

        var info = MediaCodec.BufferInfo()
        var oindex = h264Encode!!.dequeueOutputBuffer(info, 0)
        while (oindex > -1) {
            val outBuffer = h264Encode!!.getOutputBuffer(oindex)
            outBuffer.position(info.offset)
            outBuffer.limit(info.offset + info.size)

            val dstByte = ByteArray(info.size)
            outBuffer.get(dstByte)
            fos!!.write(dstByte)

            h264Encode!!.releaseOutputBuffer(oindex, 0)
            oindex = h264Encode!!.dequeueOutputBuffer(info, 0)
        }

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
            if (!isStarted) {
                initEncode()
                isStarted = true
                curFile = MediaMgr.instance.getNewFile()
                fos = FileOutputStream(curFile)
//                encodeCallback.fos = FileOutputStream(curFile)
            } else {
                isStarted = false
                stopRecord()
            }
        }
    }

    private fun stopRecord() {
//        encodeCallback.close()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Utils.CAMERA_REQUESE_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            } else {
                initCamera()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        fos?.close()
        h264Encode?.release()
        cameraSession!!.stopRepeating()

        cameraSession?.close()
        cameraSession = null

        subThread.quitSafely()
    }

/*
    class EncodeCallback : MediaCodec.Callback() {
        lateinit var fos : FileOutputStream

        fun close(){
            fos.close()
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            Utils.log("onInputBufferAvailable")
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (fos == null) {
                Utils.log("not start")
                return
            }
            val outBuffer = codec.getOutputBuffer(index)
            outBuffer.position(info.offset)
            outBuffer.limit(info.offset + info.size)
            val srcByte = ByteArray(info.size)
            outBuffer.get(srcByte)
            fos.write(srcByte)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            Utils.log("onOutputFormatChanged")
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log("onError")
        }
    }
*/
}