package csu.liutao.ffmpegdemo.ativities

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.media.Image
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.Camera2Mgr
import csu.liutao.ffmpegdemo.medias.VideoEncoder
import csu.liutao.ffmpegdemo.medias.MediaMgr
import csu.liutao.ffmpegdemo.medias.MuxerManger
import java.nio.ByteBuffer

class MediaRecordActivity : AppCompatActivity (){
    private lateinit var textureView : TextureView
    private lateinit var button : ImageButton

    private lateinit var camera2Mgr: Camera2Mgr

    private var encoder : VideoEncoder? = null

    private var muxer : MuxerManger? = null

    val imageListener = object : Camera2Mgr.ImageListener {
        override fun handleImage(image: Image) {
            val plants = image.planes
            val size = plants.size
            if (size != 3) throw Exception("image data is wrong")
            val yBuffer = plants[0].buffer
            val ySize = yBuffer.remaining()
            val vBuffer = plants[2].buffer
            val vSize = vBuffer.remaining()

            val allSize = ySize  + vSize
            val srcByte = ByteArray(allSize)

            Utils.log("y ="+ ySize+",v="+vSize)
            //nV21
            yBuffer.get(srcByte, 0, ySize)
            vBuffer.get(srcByte, ySize , vSize)

            encoder?.offer(srcByte)
        }
    }

    private val codecCallback = object : VideoEncoder.Callback {
        override fun onOutputFormatChanged(format: MediaFormat) {
            Utils.log("format changed")
            muxer!!.addTrack(format)
            muxer!!.start()
        }

        override fun onOutputBufferAvailable(buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
            Utils.log("out data available")
            muxer?.write(buffer, info)
        }
    }


    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            camera2Mgr = Camera2Mgr.Builder()
                .surface(Surface(surface), false)
                .imageReader(textureView.width, textureView.height, imageListener)
                .build()
            if (Utils.checkCameraPermission(this@MediaRecordActivity)) {
                camera2Mgr.openCamera(this@MediaRecordActivity)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_record)
        textureView = findViewById(R.id.preview)
        button = findViewById(R.id.take)

        textureView.surfaceTextureListener = surfaceTextureListener
        button.setOnClickListener{
            if (muxer == null) {
                initCodec()
                camera2Mgr.take()
            } else {
                camera2Mgr.stop()
                encoder?.stop()
                muxer?.stop()
                finish()
            }
        }
    }

    private fun initCodec() {
        val format = MediaMgr.instance.getH264CodecFromat(textureView.width, textureView.height)
        val curFile = MediaMgr.instance.getNewFile()

        encoder = VideoEncoder(format, codecCallback)
        encoder!!.start()

        muxer = MuxerManger(curFile.canonicalPath)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Utils.CAMERA_REQUESE_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            } else {
                camera2Mgr.openCamera(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        camera2Mgr.release()
        encoder?.release()
        VideoEncoder.releaseThread()
        muxer?.release()
    }
}