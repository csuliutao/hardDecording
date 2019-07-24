package csu.liutao.ffmpegdemo.ativities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.medias.MediaMgr
import csu.liutao.ffmpegdemo.medias.MediaRecord
import java.io.File

class MediaRecordActivity : AppCompatActivity() {
    private lateinit var mediaRecord: MediaRecord
    private lateinit var textureView: TextureView
    private var isStart = false

    private var curFile : File? = null

    private val callback = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean =true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            CodecManager.start()
            curFile = MediaMgr.instance.getNewFile(false)
            mediaRecord = MediaRecord(curFile!!.canonicalPath)
            if (Utils.checkMediaPermission(this@MediaRecordActivity)) mediaRecord.prepare(this@MediaRecordActivity,
                Surface(surface), width, height
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_base_layout)
        textureView = findViewById(R.id.texture)
        textureView.surfaceTextureListener = callback
        textureView.setOnClickListener {
            if (!isStart) {
                isStart = true
                mediaRecord.startRecord()
            } else {
                mediaRecord.saveRecord()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (curFile != null) {
            Utils.log("media setResult")
            val intent = Intent()
            intent.putExtra(MediaMgr.instance.FILE_PATH, curFile!!.canonicalFile)
            setResult(Activity.RESULT_OK, intent)
        }
        mediaRecord.release()
        CodecManager.releaseThread()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Utils.MEDIA_REQUESE_CODE) {
            var size = grantResults.size
            while (size > 0) {
                size--
                if (grantResults[size] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                    return
                }
            }
            mediaRecord.prepare(this, Surface(textureView.surfaceTexture), textureView.width, textureView.height)
        }
    }
}
