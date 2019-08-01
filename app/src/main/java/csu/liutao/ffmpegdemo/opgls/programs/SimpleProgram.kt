package csu.liutao.ffmpegdemo.opgls.programs

import android.content.Context
import android.opengl.GLES30
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.VertexAttribHandler
import csu.liutao.ffmpegdemo.opgls.abs.IProgram
import java.nio.FloatBuffer

class SimpleProgram: IProgram {
    val V_POSION = 0
    val V_COLOE = 1
    val V_SIZE = 2
    val V_MATRIX = 3

    val POSITION_DIMENSION = 4
    val COLOR_DIMENSION = 3
    val STRIDE_NUM = POSITION_DIMENSION + COLOR_DIMENSION
    val TRIANGLE_POINTS_NUM = 6
    val LINE_POINTS_NUM = 2

    private var floats = FloatArray(16)

    val vertexs = floatArrayOf(
        0f, 0f, 0f, 1.5f, 1f, 1f, 1f,
        -0.75f, 0.75f, 0f, 2f, 0.7f, 0.7f, 0.7f,
        0.75f, 0.75f, 0f, 2f, 0.7f, 0.7f, 0.7f,
        0.75f, -0.75f, 0f, 1f, 0.7f, 0.7f, 0.7f,
        -0.75f, -0.75f, 0f, 1f, 0.7f, 0.7f, 0.7f,
        -0.75f, 0.75f, 0f, 2f, 0.7f, 0.7f, 0.7f,
        -0.75f, 0f, 0f, 1.5f,  0f, 0f, 1f,
        0.75f, 0f, 0f, 1.5f, 0f, 0f, 1f,
        0f, 0.3f, 0f, 1.75f, 1f, 0f, 0f,
        0f, -0.3f, 0f, 1.25f, 0f, 1f, 0f
    )

    private lateinit var vertexBuffer: FloatBuffer
    private val program = VertexAttribHandler()
    private var curProgram = -1


    init {
        vertexBuffer = GlUtils.getDirectFloatBuffer(vertexs)
    }

    override fun prepare(context: Context, vetexId: Int, fragmentId: Int) {
        curProgram = GlUtils.initProgramWithShaderResource(
            context,
            R.raw.simple_vetex,
            R.raw.simple_fragment
        )
        val pInfo = VertexAttribHandler.AttribInfo(
            POSITION_DIMENSION,
            GLES30.GL_FLOAT,
            STRIDE_NUM * 4
        )
        program.addLocationInfo(V_POSION, pInfo)
        val cInfo =
            VertexAttribHandler.AttribInfo(COLOR_DIMENSION, GLES30.GL_FLOAT, STRIDE_NUM * 4)
        program.addLocationInfo(V_COLOE, cInfo)
        program.addBuffer(vertexBuffer)

    }

    override fun draw() {
        GLES30.glUseProgram(curProgram)
        val allPoss = intArrayOf(V_POSION, V_COLOE)
        program.enableAttribute(allPoss)
        GLES30.glUniformMatrix4fv(V_MATRIX, 1, false, floats, 0)

        program.bindAttribute(V_POSION, 0)
        program.bindAttribute(V_COLOE, POSITION_DIMENSION)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN,0, TRIANGLE_POINTS_NUM)

        program.bindAttribute(V_POSION, TRIANGLE_POINTS_NUM * STRIDE_NUM)
        program.bindAttribute(V_COLOE, POSITION_DIMENSION + TRIANGLE_POINTS_NUM * STRIDE_NUM)
        GLES30.glLineWidth(5f)
        GLES30.glDrawArrays(GLES30.GL_LINES,0, LINE_POINTS_NUM)

        program.bindAttribute(V_POSION, (TRIANGLE_POINTS_NUM + LINE_POINTS_NUM) * STRIDE_NUM)
        program.bindAttribute(V_COLOE, POSITION_DIMENSION + (TRIANGLE_POINTS_NUM + LINE_POINTS_NUM) * STRIDE_NUM)
        GLES30.glVertexAttrib1f(V_SIZE, 20f)
        GLES30.glDrawArrays(GLES30.GL_POINTS,0,2)

        program.disableAttribute(allPoss)
    }

    override fun initScreenSize(width: Int, height: Int) {
        GlUtils.getBase3DMatrix(floats, width, height)
    }
}