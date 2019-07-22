package csu.liutao.ffmpegdemo.h264

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.lang.Exception

class AudioRecordManager {
    private val sampleRate = 44100

    private var bufferSize = -1

    private var record : AudioRecord? = null


    init {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
    }

    fun getBufferSize() : Int = bufferSize

    fun isRecording () : Boolean {
        if (record == null) false
        return record!!.recordingState == AudioRecord.RECORDSTATE_RECORDING
    }

    fun start() {
        record = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        record?.startRecording()
    }

    fun read(bytes : ByteArray, offset : Int, length: Int) : Int {
        if (record == null) throw Exception("audio record no init")
        return record!!.read(bytes, offset, length)
    }

    fun stop(){
        record?.stop()
    }

    private fun release() {
        record?.release()
        record = null
    }

    companion object {
        val instance = AudioRecordManager()
        fun release() = instance.release() // 不再使用释放
    }
}