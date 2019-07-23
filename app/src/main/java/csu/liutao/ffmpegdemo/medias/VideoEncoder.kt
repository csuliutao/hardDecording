package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque

/**
 * 适合输入为byteBuffer的编码
 */
class VideoEncoder(val format: MediaFormat, val callback :Callback, val queueSize : Int = 15){
    private val tag = "VideoEncoder"
    private lateinit var codecMgr : CodecManager

    private val queue = LinkedBlockingDeque<ByteArray>(queueSize)

    private val codecCallback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val buffer = codec.getOutputBuffer(index)
            callback.onOutputBufferAvailable(buffer, info)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val bytes = queue.take()
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            buffer.put(bytes)
            codec.queueInputBuffer(index, 0, bytes.size, 0, MediaCodec.BUFFER_FLAG_KEY_FRAME)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            callback.onOutputFormatChanged(format)
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log(tag, "onError")
        }
    }

    fun offer(byte : ByteArray) {
        queue.offer(byte)
    }

    fun start() {
        CodecManager.start()
        codecMgr = CodecManager(format, codecCallback)
        codecMgr.start()
    }

    fun getOutputFormat() : MediaFormat{
        return codecMgr.getOutputFormat()
    }

    fun stop() {
        queue.clear()
    }

    fun release() {
        codecMgr.release()
    }

    companion object {
        fun releaseThread() {
            CodecManager.releaseThread()
        }
    }

    interface Callback {
        fun onOutputFormatChanged(format: MediaFormat)
        fun onOutputBufferAvailable(buffer: ByteBuffer, info: MediaCodec.BufferInfo)
    }
}