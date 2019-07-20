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
    private lateinit var mediaCodec : MediaCodec
    private val audio = "audio"
    private lateinit var outputListener: CodecOutputListener
    private lateinit var finishListener: FinishListener
    private var isStop = false

    private val callback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (isStop) {
                release()
                return
            }
            val outBuffer = mediaCodec.getOutputBuffer(index)
            val bytes = ByteArray(info.size)
            outBuffer.get(bytes, info.offset, info.size)
            outputListener.output(bytes)
            mediaCodec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            if (isStop) return
            val inBuffer = mediaCodec.getInputBuffer(index)
            inBuffer.clear()
            val sampleSize = extractor.readSampleData(inBuffer, 0)
            if (sampleSize < 0) {
                finishListener.onFinished()
                return
            }

            extractor.advance()
            mediaCodec.queueInputBuffer(index, 0, sampleSize, 0, 0)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            Utils.log(tag, "onOutputFormatChanged")
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log(tag, "onError")
        }
    }


    private fun init() {
        val num = extractor.trackCount
        var i = 0
        while (i < num) {
            val format = extractor.getTrackFormat(i)
            val type = format.getString(MediaFormat.KEY_MIME)
            if (type.startsWith(audio)) {
                extractor.selectTrack(i)
                mediaCodec = MediaCodec.createDecoderByType(type)
                mediaCodec.configure(format, null, null, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mediaCodec.setCallback(callback, subHandler)
                } else {
                    mediaCodec.setCallback(callback)
                }
                mediaCodec.start()
                break
            }
            i++
        }
    }

    fun stop() {
        isStop = true
    }

    private fun release() {
        mediaCodec.release()
        extractor.release()
    }

    interface FinishListener {
        fun onFinished()
    }

    companion object {
        private val subThread = HandlerThread("CodecDecoderMgr")
        private lateinit var subHandler: Handler

        init {
            subThread.start()
            subHandler = Handler(subThread.looper)
        }

        fun release() {
            subThread.quitSafely()
        }
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