package csu.liutao.ffmpegdemo.opgls.programs

import android.content.Context
import android.opengl.GLES30.*
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.OpglFileManger
import csu.liutao.ffmpegdemo.opgls.VertexAttribHandler
import csu.liutao.ffmpegdemo.opgls.abs.IProgram

class ImgProgram : IProgram {
    val pos = 0
    val coord = 1
    val unit = 2
    /*val red = 3
    val green = 4
    val blue = 5
    val alpha = 6*/

    val floats = floatArrayOf(
        -1f, 1f, 0f, 0f,
        -1f, -1f, 0f, 1f,
        1f, -1f, 1f, 1f,
        1f, 1f, 1f, 0f
    )

    val vertexs = GlUtils.getDirectFloatBuffer(floats)

    private var curProgram = -1
    private val attribHandler = VertexAttribHandler()

    private var textureId = -1


    override fun prepare(context: Context, vetexId: Int, fragmentId: Int) {
        curProgram =
            GlUtils.initProgramWithShaderResource(context, vetexId, fragmentId)
        attribHandler.addBuffer(vertexs)

        attribHandler.addLocationInfo(pos,
            VertexAttribHandler.AttribInfo(2, GL_FLOAT, 4 * 4)
        )
        attribHandler.addLocationInfo(coord,
            VertexAttribHandler.AttribInfo(2, GL_FLOAT, 4 * 4)
        )
        textureId = GlUtils.loadTexture(context, OpglFileManger.instance.getLastPic())
    }

    override fun draw() {
        glUseProgram(curProgram)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glUniform1i(unit, 0)

        /*glUniform1f(red, 0.2f)
        glUniform1f(green, 0.5f)
        glUniform1f(blue, 0.9f)
        glUniform1f(alpha, 0.7f)*/

        val poss = intArrayOf(pos, coord)
        attribHandler.enableAttribute(poss)
        attribHandler.bindAttribute(coord, 2)
        attribHandler.bindAttribute(pos, 0)

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
//        glDrawArrays(GL_TRIANGLES, 1, 3)
        attribHandler.disableAttribute(poss)
    }

    /**
     * 展示图片，不做正交，透视转换
     */
    override fun initScreenSize(width: Int, height: Int) = Unit
}