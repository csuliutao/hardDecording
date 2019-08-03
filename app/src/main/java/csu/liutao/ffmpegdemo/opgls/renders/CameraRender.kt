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
import java.nio.IntBuffer
import android.graphics.Bitmap
import csu.liutao.ffmpegdemo.opgls.ReadPixesConvert


class CameraRender(val context: Context) : GLSurfaceView.Renderer {
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

    private var isSaved = false

    override fun onDrawFrame(gl: GL10?) {
        if (isSaved) return
        glClear(GL_COLOR_BUFFER_BIT)
        program.onDrawFrame()
        if (saveListener != null) {
            savePicture()
            saveListener!!.onSave(true)
            isSaved = true
            return
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        curWidth = width
        curHeight = height
        glViewport(0, 0, width, height)
        surfaceTexture.setDefaultBufferSize(width, height)
        program.onSurfaceChanged(width, height)
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
                val builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                builder.addTarget(surface)
                if (listener != null) surfaceTexture.setOnFrameAvailableListener(listener!!)
                cameraSession!!.setRepeatingRequest(builder.build(), null, handler)
            }
        }, handler)
    }

    fun resume() {
        isSaved = false
        saveListener = null
    }

    fun save(listener : OnSavePictureListener) {
        this.saveListener = listener
    }

    fun savePicture() {
        PictureMgr.instance.initDir(context)
        val newFile = PictureMgr.instance.getFile()
        val outputStream = FileOutputStream(newFile)

        val convert = ReadPixesConvert()
        convert.init(curWidth, curHeight)
        val modelData = convert.convertRGBA180()

        val modelBitmap = Bitmap.createBitmap(modelData, curWidth, curHeight, Bitmap.Config.ARGB_8888)
        modelBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
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
}