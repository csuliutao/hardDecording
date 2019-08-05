package csu.liutao.ffmpegdemo.opgls

import android.opengl.GLES30
import android.util.SparseArray
import androidx.core.util.containsKey
import csu.liutao.ffmpegdemo.Utils
import java.lang.Exception
import java.nio.Buffer
import java.nio.FloatBuffer

/**
 * 在使用相关程序和绘制之间绑定数据
 */
class VertexAttribHandler() {
    private val map = SparseArray<AttribInfo>()
    private var data : FloatBuffer? = null

    private var vboId = -1
    private var needVbo = false

    fun addLocationInfo(location : Int, info : AttribInfo) {
        map.put(location, info)
    }

    fun addBuffer(buffer: FloatBuffer, need : Boolean = false) {
        needVbo = need
        data = buffer
        vboId = GlUtils.genVboId(buffer as FloatBuffer)
    }

    fun bindAttribute(location : Int,offset : Int) {
        if (data == null) throw Exception("no buffer data")
        if (!map.containsKey(location)) throw Exception("find no info about location")
        val info = map.get(location)
        if (needVbo) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
            //gles中偏移量都是对于byte来说，也就是一个字节，而android中便宜都是对于数据类型来说的，重点重点
            GLES30.glVertexAttribPointer(location, info.demision, info.type, false, info.stride, offset * 4)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        } else {
            bindAttribute(location, info.demision, info.type, offset, info.stride, data!!)
        }
    }

    private fun bindAttribute(location : Int, demision : Int, type : Int, offset : Int, stride : Int, buffer: Buffer) {
        buffer.position(offset)
        GLES30.glVertexAttribPointer(location, demision, type, false, stride ,buffer)
        buffer.position(0)
    }

    private fun bindAttribute(location : Int, demision : Int, type : Int, offset : Int, stride : Int) {

    }

    fun enableAttribute(location: Int) {
        GLES30.glEnableVertexAttribArray(location)
    }

    fun enableAttribute(locations : IntArray) {
        for (location in locations) {
            enableAttribute(location)
        }
    }

    fun disableAttribute(location : Int) {
        GLES30.glDisableVertexAttribArray(location)
    }

    fun disableAttribute(locations : IntArray) {
        for (location in locations) {
            GLES30.glDisableVertexAttribArray(location)
        }
    }

    data class AttribInfo(val demision : Int, val type : Int, val stride : Int)
}