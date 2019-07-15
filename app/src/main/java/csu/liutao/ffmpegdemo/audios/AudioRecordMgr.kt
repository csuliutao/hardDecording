package csu.liutao.ffmpegdemo.audios

import android.media.AudioRecord
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordMgr private constructor(){
    var curState = RecordState.RELEASE

    private var isRecording = false

    private var bufferSize = 0
    private var audioRecord : AudioRecord? = null

    lateinit var callback : OnRecordSucess

    fun setRecordState(state :Boolean) {
        synchronized(instance) {
            isRecording = state
        }
    }

    fun isInRecording() : Boolean {
        synchronized(instance) {
            return isRecording
        }
    }

    private fun prepare() {
        bufferSize = AudioRecord.getMinBufferSize(RecordMgr.SAMPLE_RATE, RecordMgr.CHANNEL_CONFIG, RecordMgr.AUDIO_FORMAT)
        audioRecord = AudioRecord(RecordMgr.AUDIO_SOURCE, RecordMgr.SAMPLE_RATE, RecordMgr.CHANNEL_CONFIG, RecordMgr.AUDIO_FORMAT, bufferSize)
    }


    fun paly() {
        prepare()
        curState = RecordState.PLAY
        if (!isInRecording()) {
            setRecordState(true)
            audioRecord!!.startRecording()

            RecordThread(getNewFile()).start()
        }
    }

    private fun getNewFile(): File {
        val formater = SimpleDateFormat(TIME_FORMAT)
        val date = Calendar.getInstance()
        val name = formater.format(date.time)+RecordMgr.END_TAG
        Log.e("liutao-e", name)
        val file = File(RecordMgr.mgr.getRecordDir(), name)
        file.createNewFile()
        return file
    }

    fun pause() {
        curState = RecordState.PAUSE
        setRecordState(false)
    }

    private fun release() {
        audioRecord!!.release()
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