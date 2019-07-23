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
//        addH264Cds(format)
        format.setInteger(MediaFormat.KEY_ROTATION, KEY_ROTATION)
        return format
    }

    fun addH264Cds(format: MediaFormat) {
        format.setByteBuffer("csd-0", ByteBuffer.wrap(sps))
        format.setByteBuffer("csd-1", ByteBuffer.wrap(pps))
    }

    fun imageToNV21(image : Image) : ByteArray{
        val width = image.width
        val height = image.height

        val planes = image.planes
        val result = ByteArray(width * height * 3 / 2)

        var stride = planes[0].rowStride

        if (stride == width) {
            planes[0].buffer.get(result, 0, width * height)
        } else {
            for (row in 0 until height) {
                planes[0].buffer.position(row * stride)
                planes[0].buffer.get(result, row * width, width)
            }
        }

        stride = planes[1].rowStride

        val pixelStride = planes[1].pixelStride

        val rowBytesCb = ByteArray(stride)
        val rowBytesCr = ByteArray(stride)

        for (row in 0 until height / (2 * pixelStride)) {
            planes[1].buffer.position(row * stride)
            planes[1].buffer.get(rowBytesCb)

            planes[2].buffer.position(row * stride)
            planes[2].buffer.get(rowBytesCr)

            val rowOffset = width * height + row * width / 2
            for (col in 0 until width / (2 * pixelStride)) {
                result[rowOffset + col * 2] = rowBytesCr[col * pixelStride]
                result[rowOffset + col * 2 + 1] = rowBytesCb[col * pixelStride]
            }
        }
        return result
        /*val plants = image.planes
        val size = plants.size
        if (size != 3) throw Exception("image data is wrong")
        val yBuffer = plants[0].buffer
        val ySize = yBuffer.remaining()
        val vBuffer = plants[2].buffer
        val vSize = vBuffer.remaining()

        val allSize = ySize  + vSize
        val srcByte = ByteArray(allSize)

        Utils.log("y ="+ ySize+",v="+vSize)
        //nV21
        yBuffer.get(srcByte, 0, ySize)
        vBuffer.get(srcByte, ySize , vSize)
        return srcByte*/
    }


    companion object{
        val instance = VideoMgr()
        val KEY_FRAME_RATE = 30
        val KEY_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        val KEY_ROTATION = 90
        val KEY_I_FRAME_INTERVAL = 2

        val sps = byteArrayOf(0, 0, 0, 1, 103, 100, 0, 40, -84, 52, -59, 1, -32, 17, 31, 120, 11, 80, 16, 16, 31, 0, 0, 3, 3, -23, 0, 0, -22, 96, -108)
        val pps = byteArrayOf(0, 0, 0, 1, 104, -18, 60, -128)
    }
}