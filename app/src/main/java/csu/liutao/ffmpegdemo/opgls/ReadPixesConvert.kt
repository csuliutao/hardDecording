package csu.liutao.ffmpegdemo.opgls

import java.lang.Exception

// 为了大范围转换节约性能
class ReadPixesConvert {
    private var size = -1
    private lateinit var result : IntArray
    private var curWidth = -1
    private var curHeight = -1

    fun init(len : Int, w: Int, h : Int) {
        size = len
        curHeight = h
        curWidth = w
        result = IntArray(size)
    }

    fun convertRGBA180(srcArray : IntArray) {
        if (srcArray.size != size) throw Exception("use error")
        val length = size -1
        var baseOffset = 0
        for (i in 0 until curHeight) {
            baseOffset = i * curWidth
            for (k in 0 until  curWidth) {
                result[length - baseOffset - curWidth + k + 1] = srcArray[baseOffset + k]
            }
        }
        System.arraycopy(result, 0, srcArray, 0, size)
    }

}