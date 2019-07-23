package csu.liutao.ffmpegdemo.medias

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodecInfo
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import java.io.File

class MediaMgr private constructor(){
    private val VIDEO_PATH = "video"
    private val H264 = ".h264"
    private lateinit var vedioDir :String

    private val MEDIA_PATH = "media"
    private val MP4 = ".mp4"
    private lateinit var mediaDir :String

    fun initDir(context : Context, isVideo : Boolean = true) {
        val dir : File
        if (isVideo) {
            vedioDir = context.externalCacheDir.canonicalPath + File.separator + VIDEO_PATH
            dir = File(vedioDir)
        } else {
            mediaDir = context.externalCacheDir.canonicalPath + File.separator + MEDIA_PATH
            dir = File(mediaDir)
        }
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun getAllFiles (isVideo : Boolean = true) : List<File> {
        val dir : File = if (isVideo) File(vedioDir) else File(mediaDir)
        val files = dir.listFiles()
        return files.asList()
    }

    fun getNewFile(isVideo : Boolean = true): File{
        if (isVideo) return Utils.getNewFile(vedioDir, H264)
        return Utils.getNewFile(mediaDir, MP4)
    }

    fun getH264CodecFromat(width : Int, height: Int) : MediaFormat {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 3)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, KEY_FRAME_RATE)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, KEY_COLOR_FORMAT)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_I_FRAME_INTERVAL)

        format.setInteger(MediaFormat.KEY_ROTATION, KEY_ROTATION)
        return format
    }

    fun getChannelMaskByCount(count : Int) : Int {
        if (count == 2) return AudioFormat.CHANNEL_IN_STEREO
        return AudioFormat.CHANNEL_IN_STEREO

    }

    companion object{
        val instance = MediaMgr()
        val KEY_FRAME_RATE = 30
        val KEY_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        val KEY_ROTATION = 90
        val KEY_I_FRAME_INTERVAL = 2
    }
}