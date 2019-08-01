package csu.liutao.ffmpegdemo.audios

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import csu.liutao.ffmpegdemo.medias.LockCodecCallback
import java.util.concurrent.locks.ReentrantReadWriteLock

// 解码音频的
class AudioDecoder private constructor(){
    private val tag = "AudioDecoder"
    private val extractor = MediaExtractor()
    private var mediaCodec : MediaCodec? = null
    private val audio = "audio"
    private lateinit var outputListener: CodecOutputListener

    private val lock = ReentrantReadWriteLock()

    private val callback = object : LockCodecCallback(lock) {
        override fun onInput(codec: MediaCodec, index: Int) {
            if (mediaCodec == null) return
            val inBuffer = mediaCodec!!.getInputBuffer(index)
            inBuffer.clear()
            val sampleSize = extractor.readSampleData(inBuffer, 0)
            if (sampleSize < 0) {
                outputListener.onfinish()
                return
            }

            extractor.advance()
            mediaCodec!!.queueInputBuffer(index, 0, sampleSize, 0, 0)
        }

        override fun onOutput(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (mediaCodec == null) return
            val outBuffer = mediaCodec!!.getOutputBuffer(index)
            outputListener.output(outBuffer, info)
            mediaCodec!!.releaseOutputBuffer(index, false)
        }
    }

    private val subThread = HandlerThread("AudioDecoder")
    private lateinit var subHandler: Handler

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
                mediaCodec = MediaCodec.createDecoderByType(type)
                mediaCodec!!.configure(format, null, null, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mediaCodec!!.setCallback(callback, subHandler)
                } else {
                    mediaCodec!!.setCallback(callback)
                }
                mediaCodec!!.start()
                break
            }
            i++
        }
    }

    fun pause() {
        lock.writeLock().lock()
        extractor.release()
        mediaCodec?.release()
        mediaCodec = null
        lock.writeLock().unlock()
    }

    fun release() {
        subThread.quitSafely()
    }

    interface FinishListener {
        fun onFinished()
    }

    class Builder {
        private val mgr = AudioDecoder()

        fun file(file: String) : Builder {
            mgr.extractor.setDataSource(file)
            return this
        }

        fun outputListener(listener: CodecOutputListener) : Builder {
            mgr.outputListener = listener
            return this
        }

        fun build() : AudioDecoder{
            mgr.init()
            return mgr
        }
    }
}