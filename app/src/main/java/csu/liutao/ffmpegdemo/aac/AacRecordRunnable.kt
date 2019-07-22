package csu.liutao.ffmpegdemo.aac

import android.media.MediaCodec
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.h264.AudioRecordManager
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.medias.MediaRunnable
import csu.liutao.ffmpegdemo.medias.MuxerManger
import java.util.concurrent.LinkedBlockingDeque

class AacRecordRunnable(var muxer: MuxerManger, queueSize : Int = 10) :MediaRunnable {
    private val tag = "AacRecordRunnable"
    private lateinit var codecManager: CodecManager
    private val queue = LinkedBlockingDeque<AacBytesInfo>(queueSize)
    private val audioRecord = AudioRecordManager.instance

    private val codecCallback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val buffer = codec.getOutputBuffer(index)
            muxer.write(buffer, info, false)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val buffer = codec.getInputBuffer(index)
            buffer.clear()

            val info = queue.take()
            buffer.put(info.bytes, info.offset, info.size)
            codec.queueInputBuffer(index, info.offset, info.size, 0, 0)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            muxer.addTrack(format, false)
            muxer.start()
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Utils.log(tag, "onError")
        }
    }

    init {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
            44100, 2)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioRecord.getBufferSize())
        codecManager = CodecManager(format, codecCallback)
        codecManager.start()
        audioRecord.start()
    }

    override fun run() {
        var length = -1
        while (audioRecord.isRecording()) {
            val bytes = ByteArray(audioRecord.getBufferSize())
            length = audioRecord.read(bytes, 0, audioRecord.getBufferSize())
            if (length > 0) queue.offer(AacBytesInfo(bytes, 0, length))
        }
    }

    override fun stop() {
        queue.clear()
        audioRecord.stop()
        codecManager.stop()
    }

    override fun release() {
        codecManager.release()
    }
}