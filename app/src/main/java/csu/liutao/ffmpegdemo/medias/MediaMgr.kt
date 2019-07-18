package csu.liutao.ffmpegdemo.medias

import android.content.Context
import android.media.MediaMuxer
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.audios.AudioMgr
import java.io.File
import java.util.*

class MediaMgr private constructor(){
    private val PATH = "medias"
    private val END_TAG = ".mp4"
    val VEDIO_H264 = "video/avc"
    val AUDIO_AAC = "audio/mp4a-latm"

    val MEDIA_FORMAT = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4

    private lateinit var mediaDir :String

    fun initDir(context : Context) {
        mediaDir = context.filesDir.canonicalPath + File.separator + PATH
        val dir = File(mediaDir)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun getAllFiles () : List<File> {
        val dir = File(mediaDir)
        val files = dir.listFiles()
        return files.asList()
    }

    fun getNewFile(): File{
        return Utils.getNewFile(mediaDir, END_TAG)
    }


    companion object{
        val instance = MediaMgr()
    }
}