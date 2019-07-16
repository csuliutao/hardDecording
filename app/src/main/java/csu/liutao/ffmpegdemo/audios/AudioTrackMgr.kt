package csu.liutao.ffmpegdemo.audios

import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.io.File
import java.io.FileInputStream

class AudioTrackMgr private constructor(){
    private var curFile : File? = null
    private var minBuffer: Int = 0
    private var audioTrack : AudioTrack? = null
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
    private var lastCount = 0L
    var pauseListener : FinishListener? = null

    init {
        minBuffer = AudioTrack.getMinBufferSize(AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT)
    }

    fun play(file : File){
        if (curFile != null) pause()
        curFile = file
        lastCount = 0
        replay()
    }

    fun pause(){
        isPaused = true
        release()
    }

    fun replay(){
        isPaused = false
        prepare()
        TrackThread().start()
    }

    fun prepare() {
        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
            AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT, minBuffer,
            AudioTrack.MODE_STREAM)
    }

    fun release(){
        audioTrack?.release()
        audioTrack = null
    }

    class TrackThread() : Thread() {

        override fun run() {
            super.run()
            val size = AudioTrackMgr.instance.minBuffer
            val buffer = ByteArray(size)
            val fread = FileInputStream(AudioTrackMgr.instance.curFile)

            fread.skip(AudioTrackMgr.instance.lastCount)
            Log.e("liutao-e","skip num = "+ AudioTrackMgr.instance.lastCount)

            var eachLength = fread.read(buffer, 0, size)

            AudioTrackMgr.instance.audioTrack!!.play()
            while (-1 != eachLength && !AudioTrackMgr.instance.isPaused) {
                AudioTrackMgr.instance.audioTrack!!.write(buffer, 0, eachLength)
                AudioTrackMgr.instance.lastCount += eachLength
                eachLength = fread.read(buffer, 0, size)
            }
            if (-1 == eachLength) {
                AudioTrackMgr.instance.lastCount = 0
                AudioTrackMgr.instance.pauseListener?.onFinished()
            }
            fread.close()
        }
    }

    interface FinishListener {
        fun onFinished()
    }


    companion object{
        val instance = AudioTrackMgr()
    }
}