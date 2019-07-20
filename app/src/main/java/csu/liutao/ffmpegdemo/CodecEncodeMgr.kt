package csu.liutao.ffmpegdemo

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import csu.liutao.ffmpegdemo.audios.AudioMgr
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

class CodecEncodeMgr private constructor(){
    private var format = AudioMgr.mgr.getAudioBaseFormat()
    private lateinit var codec : MediaCodec
    private lateinit var queue : ArrayBlockingQueue<Input>

    private lateinit var listener : OutputListener

    private val subThread = HandlerThread("CodecEncodeMgr")
    private lateinit var subHandler : Handler

    private val tag = "CodecEncodeMgr"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            subThread.start()
            subHandler = Handler(subThread.looper)
        }
    }

    private val callback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val outBuffer = codec.getOutputBuffer(index)
            val bytes = ByteArray(info.size)
            outBuffer.get(bytes, info.offset, info.size)
            listener.output(bytes)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val inBuffer = codec.getInputBuffer(index)
            inBuffer.clear()
            val input = queue.take()
            inBuffer.put(input.bytes, input.offset, input.size)
            codec.queueInputBuffer(index, 0, input.size, 0, 0)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            Utils.log(tag, "onOutputFormatChanged")
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log(tag, "onError")
        }
    }

    fun offerInput(inputBytes : ByteArray, offset : Int, size : Int) {
        val bytes = inputBytes.clone()
        val input = Input(bytes, offset, size)
        queue.put(input)
    }

    private fun start() {
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            codec.setCallback(callback, subHandler)
        } else {
            codec.setCallback(callback)
        }
        codec.start()
    }


    fun release () {
        codec.release()
        subThread.quitSafely()
    }

    interface OutputListener {
        fun output(bytes : ByteArray)
    }

    data class Input(var bytes: ByteArray,var offset: Int,var size: Int)

    class Builder {
        private val mgr = CodecEncodeMgr()
        fun mediaFormat(format: MediaFormat) : Builder {
            mgr.format = format
            return this
        }

        fun queueSize(size : Int = 10) : Builder {
            mgr.queue = ArrayBlockingQueue(size)
            return this
        }

        fun outputLstener(listener : OutputListener) : Builder{
            mgr.listener = listener
            return this
        }

        fun build() : CodecEncodeMgr {
            val type = mgr.format.getString(MediaFormat.KEY_MIME)
            mgr.codec = MediaCodec.createEncoderByType(type)
            mgr.start()
            return mgr
        }
    }
}