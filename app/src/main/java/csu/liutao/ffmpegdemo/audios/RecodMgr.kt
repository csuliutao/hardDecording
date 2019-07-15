package csu.liutao.ffmpegdemo.audios

import android.content.Context
import android.media.AudioFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import java.io.File

class RecordMgr private constructor(){
    private val TAG = "RecordMgr"
    private val PATH = "records"

    private var isInitDir = false;

    private lateinit var recordDir : String


    fun initRecordDir(context : Context) {
        if (!isInitDir) {
            isInitDir = true
            recordDir = context.filesDir.canonicalPath + File.separator + PATH
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

    companion object {
        val mgr = RecordMgr()
        val INVALID_POS = -1

        val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

        val SAMPLE_RATE = 44100

        val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO

        val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        val END_TAG =".pcm"
    }
}