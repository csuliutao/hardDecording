package csu.liutao.ffmpegdemo.audios

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import csu.liutao.ffmpegdemo.Utils

// 解码音频的
class CodecDecoderMgr private constructor(){
    private val tag = "CodecDecoderMgr"
    private val extractor = MediaExtractor()
    private lateinit var codec : MediaCodec
    private val audio = "audio"
    private val subThread = HandlerThread("CodecDecoderMgr")
    private lateinit var subHandler: Handler
    private lateinit var outputListener: CodecOutputListener
    private lateinit var finishListener: FinishListener

    private val callback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val outBuffer = codec.getOutputBuffer(index)
            val bytes = ByteArray(info.size)
            outBuffer.get(bytes, info.offset, info.size)
            outputListener.output(bytes)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val inBuffer = codec.getInputBuffer(index)
            inBuffer.clear()
            val sampleSize = extractor.readSampleData(inBuffer, 0)
            if (sampleSize < 0) {
                finishListener.onFinished()
                return
            }

            extractor.advance()
            codec.queueInputBuffer(index, 0, sampleSize, 0, 0)
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


    private fun init() {
        val num = extractor.trackCount
        var i = 0
        while (i < num) {
            val format = extractor.getTrackFormat(i)
            val type = format.getString(MediaFormat.KEY_MIME)
            if (type.startsWith(audio)) {
                extractor.selectTrack(i)
                codec = MediaCodec.createDecoderByType(type)
                codec.configure(format, null, null, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    codec.setCallback(callback, subHandler)
                } else {
                    codec.setCallback(callback)
                }
                codec.start()
                break
            }
            i++
        }
    }

    fun release() {
        extractor.release()
        codec.release()
        subThread.quitSafely()
    }

    interface FinishListener {
        fun onFinished()
    }

    class Builder {
        private val mgr = CodecDecoderMgr()

        fun file(file: String) : Builder {
            mgr.extractor.setDataSource(file)
            return this
        }

        fun outputListener(listener: CodecOutputListener) : Builder {
            mgr.outputListener = listener
            return this
        }

        fun finishListener(listener: FinishListener) : Builder {
            mgr.finishListener = listener
            return this
        }

        fun build() : CodecDecoderMgr{
            mgr.init()
            return mgr
        }
    }
}