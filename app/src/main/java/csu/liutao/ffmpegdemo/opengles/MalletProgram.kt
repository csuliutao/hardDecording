package csu.liutao.ffmpegdemo.opengles

import android.content.Context
import java.nio.FloatBuffer
import android.opengl.GLES30.*
import android.opengl.Matrix

class MalletProgram : IProgram {
    val matrix = 0
    val pos = 1
    val color = 2
    val pointSize = 3

    private val floats = floatArrayOf(
        0f, 0.3f, 0f, 1.75f, 1f, 0f, 0f,
        0f, -0.3f, 0f, 1.25f, 0f, 1f, 0f
    )
    private val matArray = FloatArray(16)
    private lateinit var vetexs : FloatBuffer
    private var curProgram = -1
    private var attrHandler = VertexAttribHandler()

    init {
        vetexs = GlUtils.getDirectFloatBuffer(floats)
    }

    override fun prepare(context: Context, vetexId: Int, fragmentId: Int) {
        curProgram = GlUtils.initProgramWithShaderResource(context, vetexId, fragmentId)
        attrHandler.addBuffer(vetexs)
        attrHandler.addLocationInfo(pos, VertexAttribHandler.AttribInfo(4, GL_FLOAT, 7 * 4))
        attrHandler.addLocationInfo(color, VertexAttribHandler.AttribInfo(3, GL_FLOAT, 7 * 4))
    }

    override fun draw() {
        glUseProgram(curProgram)
        glUniformMatrix4fv(matrix, 1, false, matArray, 0)
        glVertexAttrib1f(pointSize, 20f)
        val poss = intArrayOf(pos, color)
        attrHandler.enableAttribute(poss)
        attrHandler.bindAttribute(pos, 0)
        attrHandler.bindAttribute(color, 4)
        glDrawArrays(GL_POINTS, 0, 2)
        attrHandler.disableAttribute(poss)
    }

    override fun initScreenSize(width: Int, height: Int) {
        GlUtils.getOMatrix(matArray, width, height)
        val viewFloats = FloatArray(16)
        Matrix.setLookAtM(viewFloats, 0, 0f, 1f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
        val pMatrix = FloatArray(16)
        Matrix.perspectiveM(pMatrix, 0, 45f, width.toFloat() / height, 2f, 10f)
        Matrix.translateM(pMatrix, 0, 0f, 0f, -3f)
        Matrix.multiplyMM(matArray, 0, viewFloats, 0, matArray, 0)
        Matrix.multiplyMM(matArray, 0, pMatrix, 0, matArray, 0)
    }
}