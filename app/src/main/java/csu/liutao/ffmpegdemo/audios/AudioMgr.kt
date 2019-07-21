package csu.liutao.ffmpegdemo.audios

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.util.Log
import java.io.File

class AudioMgr private constructor(){
    private val TAG = "AudioMgr"
    private val PATH = "audios"

    private var isInitDir = false;

    private lateinit var recordDir : String


    fun initRecordDir(context : Context) {
        if (!isInitDir) {
            isInitDir = true
            recordDir = context.externalCacheDir.canonicalPath + File.separator + PATH
            val file = File(recordDir)
            if (!file.exists()) {
                val sucess = file.mkdirs()
                Log.e("liutao-e", "is sucess =" + sucess);
            } else {
                Log.e("liutao-e", "exist =" + file.canonicalPath);
            }
        }
    }

    fun getRecordDir() : String {
        return recordDir
    }

    fun getFiles() :List<File> {
        val files = File(recordDir).listFiles()
        Log.e("liutao_e", files.size.toString())
        return files.asList()
    }

    fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = KEY_AAC_PROFILE // AAC LC
        val chanCfg = CHANNEL_COUNT // CPE

        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (SAMPLE_RATE shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    fun getAudioBaseFormat() : MediaFormat{
        val format =  MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, AudioMgr.SAMPLE_RATE, AudioMgr.CHANNEL_COUNT)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, AudioMgr.KEY_AAC_PROFILE)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64000)
        return format
    }



    companion object {
        val mgr = AudioMgr()
        val INVALID_POS = -1

        val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

        val SAMPLE_RATE = 44100

        val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO

        val CHANNEL_COUNT = 2

        val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        val KEY_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC

        val END_TAG =".aac"
    }
}