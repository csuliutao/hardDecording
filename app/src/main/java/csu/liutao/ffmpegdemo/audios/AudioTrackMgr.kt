package csu.liutao.ffmpegdemo.audios

import android.media.*
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

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
        private val decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        private val extractor = MediaExtractor()

        init {
            extractor.setDataSource(AudioTrackMgr.instance.curFile!!.canonicalPath)
            val format = extractor.getTrackFormat(0)
            val type = format.getString(MediaFormat.KEY_MIME)
            if (type.startsWith("audio")) {
                extractor.selectTrack(0)
//                format.setInteger(MediaFormat.KEY_IS_ADTS, 1)
//                format.setInteger(MediaFormat.KEY_AAC_PROFILE, AudioMgr.KEY_AAC_PROFILE)
//                format.setInteger(MediaFormat.KEY_BIT_RATE, 64000)
                decoder.configure(format, null, null, 0)
                decoder.start()
            } else {
                throw Exception("aac file wrong!")
            }
        }

        override fun run() {
            super.run()
            val size = AudioTrackMgr.instance.minBuffer
            val buffer = ByteArray(size)
            var info = MediaCodec.BufferInfo()

            AudioTrackMgr.instance.audioTrack!!.play()
            while (!AudioTrackMgr.instance.isPaused) {
                val iindex = decoder.dequeueInputBuffer(-1)
                if (iindex < -1) {
                    return
                }
                val inBuffer = decoder.getInputBuffer(iindex)
                inBuffer.clear()
                val inSize = extractor.readSampleData(inBuffer, 0)
                if (inSize < 0)
                {
                    AudioTrackMgr.instance.lastCount = 0
                    AudioTrackMgr.instance.pauseListener?.onFinished()
                    return
                }
                extractor.advance()
                decoder.queueInputBuffer(iindex, 0, inSize, 0, 0)

                var oindex = decoder.dequeueOutputBuffer(info, 0)

                while (oindex > -1) {
                    val outBuffer = decoder.getOutputBuffer(oindex)
                    outBuffer.position(info.offset)
                    outBuffer.limit(info.size)
                    outBuffer.get(buffer, 0, info.size)
                    outBuffer.clear()
                    AudioTrackMgr.instance.audioTrack!!.write(buffer, 0, info.size)
                    decoder.releaseOutputBuffer(oindex, 0)
                    oindex = decoder.dequeueOutputBuffer(info, 0)
                }
            }
        }
    }

    interface FinishListener {
        fun onFinished()
    }


    companion object{
        val instance = AudioTrackMgr()
    }
}