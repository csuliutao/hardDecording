package csu.liutao.ffmpegdemo.audios

import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import android.provider.MediaStore
import android.util.Log
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.MediaMgr
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

            RecordThread(Utils.getNewFile(AudioMgr.mgr.getRecordDir(), AudioMgr.END_TAG)).start()
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
        private val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)

        init {
            val format =  MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_COUNT)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AudioRecordMgr.instance.bufferSize)
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, AudioMgr.KEY_AAC_PROFILE)
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, AudioMgr.SAMPLE_RATE)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 64000)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()
        }

        private fun encodeToFile (srcByte : ByteArray, offset : Int ,size : Int, fos : FileOutputStream) {
            val iindex = encoder.dequeueInputBuffer(-1)
            if (iindex > -1) {
                val inBuffer = encoder.getInputBuffer(iindex)
                inBuffer.clear()
                inBuffer.put(srcByte, offset, size)
                inBuffer.limit(size)
                encoder.queueInputBuffer(iindex, 0, size, 0, 0)
            }

            var info = MediaCodec.BufferInfo()
            var oindex = encoder.dequeueOutputBuffer(info, 0)
            val desByte = ByteArray(AudioRecordMgr.instance.bufferSize)
            while (oindex > -1) {
                val outBuffer = encoder.getOutputBuffer(oindex)
                AudioMgr.mgr.addADTStoPacket(desByte, 7 + info.size)
                outBuffer.position(info.offset)
                outBuffer.limit(info.offset + info.size)
                outBuffer.get(desByte, 7, info.size)
                fos.write(desByte, 0, 7 + info.size)

                encoder.releaseOutputBuffer(oindex, 0)
                oindex = encoder.dequeueOutputBuffer(info, 0)
            }
        }


        override fun run() {
            super.run()
            val fos = FileOutputStream(file)
            val size = AudioRecordMgr.instance.bufferSize
            val byte = ByteArray(size)
            while(AudioRecordMgr.instance.isRecording) {
                val lenth = AudioRecordMgr.instance.audioRecord!!.read(byte, 0, size)
                if (lenth != AudioRecord.ERROR_BAD_VALUE) {
                    encodeToFile(byte, 0, lenth, fos)
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
    }

    enum class RecordState (var isRecording : Boolean, var display :String) {
        PLAY(true, "recording"),
        PAUSE(false, "please record next"),
        RELEASE(false, "please record"),
        CANCEL(false, "cancel record")
    }
}