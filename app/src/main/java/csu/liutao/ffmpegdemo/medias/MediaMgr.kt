package csu.liutao.ffmpegdemo.medias

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.audios.AudioMgr
import java.io.File
import java.util.*

class MediaMgr private constructor(){
    private val PATH = "medias"
    private val END_TAG = ".mp4"

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

    fun getH264CodecFromat(width : Int, height: Int) : MediaFormat {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE,width * height)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, KEY_FRAME_RATE)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, KEY_COLOR_FORMAT)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_I_FRAME_INTERVAL)

        format.setInteger(MediaFormat.KEY_ROTATION, KEY_ROTATION)
        return format
    }


    companion object{
        val instance = MediaMgr()
        val KEY_FRAME_RATE = 30
        val KEY_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        val KEY_ROTATION = 90
        val KEY_I_FRAME_INTERVAL = 2
    }
}