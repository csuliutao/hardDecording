package csu.liutao.ffmpegdemo.ativities

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
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

    private val subThread = HandlerThread("MediaPlayActivity")

    private lateinit var subHandler: Handler

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
//                mSurface = Surface(surface)
                play()
            }
        }

        filePath = intent.getSerializableExtra(Utils.PLAY_FILE) as File

        subThread.start()
        subHandler = Handler(subThread.looper)
    }

    @SuppressLint("NewApi")
    private fun play() {
        if (!filePath.exists()) {
            finish()
        }
        extractor.setDataSource(filePath.canonicalPath)
        val format = extractor.getTrackFormat(0)
        val type = format.getString(MediaFormat.KEY_MIME)
        if (type.equals(MediaFormat.MIMETYPE_VIDEO_AVC)) {
            extractor.selectTrack(0)
            textureView.surfaceTexture.setDefaultBufferSize(format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT))
            mSurface = Surface(textureView.surfaceTexture)
            decoder.configure(format, mSurface, null, 0)

            decoder.setOutputSurface(mSurface)
            decoder.setCallback(object : MediaCodec.Callback() {
                override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    Utils.log("oindex ="+index +", size =" + info.size)
                    decoder.releaseOutputBuffer(index, true)
                }

                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    Utils.log("onInputBufferAvailable index ="+index)
                    if (!played) return
                    val inBuffer = decoder.getInputBuffer(index)
                    inBuffer.clear()
                    val sampleSize = extractor.readSampleData(inBuffer, 0)
                    Utils.log("sampleSize ="+sampleSize)
                    if (sampleSize > 0) {
                        extractor.advance()
                        decoder.queueInputBuffer(index, 0, sampleSize, 0, 0)
                    } else {
                        played = false
                    }
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    Utils.log("onOutputFormatChanged")
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    Utils.log("onError")
                }
            }, subHandler)
            decoder.start()

//            startDecoder()
        }
    }

    private fun startDecoder() {
        var iindex = -1
        var oindex = 0
        var sampleSize = -1
        var info = MediaCodec.BufferInfo()
        while (played) {
            iindex = decoder.dequeueInputBuffer(-1)
            if (iindex > -1) {
                val inBuffer = decoder.getInputBuffer(iindex)
                inBuffer.clear()

                sampleSize = extractor.readSampleData(inBuffer, 0)
                if (sampleSize <= 0) break
                decoder.queueInputBuffer(iindex, 0, sampleSize, 0, 0)
                extractor.advance()
            }
            oindex = decoder.dequeueOutputBuffer(info, -1)
            while (oindex > -1) {
                decoder.releaseOutputBuffer(oindex, true)
                oindex = decoder.dequeueOutputBuffer(info, -1)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        played = false
        decoder.release()
        extractor.release()
        subThread.quitSafely()
    }
}