package csu.liutao.ffmpegdemo.audios

import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class AudioRecordMgr private constructor(){
    var curState = RecordState.RELEASE

    private val runnable = object : Runnable{
        override fun run() {
            AudioRecordMgr.instance.startRecord()
        }
    }

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

    private lateinit var fos : FileOutputStream

    private lateinit var encodeMgr : AudioEncoder

    private var headerByte = ByteArray(7)

    private lateinit var file : File

    private val listener = object : CodecOutputListener {
        override fun output(byteBuf: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
            val bytes = ByteArray(bufferInfo.size)
            byteBuf.get(bytes, bufferInfo.offset, bufferInfo.size)
            AudioMgr.mgr.addADTStoPacket(headerByte, 7 + bytes.size)
            fos.write(headerByte)
            fos.write(bytes)
        }

        override fun onfinish() {
            if (!isRecording) return
            isRecording = false
            fos.close()
            AudioRecordMgr.instance.callback.onSucess(file.canonicalPath)
        }
    }

    init {
        bufferSize = AudioRecord.getMinBufferSize(AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_CONFIG, AudioMgr.AUDIO_FORMAT)

        val format = AudioMgr.mgr.getAudioBaseFormat()
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize)
        encodeMgr = AudioEncoder.Builder()
            .mediaFormat(format)
            .outputLstener(listener)
            .queueSize()
            .build()

    }


    fun paly() {
        curState = RecordState.PLAY
        if (!isRecording) {
            isRecording = true
            audioRecord!!.startRecording()
            file = Utils.getNewFile(AudioMgr.mgr.getRecordDir(), AudioMgr.END_TAG)

            fos = FileOutputStream(file)

            Thread(runnable).start()
        }
    }

    private fun startRecord() {
        val byte = ByteArray(bufferSize)
        while(isRecording) {
            val lenth = audioRecord!!.read(byte, 0, bufferSize)
            if (lenth > 0) {
                encodeMgr.offerInput(byte, 0, lenth)
            }
        }
    }

    fun prapare() {
        if (audioRecord == null) {
            audioRecord = AudioRecord(
                AudioMgr.AUDIO_SOURCE,
                AudioMgr.SAMPLE_RATE,
                AudioMgr.CHANNEL_CONFIG,
                AudioMgr.AUDIO_FORMAT,
                bufferSize)
        }
    }

    fun pause() {
        if (!isRecording) return
        audioRecord!!.stop()
        encodeMgr.release()
        curState = RecordState.PAUSE
    }

    fun release() {
        audioRecord?.release()
        audioRecord = null
    }

    interface OnRecordSucess {
        fun onSucess(path : String)
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