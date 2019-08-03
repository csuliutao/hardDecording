package csu.liutao.ffmpegdemo.opgls

import android.opengl.GLES30.*
import java.nio.IntBuffer

// 为了多次相同类型转换节约性能
class ReadPixesConvert {
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
        byteBuffer = IntBuffer.allocate(size)
    }

    fun convertRGBA180() : IntArray{
        glReadPixels(0, 0, curWidth, curHeight, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer)
        val modelData = byteBuffer.array()
        val length = size -1
        var baseOffset = 0
        for (i in 0 until curHeight) {
            baseOffset = i * curWidth
            for (k in 0 until  curWidth) {
                result[length - baseOffset - curWidth + k + 1] = modelData[baseOffset + k]
            }
        }
        System.arraycopy(result, 0, modelData, 0, size)
        return modelData
    }

}