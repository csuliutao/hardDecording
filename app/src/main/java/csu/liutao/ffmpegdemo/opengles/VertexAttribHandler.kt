package csu.liutao.ffmpegdemo.opengles

import android.content.Context
import android.opengl.GLES30
import android.util.SparseArray
import androidx.core.util.containsKey
import java.lang.Exception
import java.nio.Buffer

/**
 * 在使用相关程序和绘制之间绑定数据
 */
class VertexAttribHandler() {
    private val map = SparseArray<AttribInfo>()
    private var data : Buffer? = null

    fun addLocationInfo(location : Int, info : AttribInfo) {
        map.put(location, info)
    }

    fun addBuffer(buffer: Buffer) {
        data = buffer
    }

    fun bindAttribute(location : Int,offset : Int) {
        if (data == null) throw Exception("no buffer data")
        bindAttribute(location, offset, data!!)
    }

    private fun bindAttribute(location : Int,offset : Int, buffer: Buffer) {
        if (!map.containsKey(location)) throw Exception("find no info about location")
        val info = map.get(location)
        bindAttribute(location, info.demision, info.type, offset, info.stride, buffer)
    }

    private fun bindAttribute(location : Int, demision : Int, type : Int, offset : Int, stride : Int, buffer: Buffer) {
        buffer.position(offset)
        GLES30.glVertexAttribPointer(location, demision, type, false, stride ,buffer)
        buffer.position(0)
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