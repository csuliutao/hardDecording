package csu.liutao.ffmpegdemo.aac

import android.media.MediaCodec
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.medias.ExtractorManager
import csu.liutao.ffmpegdemo.medias.MediaInfo
import java.util.concurrent.LinkedBlockingDeque

class AacPlayer(val curFile : String, queueSize : Int = 10){
    private val tag = "AacPlayer"
    private lateinit var codecManager: CodecManager
    private val queue = LinkedBlockingDeque<MediaInfo>(queueSize)
    private val audioTrack = AudioTrackManager.instance
    private lateinit var extractor : ExtractorManager

    private val callback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val buffer = codec.getOutputBuffer(index)
            val bytes = ByteArray(info.size)
            buffer.get(bytes, info.offset, info.size)
            audioTrack.write(bytes, 0, info.size)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            val length = extractor.read(buffer, 0)
            if (length > 0) codec.queueInputBuffer(index, 0, length, 0, 0)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            val sampleSize = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val cheannel = format.getInteger(MediaFormat.KEY_CHANNEL_MASK)
            val encoding = format.getInteger(MediaFormat.KEY_PCM_ENCODING)
            audioTrack.prapare(sampleSize, cheannel, encoding)
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
        codecManager.start()
    }

    fun stop() {
        queue.clear()
        codecManager.stop()
        audioTrack.stop()
    }

    fun release() {
        codecManager.release()
    }
}