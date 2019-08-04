package csu.liutao.ffmpegdemo.opgls

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ReadPixesConvertVedio {
    private val step = 3
    private lateinit var result : ByteArray
    private lateinit var byteBuffer: ByteBuffer
    private var size = -1
    private var curWidth = -1
    private var curHeight = -1

    fun init(w: Int, h : Int) {
        curHeight = h
        curWidth = w
        size = w * h * step
        result = ByteArray(size)
        byteBuffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder())
    }

    /**
     * 视频格式nv21
     */
    fun convert180YUV() : ByteArray {
        byteBuffer.position(0)
        GLES30.glReadPixels(0, 0, curWidth, curHeight, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, byteBuffer)
        byteBuffer.get(result)
        val ySize = curWidth * curHeight
        val uvSize = ySize / 2
        val yuv = ByteArray(ySize + uvSize)
        var curR = -1
        var curG = -1
        var curB = -1
        var curY = -1
        var curU = -1
        var curV = -1
        var resultOffset = 0
        var yOffset = 0
        for (i in 0 until curHeight) {
            for (k in 0 until  curWidth) {
                resultOffset = i * curWidth * step + k * step
                yOffset = i * curWidth + k

                curR = result[resultOffset].toInt()
                curG = result[resultOffset + 1].toInt()
                curB = result[resultOffset + 2].toInt()

                //Y = 0.299 R + 0.587 G + 0.114 B
                curY = (0.299 * curR + 0.587 * curG + 0.114 * curB).toInt()
                yuv[ySize - yOffset - 1 ] = curY.toByte()
                if (isCenterY(yOffset)) {
                    // U = - 0.1687 R - 0.3313 G + 0.5 B + 128
                    curU = (-0.1687 * curR - 0.3313 * curG + 0.5 * curB).toInt() + 128
                    // V = 0.5 R - 0.4187 G - 0.0813 B + 128
                    curV = (0.5 * curR - 0.4187 * curG - 0.0813 * curB).toInt() + 128
                    yuv[ySize + uvSize - yOffset / 4 - 1] = curU.toByte()
                    yuv[ySize + uvSize - yOffset / 4 - 2] = curV.toByte()
                }
            }
        }
        return yuv
    }

    fun isCenterY(pos : Int) : Boolean{
        return (pos % 4) == 2
    }
}