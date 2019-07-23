package csu.liutao.ffmpegdemo.aac

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.medias.MediaInfo
import csu.liutao.ffmpegdemo.medias.MediaRunnable
import csu.liutao.ffmpegdemo.medias.MuxerManger
import java.util.concurrent.LinkedBlockingDeque

class AacRecordRunnable(var muxer: MuxerManger, queueSize : Int = 10) :MediaRunnable {
    private val tag = "AacRecordRunnable"
    private lateinit var codecManager: CodecManager
    private val queue = LinkedBlockingDeque<MediaInfo>(queueSize)
    private val audioRecord = AudioRecordManager.instance

    private val codecCallback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (!codecManager.isCodec()) return
            val buffer = codec.getOutputBuffer(index)
            muxer.write(buffer, info, false)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            if (!codecManager.isCodec()) {
                queue.clear()
                return
            }
            val buffer = codec.getInputBuffer(index)
            buffer.clear()

            val info = queue.take()
            buffer.put(info.bytes, info.offset, info.size)
            if (codecManager.isCodec()) codec.queueInputBuffer(index, info.offset, info.size, System.nanoTime() / 1000, 0)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            muxer.addTrack(format, false)
            muxer.start()
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log(tag, "onError")
        }
    }

    override fun run() {
        var length = -1
        while (audioRecord.isRecording()) {
            val bytes = ByteArray(audioRecord.getBufferSize())
            length = audioRecord.read(bytes, 0, audioRecord.getBufferSize())
            if (length > 0) queue.offer(MediaInfo(bytes, 0, length))
        }
    }

    override fun stop() {
        audioRecord.stop()
        codecManager.stop()
    }

    override fun release() {
        codecManager.release()
    }

    fun start() {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
            44100, 2)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioRecord.getBufferSize())
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64000)
        codecManager = CodecManager(format, codecCallback)
        codecManager.start()
        audioRecord.start()
    }
}