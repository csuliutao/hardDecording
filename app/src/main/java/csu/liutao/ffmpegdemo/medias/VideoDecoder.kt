package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import csu.liutao.ffmpegdemo.Utils

class VideoDecoder private constructor(){
    private val tag = "VideoDecoder"
    private val extractor = MediaExtractor()
    private lateinit var mediaCodec : MediaCodec
    private val video = "video"
    private lateinit var finishListener: FinishListener
    private lateinit var surface : Surface

    private val callback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            Utils.log(tag, "out size =" + info.size)
            mediaCodec.releaseOutputBuffer(index, true)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
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
            if (type.startsWith(video)) {
                extractor.selectTrack(i)
                mediaCodec = MediaCodec.createDecoderByType(type)
                mediaCodec.configure(format, surface, null, 0)
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

    fun release() {
        mediaCodec.release()
        extractor.release()
    }

    interface FinishListener {
        fun onFinished()
    }

    companion object {
        private val subThread = HandlerThread("VideoDecoder")
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
        private val mgr = VideoDecoder()

        fun file(file: String) : Builder {
            mgr.extractor.setDataSource(file)
            return this
        }

        fun finishListener(listener: FinishListener) : Builder {
            mgr.finishListener = listener
            return this
        }

        fun outputSurface (surface: Surface) : Builder{
            mgr.surface = surface
            return this
        }

        fun build() : VideoDecoder {
            mgr.init()
            return mgr
        }
    }
}