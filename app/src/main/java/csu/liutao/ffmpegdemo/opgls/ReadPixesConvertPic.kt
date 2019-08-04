package csu.liutao.ffmpegdemo.opgls

import android.opengl.GLES30.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

// 为了多次相同类型转换节约性能
class ReadPixesConvertPic {
    private lateinit var result : IntArray
    private lateinit var byteBuffer: IntBuffer
    private var size = -1
    private var curWidth = -1
    private var curHeight = -1

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
        for (i in 0 until curHeight) {
            baseOffset = i * curWidth
            for (k in 0 until  curWidth) {
                modelData[size - baseOffset - curWidth + k] = convertRgba(result[baseOffset + k])
            }
        }
        return modelData
    }

    fun convertRgba(pix : Int) : Int {
        val pb = pix shr 16 and 0xff
        val pr = pix shl 16 and 0x00ff0000
        val pix1 = pix and -0xff0100 or pr or pb
        return pix1
    }

}