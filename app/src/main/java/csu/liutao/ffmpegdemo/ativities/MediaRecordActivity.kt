package csu.liutao.ffmpegdemo.ativities

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.MediaMgr
import csu.liutao.ffmpegdemo.medias.MediaRecord

class MediaRecordActivity : AppCompatActivity() {
    private lateinit var mediaRecord: MediaRecord
    private lateinit var textureView: TextureView
    private var isStart = false
    private val textureCallback = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            mediaRecord = MediaRecord(MediaMgr.instance.getNewFile(false).canonicalPath)
            if (Utils.checkCameraPermission(this@MediaRecordActivity)) mediaRecord.prepare(
                this@MediaRecordActivity,
                Surface(surface),
                width,
                height
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_base_layout)
        textureView = findViewById(R.id.texture)
        textureView.surfaceTextureListener = textureCallback
        textureView.setOnClickListener {
            if (!isStart) {
                isStart = true
                mediaRecord.startRecord()
            } else {
                mediaRecord.saveRecord()
                mediaRecord.release()
                finish()
            }
        }

        Utils.checkeAudioPermission(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Utils.CAMERA_REQUESE_CODE || requestCode == Utils.AUDIO_REQUESE_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) finish()
            if (requestCode == Utils.CAMERA_REQUESE_CODE) mediaRecord.prepare(
                this,
                Surface(textureView.surfaceTexture),
                textureView.width,
                textureView.height
            )
        }
    }
}