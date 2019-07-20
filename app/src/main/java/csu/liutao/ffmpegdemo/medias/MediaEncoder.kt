package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.audios.AudioMgr
import csu.liutao.ffmpegdemo.audios.CodecOutputListener
import java.util.concurrent.ArrayBlockingQueue

open class MediaEncoder private constructor(){
    private var format = AudioMgr.mgr.getAudioBaseFormat()
    private lateinit var mediaCodec : MediaCodec
    private lateinit var queue : ArrayBlockingQueue<Input>

    private lateinit var listener : CodecOutputListener

    private val tag = "MediaEncoder"

    private val subThread = HandlerThread("MediaEncoder")
    private lateinit var subHandler : Handler

    private val callback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val outBuffer = mediaCodec.getOutputBuffer(index)
            val bytes = ByteArray(info.size)
            outBuffer.get(bytes, info.offset, info.size)
            listener.output(bytes)
            mediaCodec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val inBuffer = mediaCodec.getInputBuffer(index)
            inBuffer.clear()
            val input = queue.take()
            inBuffer.put(input.bytes, input.offset, input.size)
            mediaCodec.queueInputBuffer(index, 0, input.size, 0, 0)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            Utils.log(tag, "onOutputFormatChanged")
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log(tag, "onError")
        }
    }

    init {
        subThread.start()
        subHandler = Handler(subThread.looper)
    }

    fun offerInput(inputBytes : ByteArray, offset : Int, size : Int) {
        val bytes = inputBytes.clone()
        val input = Input(bytes, offset, size)
        queue.put(input)
    }

    private fun start() {
        val type = format.getString(MediaFormat.KEY_MIME)
        mediaCodec = MediaCodec.createEncoderByType(type)
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaCodec.setCallback(callback, subHandler)
        } else {
            mediaCodec.setCallback(callback)
        }
        mediaCodec.start()
    }

    fun resetQueue() {
        queue.clear()
    }

    fun release() {
        mediaCodec.release()
        subThread.quitSafely()
    }

    data class Input(var bytes: ByteArray,var offset: Int,var size: Int)

    class Builder {
        private val mgr = MediaEncoder()
        fun mediaFormat(format: MediaFormat) : Builder {
            mgr.format = format
            return this
        }

        fun queueSize(size : Int = 10) : Builder {
            mgr.queue = ArrayBlockingQueue(size)
            return this
        }

        fun outputLstener(listener : CodecOutputListener) : Builder {
            mgr.listener = listener
            return this
        }

        fun build() : MediaEncoder {
            mgr.start()
            return mgr
        }
    }
}