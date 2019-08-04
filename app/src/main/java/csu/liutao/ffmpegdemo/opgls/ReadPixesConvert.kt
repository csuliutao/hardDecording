package csu.liutao.ffmpegdemo.opgls

import android.opengl.GLES30.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

// 为了多次相同类型转换节约性能
open class ReadPixesConvert {
    protected lateinit var result : IntArray
    protected lateinit var byteBuffer: IntBuffer
    protected var size = -1
    protected var curWidth = -1
    protected var curHeight = -1

    fun init(w: Int, h : Int) {
        curHeight = h
        curWidth = w
        size = w * h
        result = IntArray(size)
        byteBuffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    }

    /**
     * 转换rgba888所需像素值, android图片标准格式Bitmap.Config.ARGB_8888
     */
    fun convertRGBA180() : IntArray{
        byteBuffer.position(0)
        glReadPixels(0, 0, curWidth, curHeight, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer)
        byteBuffer.get(result)
        val modelData = IntArray(size)
        var baseOffset = 0
        var pix = -1
        for (i in 0 until curHeight) {
            baseOffset = i * curWidth
            for (k in 0 until  curWidth) {
                pix = result[baseOffset + k]
                modelData[size - baseOffset - curWidth + k] =  pix and -0xff0100 or (pix shl 16 and 0x00ff0000) or (pix shr 16 and 0xff)
            }
        }
        return modelData
    }

    /**
     * 视频格式nv21
     */
    fun convert180YUV() : ByteArray {
        byteBuffer.position(0)
        glReadPixels(0, 0, curWidth, curHeight, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer)
        byteBuffer.get(result)
        var baseOffset = 0
        var pix = -1
        var index = -1

        val yuv = ByteArray(size * 3 / 2)
        var R = -1
        var G = -1
        var B = -1
        var Y = -1
        var U = -1
        var V = -1
        for (i in 0 until curHeight) {
            baseOffset = i * curWidth
            for (k in 0 until  curWidth) {
                pix = result[baseOffset + k]
                index = size - baseOffset - curWidth + k
                B = pix shr 16 and 0xff
                R = pix shl 16 and 0x00ff0000
                G = pix and -0xff0100
                Y = ((66 * R + 129 * G + 25 * B + 128) shr 8) + 16
                yuv[index] = valid(Y, 235)
                if (index % 4 == 1) {
                    U = ((-38 * R - 74 * G + 112 * B) shr 8) + 128
                    yuv[size + index / 4] = valid(U, 239)
                    V = ((112 * R - 94 * G - 18 * B) shr 8) + 128
                    yuv[size + index / 4 + 1] = valid(V, 239)
                }
            }
        }
        return yuv
    }

    fun valid(value : Int, maxV : Int) : Byte{
        val result = if(value < 16) 16 else if (value > maxV) maxV else value
        return result.toByte()
    }

}