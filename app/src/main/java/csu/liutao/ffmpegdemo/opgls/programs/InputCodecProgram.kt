package csu.liutao.ffmpegdemo.opgls.programs

import android.content.Context
import android.opengl.GLES30
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.OpglFileManger
import csu.liutao.ffmpegdemo.opgls.VertexAttribHandler

class InputCodecProgram : IInputTextureProgram {
    val pos = 0
    val coord = 1
    val unit = 2

    val floats = floatArrayOf(
        -1f, 1f, 0f, 1f,
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        1f, 1f, 1f, 1f
    )

    val vertexs = GlUtils.getDirectFloatBuffer(floats)

    private var curProgram = -1
    private val attribHandler = VertexAttribHandler()

    private var textureId = -1

    override fun prepare(textureId: Int) {
        this.textureId = textureId
    }

    override fun prepare(context: Context, vetexId: Int, fragmentId: Int) {
        curProgram =
            GlUtils.initProgramWithShaderResource(context, vetexId, fragmentId)
        attribHandler.addBuffer(vertexs)

        attribHandler.addLocationInfo(pos,
            VertexAttribHandler.AttribInfo(2, GLES30.GL_FLOAT, 4 * 4)
        )
        attribHandler.addLocationInfo(coord,
            VertexAttribHandler.AttribInfo(2, GLES30.GL_FLOAT, 4 * 4)
        )
    }

    override fun draw() {
        GLES30.glUseProgram(curProgram)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(unit, 0)

        val poss = intArrayOf(pos, coord)
        attribHandler.enableAttribute(poss)
        attribHandler.bindAttribute(coord, 2)
        attribHandler.bindAttribute(pos, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4)
        attribHandler.disableAttribute(poss)
    }

    override fun initScreenSize(width: Int, height: Int) = Unit
}