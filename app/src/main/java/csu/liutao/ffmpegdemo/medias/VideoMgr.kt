package csu.liutao.ffmpegdemo.medias

import android.content.Context
import android.media.Image
import android.media.MediaCodecInfo
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import java.io.File
import java.lang.Math.min
import java.nio.ByteBuffer


class VideoMgr private constructor(){
    private val PATH = "medias"
    private val END_TAG = ".mp4"

    private lateinit var mediaDir :String

    fun initDir(context : Context) {
        mediaDir = context.externalCacheDir.canonicalPath + File.separator + PATH
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
        format.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 3)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, KEY_FRAME_RATE)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, KEY_COLOR_FORMAT)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_I_FRAME_INTERVAL)
        format.setInteger(MediaFormat.KEY_ROTATION, KEY_ROTATION)
        return format
    }

    fun imageToNV21(image : Image) : ByteArray{
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4

        val nv21 = ByteArray(ySize + uvSize * 2)

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer


        val yRemaining = yBuffer.remaining()
        var pos = 0

        pos = min(yRemaining, ySize)
        yBuffer.get(nv21, 0, pos)

        val uvPix = image.planes[2].pixelStride
        val uvRemaing = vBuffer.remaining()

        if (uvPix == 2) {
            val length = min(uvSize * 2, uvRemaing)
            val uBytes = ByteArray(length)
            uBuffer.get(uBytes)
            val vBytes = ByteArray(length)
            vBuffer.get(vBytes)

            val uOffset = height / 2 + pos
            for (num in 0 until height / 2) {
                nv21[pos + num] = vBytes[num * 2]
                nv21[uOffset + num] = uBytes[num * 2]
            }

            pos += length
        } else {// uvPix =1
            val length = min(uvSize, uvRemaing)
            vBuffer.get(nv21, pos, length)
            pos += length
            uBuffer.get(nv21, pos, length)
            pos += length
        }
        Utils.log("real size ="+ pos + ",need size ="+ nv21.size)
        return nv21
    }


    companion object{
        val instance = VideoMgr()
        val KEY_FRAME_RATE = 30
        val KEY_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        val KEY_ROTATION = 90
        val KEY_I_FRAME_INTERVAL = 2

        /*val sps = byteArrayOf(0, 0, 0, 1, 103, 100, 0, 40, -84, 52, -59, 1, -32, 17, 31, 120, 11, 80, 16, 16, 31, 0, 0, 3, 3, -23, 0, 0, -22, 96, -108)
        val pps = byteArrayOf(0, 0, 0, 1, 104, -18, 60, -128)*/
    }
}