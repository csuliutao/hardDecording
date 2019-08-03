package csu.liutao.ffmpegdemo.opgls.programs

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES30.*
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.VertexAttribHandler
import csu.liutao.ffmpegdemo.opgls.camera.ICameraProgram

class CameraProgram : ICameraProgram {
    private val attribHandler = VertexAttribHandler()
    private var programId = -1

    val floats = floatArrayOf(
        -1f, 1f, 0f, 1f,
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        1f, 1f, 1f, 1f
    )

    val pos = 0
    val coord = 1
    val coordM = 2
    val unit = 3

    private val matrix = FloatArray(16)
    private val poss = intArrayOf(pos, coord)

    private var textureId = -1
    private lateinit var surfaceTexture: SurfaceTexture
    private val vertexs = GlUtils.getDirectFloatBuffer(floats)

    override fun onSurfaceCreated(context: Context, texture : SurfaceTexture, id : Int) {
        textureId = id
        surfaceTexture = texture
        programId = GlUtils.initProgramWithShaderResource(context, R.raw.camera_vetex, R.raw.camera_frag)
        attribHandler.addBuffer(vertexs)

        attribHandler.addLocationInfo(pos,
            VertexAttribHandler.AttribInfo(2, GL_FLOAT, 4 * 4)
        )
        attribHandler.addLocationInfo(coord,
            VertexAttribHandler.AttribInfo(2, GL_FLOAT, 4 * 4)
        )
    }

    override fun onSurfaceChanged(width: Int, height: Int) = Unit

    override fun onDrawFrame() {
        glUseProgram(programId)

        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(matrix)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId)
        glUniform1i(unit, 0)

        glUniformMatrix4fv(coordM, 1, false, matrix, 0)

        attribHandler.enableAttribute(poss)
        attribHandler.bindAttribute(pos, 0)
        attribHandler.bindAttribute(coord, 2)

        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)

        attribHandler.disableAttribute(poss)
    }
}