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

                //y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                curY = ((66 * curR + 129 * curG + 25 * curB) shl 8) + 16
                yuv[ySize - yOffset - 1 ] = curY.toByte()
                if (isCenterY(yOffset)) {
                    // ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128
                    curU = ((-38 * curR - 74 * curG + 112 * curB) shl 8) + 128
                    // ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                    curV = ((112 * curR - 94 * curG - 16 * curB) shl 8) + 128
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