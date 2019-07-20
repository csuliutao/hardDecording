package csu.liutao.ffmpegdemo.audios

import android.media.*
import java.io.File

class AudioTrackMgr private constructor(){
    private var curFile : File? = null
    private var minBuffer: Int = 0
    private lateinit var decoderMgr: CodecDecoderMgr
    private var audioTrack : AudioTrack? = null
        set(value) {
            synchronized(instance) {
                field = value
            }
        }
        get() {
            synchronized(instance) {
                return field
            }
        }
    private var isPaused = false
        set(value) {
            synchronized(this) {
                field = value
            }
        }
        get() {
            synchronized(this) {
                return field
            }
        }
    lateinit var pauseListener : CodecDecoderMgr.FinishListener

    init {
        minBuffer = AudioTrack.getMinBufferSize(AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT)
    }

    private val listener = object : CodecOutputListener {
        override fun output(bytes: ByteArray) {
            if (!isPaused) {
                audioTrack!!.write(bytes, 0, bytes.size)
            }
        }
    }

    fun play(file : File){
        if (curFile != null) pause()
        curFile = file
        replay()
    }

    fun pause(){
        isPaused = true
        audioTrack!!.pause()
        decoderMgr.release()
    }

    fun replay(){
        isPaused = false
        prepare()
        audioTrack!!.play()
        decoderMgr = CodecDecoderMgr.Builder()
            .file(curFile!!.canonicalPath)
            .outputListener(listener)
            .finishListener(pauseListener)
            .build()
    }

    fun prepare() {
        if (audioTrack != null) return
        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
            AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT, minBuffer,
            AudioTrack.MODE_STREAM)
    }

    fun release(){
        decoderMgr.release()
        audioTrack?.release()
        audioTrack = null
    }


    companion object{
        val instance = AudioTrackMgr()
    }
}