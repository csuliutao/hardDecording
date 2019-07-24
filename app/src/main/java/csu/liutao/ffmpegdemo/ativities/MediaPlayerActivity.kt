package csu.liutao.ffmpegdemo.ativities

import android.app.Activity
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.medias.MediaMgr
import csu.liutao.ffmpegdemo.medias.MediaPlayer
import java.io.File

class MediaPlayerActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var file : File

    private val textureCallback = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            CodecManager.start()
            mediaPlayer = MediaPlayer(file.canonicalPath)
            mediaPlayer.prapare(textureView.surfaceTexture)
            mediaPlayer.play()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_base_layout)
        textureView = findViewById(R.id.texture)
        file = intent.getSerializableExtra(Utils.PLAY_FILE) as File
        textureView.surfaceTextureListener = textureCallback
        textureView.setOnClickListener {
            mediaPlayer.stop()
            finish()
        }
    }

    override fun onBackPressed() {
        mediaPlayer.stop()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        CodecManager.releaseThread()
    }
}