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
import java.io.File

class MediaRecordActivity : AppCompatActivity() {
    private lateinit var mediaRecord: MediaRecord
    private lateinit var textureView: TextureView
    private var isStart = false

    private lateinit var curFile : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_base_layout)
        textureView = findViewById(R.id.texture)
        textureView.setOnClickListener {
            if (!isStart) {
                isStart = true
                curFile = MediaMgr.instance.getNewFile(false)
                mediaRecord = MediaRecord(curFile.canonicalPath)
                mediaRecord.prepare(this, Surface(textureView.surfaceTexture), textureView.width, textureView.height)
                mediaRecord.startRecord()
            } else {
                isStart = false
                mediaRecord.saveRecord()
                mediaRecord.release()
                finish()
            }
        }
        Utils.checkMediaPermission(this@MediaRecordActivity)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Utils.MEDIA_REQUESE_CODE) {
            var size = grantResults.size
            while (size > 0) {
                size--
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                    return
                }
            }
        }
    }
}