package csu.liutao.ffmpegdemo.opgls

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.Matrix
import csu.liutao.ffmpegdemo.R
import java.nio.FloatBuffer

class TableProgram : IProgram {
    val POSITION = 0
    val MARTIX = 1
    val TEXCOORD = 2
    val TEXUNIT = 3

    var matrix = FloatArray(16)

    val floats = floatArrayOf(
        0f, 0f, 0f, 1.5f, 0.5f, 0.5f,
        -0.75f, 0.75f, 0f, 2f, 0f, 0f,
        0.75f, 0.75f, 0f, 2f, 1f, 0f,
        0.75f, -0.75f, 0f, 1f, 1f, 1f,
        -0.75f, -0.75f, 0f, 1f,0f, 1f,
        -0.75f, 0.75f, 0f, 2f, 0f, 0f
    )
    private lateinit var vetexs : FloatBuffer
    private var textureId = -1

    var curProgram = -1
    val attrHandler = VertexAttribHandler()

    init {
        vetexs = GlUtils.getDirectFloatBuffer(floats)
    }

    override fun prepare(context: Context, vetexId: Int, fragmentId: Int) {
        curProgram = GlUtils.initProgramWithShaderResource(context, vetexId, fragmentId)
        attrHandler.addBuffer(vetexs)
        attrHandler.addLocationInfo(POSITION, VertexAttribHandler.AttribInfo(4, GL_FLOAT, 6 * 4))
        attrHandler.addLocationInfo(TEXCOORD, VertexAttribHandler.AttribInfo(2, GL_FLOAT, 6 * 4))
        textureId = GlUtils.loadTexture(context, R.drawable.beatiful)
    }

    override fun draw() {
        glUseProgram(curProgram)
        glUniformMatrix4fv(MARTIX, 1, false, matrix, 0)

        attrHandler.enableAttribute(TEXCOORD)
        attrHandler.bindAttribute(TEXCOORD, 4)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glUniform1i(TEXUNIT, 0)

        attrHandler.enableAttribute(POSITION)
        attrHandler.bindAttribute(POSITION, 0)
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6)
        attrHandler.disableAttribute(POSITION)
    }

    override fun initScreenSize(width: Int, height: Int) {
        GlUtils.getOMatrix(matrix, width, height)
        val viewFloats = FloatArray(16)
        Matrix.setLookAtM(viewFloats, 0, 0f, 0.1f, 0.1f, 0f, 0f, 0f, 0f, 1f, 0f)
        val pMatrix = FloatArray(16)
        Matrix.perspectiveM(pMatrix, 0, 45f, width.toFloat() / height, 2f, 10f)
        Matrix.translateM(pMatrix, 0, 0f, 0f, -3f)
        Matrix.multiplyMM(matrix, 0, viewFloats, 0, matrix, 0)
        Matrix.multiplyMM(matrix, 0, pMatrix, 0, matrix, 0)
    }
}