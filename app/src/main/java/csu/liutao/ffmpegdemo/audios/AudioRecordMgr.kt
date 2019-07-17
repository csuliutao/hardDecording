package csu.liutao.ffmpegdemo.audios

import android.media.AudioRecord
import android.util.Log
import csu.liutao.ffmpegdemo.Utils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordMgr private constructor(){
    var curState = RecordState.RELEASE

    private var isRecording = false
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

    private var bufferSize = 0
    private var audioRecord : AudioRecord? = null

    lateinit var callback : OnRecordSucess

    init {
        bufferSize = AudioRecord.getMinBufferSize(AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT)
    }

    private fun prepare() {
        audioRecord = AudioRecord(AudioMgr.AUDIO_SOURCE, AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT, bufferSize)
    }


    fun paly() {
        prepare()
        curState = RecordState.PLAY
        if (!isRecording) {
            isRecording = true
            audioRecord!!.startRecording()

            RecordThread(Utils.getNewFile(TIME_FORMAT, AudioMgr.mgr.getRecordDir(), AudioMgr.END_TAG)).start()
        }
    }

    fun pause() {
        curState = RecordState.PAUSE
        isRecording = false
    }

    private fun release() {
        audioRecord?.release()
        audioRecord = null
    }

    class RecordThread(var file : File) : Thread() {

        override fun run() {
            super.run()
            val fos = FileOutputStream(file)
            val size = AudioRecordMgr.instance.bufferSize
            val byte = ByteArray(size)
            while(AudioRecordMgr.instance.isRecording) {
                val lenth = AudioRecordMgr.instance.audioRecord!!.read(byte, 0, size)
                if (lenth != AudioRecord.ERROR_BAD_VALUE) {
                    fos.write(byte, 0, lenth)
                }
            }
            fos.close()
            AudioRecordMgr.instance.callback.onSucess()
            AudioRecordMgr.instance.release()
        }
    }

    interface OnRecordSucess {
        fun onSucess()
    }


    companion object {
        val instance = AudioRecordMgr()
        val TIME_FORMAT = "YYMMddHHmmss"
    }

    enum class RecordState (var isRecording : Boolean, var display :String) {
        PLAY(true, "recording"),
        PAUSE(false, "please record next"),
        RELEASE(false, "please record"),
        CANCEL(false, "cancel record")
    }
}