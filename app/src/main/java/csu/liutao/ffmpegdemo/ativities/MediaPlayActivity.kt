package csu.liutao.ffmpegdemo.ativities

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.VideoDecoder
import java.io.File

class MediaPlayActivity : AppCompatActivity() {
    private lateinit var filePath : File

    private lateinit var textureView: TextureView

    private lateinit var videoDecoder: VideoDecoder

    private val finishListener = object : VideoDecoder.FinishListener {
        override fun onFinished() {
            Utils.log("paly over")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_media_play)

        textureView = findViewById(R.id.play_view)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                Utils.log("onSurfaceTextureSizeChanged")
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                Utils.log("onSurfaceTextureUpdated")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                Utils.log("onSurfaceTextureDestroyed")

                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                videoDecoder = VideoDecoder.Builder()
                    .file(filePath.canonicalPath)
                    .outputSurface(Surface(surface))
                    .finishListener(finishListener)
                    .build()
            }
        }

        filePath = intent.getSerializableExtra(Utils.PLAY_FILE) as File

    }


    override fun onDestroy() {
        super.onDestroy()
        videoDecoder.release()
    }
}