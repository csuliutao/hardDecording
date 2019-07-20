package csu.liutao.ffmpegdemo.audios

import android.media.*
import java.io.File

class AudioTrackMgr private constructor(){
    private lateinit var curFile : File
    private var minBuffer: Int = 0
    private var decoderMgr: CodecDecoderMgr? = null
    private var audioTrack : AudioTrack? = null

    lateinit var pauseListener : CodecDecoderMgr.FinishListener

    init {
        minBuffer = AudioTrack.getMinBufferSize(AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT)
        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
            AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT, minBuffer,
            AudioTrack.MODE_STREAM)
    }

    private val listener = object : CodecOutputListener {
        override fun output(bytes: ByteArray) {
            audioTrack!!.write(bytes, 0, bytes.size)
        }
    }

    fun play(file : File){
        pause()
        curFile = file
        replay()
    }

    fun pause(){
        decoderMgr?.stop()
        audioTrack?.pause()
    }

    fun replay(){
        decoderMgr = CodecDecoderMgr.Builder()
            .file(curFile.canonicalPath)
            .outputListener(listener)
            .finishListener(pauseListener)
            .build()
        audioTrack!!.play()
    }

    fun release(){
        CodecDecoderMgr.release()
        audioTrack?.release()
        audioTrack = null
    }


    companion object{
        val instance = AudioTrackMgr()
    }
}