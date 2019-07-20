package csu.liutao.ffmpegdemo.audios

import android.media.AudioRecord
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.CodecEncodeMgr
import csu.liutao.ffmpegdemo.Utils
import java.io.FileOutputStream

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

    private var fos : FileOutputStream? = null

    init {
        bufferSize = AudioRecord.getMinBufferSize(AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT)
    }

    private fun prepare() {
        if (audioRecord == null) {
            audioRecord = AudioRecord(
                AudioMgr.AUDIO_SOURCE,
                AudioMgr.SAMPLE_RATE,
                AudioMgr.CHANNEL_CONFIG,
                AudioMgr.AUDIO_FORMAT,
                bufferSize
            )
        }
    }


    fun paly() {
        prepare()
        curState = RecordState.PLAY
        if (!isRecording) {
            isRecording = true
            audioRecord!!.startRecording()
            val file = Utils.getNewFile(AudioMgr.mgr.getRecordDir(), AudioMgr.END_TAG)
            fos = FileOutputStream(file)
            RecordThread().start()
        }
    }

    fun pause() {
        curState = RecordState.PAUSE
        isRecording = false
        AudioRecordMgr.instance.callback.onSucess()
    }

    fun release() {
        audioRecord?.release()
        audioRecord = null
    }

    class RecordThread() : Thread() {
        private lateinit var encoderMgr :CodecEncodeMgr
        private val headerByte = ByteArray(7)

        private val listener = object : CodecEncodeMgr.OutputListener{
            override fun output(bytes: ByteArray) {
                AudioMgr.mgr.addADTStoPacket(headerByte, 7 + bytes.size)
                AudioRecordMgr.instance.fos?.write(headerByte)
                AudioRecordMgr.instance.fos?.write(bytes)
                if (!AudioRecordMgr.instance.isRecording) AudioRecordMgr.instance.fos?.close()
            }
        }

        init {
            val format = AudioMgr.mgr.getAudioBaseFormat()
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AudioRecordMgr.instance.bufferSize)
            encoderMgr = CodecEncodeMgr.Builder()
                .mediaFormat(format)
                .outputLstener(listener)
                .queueSize()
                .build()
        }


        override fun run() {
            super.run()
            val size = AudioRecordMgr.instance.bufferSize
            val byte = ByteArray(size)
            while(AudioRecordMgr.instance.isRecording) {
                val lenth = AudioRecordMgr.instance.audioRecord!!.read(byte, 0, size)
                if (lenth != AudioRecord.ERROR_BAD_VALUE) {
                    encoderMgr.offerInput(byte, 0, lenth)
                }
            }
        }
    }

    interface OnRecordSucess {
        fun onSucess()
    }


    companion object {
        val instance = AudioRecordMgr()
    }

    enum class RecordState (var isRecording : Boolean, var display :String) {
        PLAY(true, "recording"),
        PAUSE(false, "please record next"),
        RELEASE(false, "please record"),
        CANCEL(false, "cancel record")
    }
}