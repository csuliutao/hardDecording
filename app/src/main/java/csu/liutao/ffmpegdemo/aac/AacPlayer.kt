package csu.liutao.ffmpegdemo.aac

import android.media.MediaCodec
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.ReentrantReadWriteLock

class AacPlayer(val curFile : String, queueSize : Int = 10){
    private val tag = "AacPlayer"
    private var codecManager: CodecManager? = null
    private val queue = LinkedBlockingDeque<MediaInfo>(queueSize)
    private val audioTrack = AudioTrackManager.instance
    private lateinit var extractor : ExtractorManager
    private val lock = ReentrantReadWriteLock()

    private val callback = object : LockCodecCallback(lock) {

        override fun onInput(codec: MediaCodec, index: Int) {
            if (codecManager == null || !codecManager!!.isCodec()) {
                queue.clear()
                return
            }
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            val info = ExtractorManager.Info()
            val length = extractor.read(buffer, info)
            Utils.log(tag, "input length="+ length)
            if (length > 0) codec.queueInputBuffer(index, 0, length, info.time, info.flag)

        }

        override fun onOutput(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (codecManager == null || !codecManager!!.isCodec()) return
            Utils.log(tag, "output size ="+ info.size)
            val buffer = codec.getOutputBuffer(index)
            val bytes = ByteArray(info.size)
            buffer.get(bytes, info.offset, info.size)
            audioTrack.write(bytes, 0, info.size)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            Utils.log(tag, "onOutputFormatChanged")
            val sampleSize = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val count = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val encoding = format.getInteger(MediaFormat.KEY_PCM_ENCODING)
            audioTrack.prapare(sampleSize, MediaMgr.instance.getChannelMaskByCount(count), encoding)
            audioTrack.start()
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log(tag, "error")
        }
    }

    init {
        extractor = ExtractorManager(curFile, MediaFormat.MIMETYPE_AUDIO_AAC)
        val format = extractor.getExtractorFormat()
        codecManager = CodecManager(format, callback, null, 0)
    }

    fun start() {
        codecManager!!.start()
    }

    fun stop() {
        lock.writeLock().lock()
        codecManager?.stop()
        audioTrack.stop()
        lock.writeLock().unlock()
    }

    fun release() {
        lock.writeLock().lock()
        codecManager?.release()
        lock.writeLock().unlock()
    }
}