package csu.liutao.ffmpegdemo.aac

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.ReentrantReadWriteLock

class AacRecordRunnable(var muxer: MuxerManger, queueSize : Int = 10) :MediaRunnable {
    private val tag = "AacRecordRunnable"
    private var codecManager: CodecManager? = null
    private val queue = LinkedBlockingDeque<MediaInfo>(queueSize)
    private val audioRecord = AudioRecordManager.instance

    private val lock = ReentrantReadWriteLock()

    private val codecCallback = object : LockCodecCallback(lock) {

        override fun onInput(codec: MediaCodec, index: Int) {
            if (codecManager == null || !codecManager!!.isCodec()) {
                queue.clear()
                return
            }
            val buffer = codec.getInputBuffer(index)
            buffer.clear()

            val info = queue.take()
            buffer.put(info.bytes, info.offset, info.size)
            muxer.setStartTime()
            codec.queueInputBuffer(index, info.offset, info.size, System.nanoTime() / 1000 - muxer.getStartTime(), 0)

        }

        override fun onOutput(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (codecManager == null || !codecManager!!.isCodec()) return
            val buffer = codec.getOutputBuffer(index)
            muxer.write(buffer, info, false)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            muxer.addTrack(format, false)
            muxer.start()
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
        codecManager?.stop()
    }

    override fun release() {
        codecManager?.release()
    }

    fun start() {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
            44100, 2)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioRecord.getBufferSize())
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64000)
        codecManager = CodecManager(format, codecCallback)
        codecManager!!.start()
        audioRecord.start()
    }
}