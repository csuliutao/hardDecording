package csu.liutao.ffmpegdemo.medias

import android.content.Context
import android.media.Image
import android.media.MediaCodecInfo
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import java.io.File
import java.nio.ByteBuffer
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import java.lang.Math.min


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
        val result = ByteArray(width * height * 3 / 2)

        val yPlant = image.planes[0]
        val yWidth = min(yPlant.rowStride, width)

        val planes = image.planes

        for(row in 0 until height) {
            yPlant.buffer.position(row * yWidth)
            yPlant.buffer.get(result,row * yWidth, yWidth)
        }

        var ySize = yWidth * height

        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val uvStride = min(uPlane.rowStride, yWidth)
        val uvPixel = uPlane.pixelStride

        val rowBytesCb = ByteArray(uvStride)
        val rowBytesCr = ByteArray(uvStride)

        for (row in 0 until height / uvPixel) {
            uPlane.buffer.position(row * uvStride)
            uPlane.buffer.get(rowBytesCb)

            vPlane.buffer.position(row * uvStride)
            vPlane.buffer.get(rowBytesCr)

            val offset = ySize + row * uvStride / uvPixel
            for (col in 0 until uvStride / uvPixel) {
                result[offset + col * 2] = rowBytesCb[col * uvPixel]
                result[offset + col * 2 + 1] = rowBytesCr[col * uvPixel]
            }
        }

        return result
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