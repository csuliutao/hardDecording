package csu.liutao.ffmpegdemo.ativities

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import java.io.File
import java.nio.ByteBuffer

class MediaPlayActivity : AppCompatActivity() {
    private lateinit var filePath : File
    private val extractor = MediaExtractor()
    private val decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

    private lateinit var textureView: TextureView

    private lateinit var mSurface : Surface

    private var played = true

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
                decoder.release()
                extractor.release()
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                mSurface = Surface(surface)
                play()
            }
        }

        filePath = intent.getSerializableExtra(Utils.PLAY_FILE) as File
    }

    private fun play() {
        if (!filePath.exists()) {
            finish()
        }
        extractor.setDataSource(filePath.canonicalPath)
        val format = extractor.getTrackFormat(0)
        val type = format.getString(MediaFormat.KEY_MIME)
        if (type.equals(MediaFormat.MIMETYPE_VIDEO_AVC)) {
            extractor.selectTrack(0)
            decoder.configure(format, mSurface, null, 0)
            decoder.start()
        }

        var sampleSize = 0
        var iindex = 0
        var oindex = 0
        var info = MediaCodec.BufferInfo()
        while (played) {
            iindex = decoder.dequeueInputBuffer(-1)
            if (iindex > -1) {
                val inBuffer = decoder.getInputBuffer(iindex)
                inBuffer.clear()
                sampleSize = extractor.readSampleData(inBuffer, 0)
                if (sampleSize < 0) {
                    break
                }
                extractor.advance()
                decoder.queueInputBuffer(iindex, 0, sampleSize, 0, 0)
            }
            oindex = decoder.dequeueOutputBuffer(info, -1)
            while (oindex > -1) {
                decoder.releaseOutputBuffer(oindex, 0)
                oindex = decoder.dequeueOutputBuffer(info, -1)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        played = false
        decoder.release()
        extractor.release()
    }
}