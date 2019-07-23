package csu.liutao.ffmpegdemo.ativities

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.MediaPlayer
import java.io.File

class MediaPlayerActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private var isStart = false
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var file : File

    private val textureCallback = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            mediaPlayer = MediaPlayer(file.canonicalPath)
            mediaPlayer.prapare(Surface(surface))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_base_layout)
        textureView = findViewById(R.id.texture)
        file = intent.getSerializableExtra(Utils.PLAY_FILE) as File
        textureView.surfaceTextureListener = textureCallback
        textureView.setOnClickListener {
            if (!isStart) {
                isStart = true
                mediaPlayer.play()
            } else {
                mediaPlayer.stop()
                mediaPlayer.release()
                finish()
            }
        }
    }
}