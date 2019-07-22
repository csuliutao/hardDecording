package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface

class CodecManager(val format: MediaFormat, val callback : MediaCodec.Callback, val surface: Surface? = null, val codeFlag : Int = MediaCodec.CONFIGURE_FLAG_ENCODE){
    private var codec : MediaCodec? = null

    fun start() {
        val type = format.getString(MediaFormat.KEY_MIME)
        codec = MediaCodec.createEncoderByType(type)
        codec!!.configure(format, surface, null, codeFlag)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            codec!!.setCallback(callback, subHandler)
        } else {
            codec!!.setCallback(callback)
        }
        codec!!.start()
    }

    fun getOutputFormat() : MediaFormat{
        return codec!!.outputFormat
    }

    fun stop() {
        codec?.stop()
    }

    fun release() {
        codec?.release()
        codec = null
    }

    companion object {
        private val subThread = HandlerThread("CodecManager")
        private lateinit var subHandler: Handler

        init {
            subThread.start()
            subHandler = Handler(subThread.looper)
        }

        fun releaseThread() {
            subThread.quitSafely()
        }
    }
}