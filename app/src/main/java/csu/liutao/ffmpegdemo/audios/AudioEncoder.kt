package csu.liutao.ffmpegdemo.audios

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import csu.liutao.ffmpegdemo.medias.LockCodecCallback
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.locks.ReentrantReadWriteLock

open class AudioEncoder private constructor(){
    private var format = AudioMgr.mgr.getAudioBaseFormat()
    private var mediaCodec : MediaCodec? = null
    private lateinit var queue : ArrayBlockingQueue<Input>

    private lateinit var listener : CodecOutputListener

    private val tag = "AudioEncoder"

    private val subThread = HandlerThread("AudioEncoder")
    private lateinit var subHandler : Handler

    private val lock = ReentrantReadWriteLock()

    private val callback = object : LockCodecCallback(lock) {
        override fun onInput(codec: MediaCodec, index: Int) {
            if (mediaCodec == null) {
                queue.clear()
                listener.onfinish()
                return
            }
            val inBuffer = mediaCodec!!.getInputBuffer(index)
            inBuffer.clear()
            val input = queue.take()
            inBuffer.put(input.bytes, input.offset, input.size)
            mediaCodec!!.queueInputBuffer(index, 0, input.size, 0, 0)
        }

        override fun onOutput(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (mediaCodec == null) return
            val outBuffer = mediaCodec!!.getOutputBuffer(index)
            listener.output(outBuffer, info)
            mediaCodec!!.releaseOutputBuffer(index, false)
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
        mediaCodec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaCodec!!.setCallback(callback, subHandler)
        } else {
            mediaCodec!!.setCallback(callback)
        }
        mediaCodec!!.start()
    }

    fun release() {
        lock.writeLock().lock()
        mediaCodec?.release()
        mediaCodec = null
        subThread.quitSafely()
        lock.writeLock().unlock()
    }

    data class Input(var bytes: ByteArray,var offset: Int,var size: Int)

    class Builder {
        private val mgr = AudioEncoder()
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

        fun build() : AudioEncoder {
            mgr.start()
            return mgr
        }
    }
}