package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread

class CodecManager(val format: MediaFormat, val callback : MediaCodec.Callback){
    private var codec : MediaCodec? = null

    init {
        val type = format.getString(MediaFormat.KEY_MIME)
        codec = MediaCodec.createEncoderByType(type)
        codec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            codec!!.setCallback(callback, subHandler)
        } else {
            codec!!.setCallback(callback)
        }
        codec!!.start()
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

        fun release() {
            subThread.quitSafely()
        }
    }
}