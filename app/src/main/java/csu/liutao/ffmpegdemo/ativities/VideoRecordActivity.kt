package csu.liutao.ffmpegdemo.ativities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.media.Image
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.*
import java.io.File
import java.nio.ByteBuffer

class VideoRecordActivity : AppCompatActivity (){
    private lateinit var textureView : TextureView
    private lateinit var button : ImageButton

    private lateinit var camera2Mgr: Camera2Mgr

    private var encoder : VideoEncoder? = null

    private var muxer : MediaMuxer? = null

    private var curFile : File? = null

    private var trackId = -1
    val imageListener = object : Camera2Mgr.ImageListener {
        override fun handleImage(image: Image) {
            val srcByte = VideoMgr.instance.imageToNV21(image)
            encoder?.offer(srcByte)
        }
    }

    private val codecCallback = object : VideoEncoder.Callback {
        override fun onOutputFormatChanged(format: MediaFormat) {
            Utils.log("format changed")
            trackId = muxer!!.addTrack(format)
            muxer!!.start()
        }

        override fun onOutputBufferAvailable(buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
            Utils.log("out data available")
            muxer?.writeSampleData(trackId, buffer, info)
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
            if (Utils.checkCameraPermission(this@VideoRecordActivity)) {
                camera2Mgr.openCamera(this@VideoRecordActivity)
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
        curFile = MediaMgr.instance.getNewFile()

        encoder = VideoEncoder(format, codecCallback)
        encoder!!.start()

        muxer = MediaMuxer(curFile!!.canonicalPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
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