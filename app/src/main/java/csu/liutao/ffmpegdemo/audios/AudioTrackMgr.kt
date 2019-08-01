package csu.liutao.ffmpegdemo.audios

import android.media.*
import java.io.File
import java.nio.ByteBuffer

class AudioTrackMgr private constructor() {
    private lateinit var curFile: File
    private var minBuffer: Int = 0
    private var decoder: AudioDecoder? = null
    private var audioTrack: AudioTrack? = null

    lateinit var pauseListener: AudioDecoder.FinishListener

    init {
        minBuffer = AudioTrack.getMinBufferSize(AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT)
    }

    private val listener = object : CodecOutputListener {
        override fun output(byteBuf: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
            val byte = ByteArray(bufferInfo.size)
            byteBuf.get(byte, bufferInfo.offset, bufferInfo.size)
            audioTrack!!.write(byte, 0, byte.size)
        }

        override fun onfinish() {
            pauseListener.onFinished()
        }
    }

    fun play(file: File) {
        pause()
        curFile = file
        replay()
    }

    fun prapare() {
        if (audioTrack == null) audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            AudioMgr.SAMPLE_RATE,
            AudioMgr.CHANNEL_CONFIG,
            AudioMgr.AUDIO_FORMAT,
            minBuffer,
            AudioTrack.MODE_STREAM
        )
    }

    fun pause() {
        decoder?.pause()
        audioTrack?.pause()
    }

    fun replay() {
        decoder = AudioDecoder.Builder()
            .file(curFile.canonicalPath)
            .outputListener(listener)
            .build()
        audioTrack!!.play()
    }

    fun release() {
        decoder?.release()
        audioTrack?.release()
        audioTrack = null
    }


    companion object {
        val instance = AudioTrackMgr()
    }
}