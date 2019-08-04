package csu.liutao.ffmpegdemo.opgls.renders

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES30.*
import android.os.Handler
import android.view.Surface
import csu.liutao.ffmpegdemo.PictureMgr
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.programs.CameraProgram
import java.io.FileOutputStream
import android.graphics.Bitmap
import csu.liutao.ffmpegdemo.opgls.OpglFileManger
import csu.liutao.ffmpegdemo.opgls.ReadPixesConvertPic
import csu.liutao.ffmpegdemo.opgls.ReadPixesConvertVedio


class CameraRender(val context: Context,val isPic : Boolean = true) : GLSurfaceView.Renderer {
    private var program = CameraProgram()
    private var textureId = -1
    private lateinit var surfaceTexture: SurfaceTexture
    val handler = Handler()

    private var cameraDevice: CameraDevice? = null
    private var cameraSession: CameraCaptureSession? = null

    var listener : SurfaceTexture.OnFrameAvailableListener? = null

    private var curWidth = -1
    private var curHeight = -1

    @Volatile
    private var saveListener : OnSavePictureListener? = null

    @Volatile
    private var isSaved = false

    @Volatile
    private var recordListener : OnSaveFrameListener? = null

    private lateinit var picConvert : ReadPixesConvertPic
    private lateinit var videoConvert : ReadPixesConvertVedio
    private var sizeListener: OnSizeChangeListener? = null

    override fun onDrawFrame(gl: GL10?) {
        if (isSaved) return
        if (isPic && saveListener != null) {
            savePicture()
            saveListener!!.onSave(true)
            isSaved = true
            return
        }
        glClear(GL_COLOR_BUFFER_BIT)
        program.onDrawFrame()
        if (!isPic && recordListener != null) {
            recordListener!!.onSave(videoConvert.convert180YUV())
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        curWidth = width
        curHeight = height
        glViewport(0, 0, width, height)
        surfaceTexture.setDefaultBufferSize(width, height)
        program.onSurfaceChanged(width, height)
        if (isPic) {
            picConvert = ReadPixesConvertPic()
            picConvert.init(curWidth, curHeight)
        } else {
            videoConvert = ReadPixesConvertVedio()
            videoConvert.init(curWidth, curHeight)
        }
        sizeListener?.onSizeChanged(curWidth, curHeight)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1f, 1f, 1f, 0f)
        textureId = GlUtils.loadExternTextureId()
        surfaceTexture = SurfaceTexture(textureId)
        program.onSurfaceCreated(context, surfaceTexture, textureId)
        initCamera()
    }

    @SuppressLint("MissingPermission")
    private fun initCamera() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        manager.openCamera(CameraCharacteristics.LENS_FACING_FRONT.toString(), object : CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createCameraSession()
            }
            override fun onDisconnected(camera: CameraDevice) = Utils.log("disconnected")
            override fun onError(camera: CameraDevice, error: Int) = Utils.log("open error")
        }, handler)
    }

    private fun createCameraSession() {
        val list = ArrayList<Surface>(1)
        val surface = Surface(surfaceTexture)
        list.add(surface)
        cameraDevice!!.createCaptureSession(list, object : CameraCaptureSession.StateCallback(){
            override fun onConfigureFailed(session: CameraCaptureSession) = Utils.log("seesion failed")

            override fun onConfigured(session: CameraCaptureSession) {
                cameraSession = session
                val flag = if (isPic) CameraDevice.TEMPLATE_PREVIEW else CameraDevice.TEMPLATE_RECORD
                val builder = cameraDevice!!.createCaptureRequest(flag)
                builder.addTarget(surface)
                if (listener != null) surfaceTexture.setOnFrameAvailableListener(listener!!)
                cameraSession!!.setRepeatingRequest(builder.build(), null, handler)
            }
        }, handler)
    }

    fun setSizeChangeListener(listener: OnSizeChangeListener) {
        sizeListener = listener
    }

    fun resume() {
        isSaved = false
        saveListener = null
    }

    fun stop() {
        cameraSession?.stopRepeating()
        isSaved = true
    }

    fun save(listener : OnSavePictureListener) {
        this.saveListener = listener
    }

    fun savePicture() {
        PictureMgr.instance.initDir(context)
        val newFile = OpglFileManger.instance.getFile(true)
        val outputStream = FileOutputStream(newFile)
        val modelBitmap = Bitmap.createBitmap(picConvert.convertRGBA180(), curWidth, curHeight, Bitmap.Config.ARGB_8888)
        modelBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
    }

    fun startRecord(listener : OnSaveFrameListener) {
        recordListener = listener
    }

    fun release() {
        cameraSession?.close()
        cameraSession = null
        cameraDevice?.close()
        cameraDevice = null
    }

    interface OnSavePictureListener {
        fun onSave(sucess : Boolean)
    }

    interface OnSaveFrameListener {
        fun onSave(bytes : ByteArray)
    }

    interface OnSizeChangeListener{
        fun onSizeChanged(w : Int, h : Int)
    }
}