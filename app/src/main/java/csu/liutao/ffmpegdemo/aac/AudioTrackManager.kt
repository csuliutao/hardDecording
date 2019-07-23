package csu.liutao.ffmpegdemo.aac

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

class AudioTrackManager private constructor() {
    private var size = -1
    private var audioTrack : AudioTrack? = null

    fun prapare(sampleRate : Int= 44100, channelConfig : Int = AudioFormat.CHANNEL_IN_STEREO, audioFormat : Int = AudioFormat.ENCODING_PCM_16BIT) {
        size = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val attr = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(audioFormat)
            .setChannelMask(channelConfig)
            .build()
        audioTrack = AudioTrack(attr, format, size, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE)
    }

    fun getBufferSize() : Int = size

    fun write(bytes : ByteArray, offset : Int, length : Int) : Int {
        if (!isPlay()) return -1
        return audioTrack!!.write(bytes, offset, length)
    }

    fun isPlay() : Boolean {
        if (audioTrack == null) return false
        return audioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING
    }

    fun start() {
        audioTrack!!.play()
    }

    fun stop() {
        audioTrack!!.stop()
    }

    private fun release() {
        audioTrack!!.release()
        audioTrack = null
    }

    companion object {
        val instance = AudioTrackManager()
        fun release() = instance.release()
    }
}