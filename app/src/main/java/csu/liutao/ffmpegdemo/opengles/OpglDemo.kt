package csu.liutao.ffmpegdemo.opengles

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OpglDemo : AppCompatActivity() {
    private var glView : GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGLView()
    }

    override fun onResume() {
        super.onResume()
        glView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView?.onPause()
    }


    private fun initGLView() {
        if (GlUtils.isSupportGLVersion(this)) {
            glView = GLSurfaceView(this)
            glView!!.setEGLContextClientVersion(3)
            glView!!.setRenderer(FirstRender(this))
            glView!!.preserveEGLContextOnPause = true
            setContentView(glView)
        } else {
            Toast.makeText(this, "not support gl3.0", Toast.LENGTH_SHORT)
            finish()
        }
    }

    class FirstRender(val context : Context) : GLSurfaceView.Renderer {
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
        private var program = 0


        init {
            vertexBuffer = GlUtils.getDirectFloatBuffer(vertexs)
        }


        override fun onDrawFrame(gl: GL10?) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
            GLES30.glUseProgram(program)

            GLES30.glUniformMatrix4fv(V_MATRIX, 1, false, floats, 0)

            GLES30.glEnableVertexAttribArray(V_POSION)
            GLES30.glEnableVertexAttribArray(V_COLOE)
            var startPos = 0

            vertexBuffer.position(startPos)
            GLES30.glVertexAttribPointer(V_POSION, POSITION_DIMENSION, GLES30.GL_FLOAT, false, STRIDE_NUM * 4, vertexBuffer)
            vertexBuffer.position(POSITION_DIMENSION + startPos)
            GLES30.glVertexAttribPointer(V_COLOE, COLOR_DIMENSION, GLES30.GL_FLOAT, false, STRIDE_NUM * 4, vertexBuffer)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, TRIANGLE_POINTS_NUM)

            startPos = TRIANGLE_POINTS_NUM * STRIDE_NUM
            vertexBuffer.position(startPos)
            GLES30.glVertexAttribPointer(V_POSION, POSITION_DIMENSION, GLES30.GL_FLOAT, false, STRIDE_NUM * 4, vertexBuffer)
            vertexBuffer.position(POSITION_DIMENSION + startPos)
            GLES30.glVertexAttribPointer(V_COLOE, COLOR_DIMENSION, GLES30.GL_FLOAT, false, STRIDE_NUM * 4, vertexBuffer)
            GLES30.glLineWidth(5f)
            GLES30.glDrawArrays(GLES30.GL_LINES, 0, LINE_POINTS_NUM)

            startPos = (TRIANGLE_POINTS_NUM + LINE_POINTS_NUM) * STRIDE_NUM
            vertexBuffer.position(startPos)
            GLES30.glVertexAttribPointer(V_POSION, POSITION_DIMENSION, GLES30.GL_FLOAT, false, STRIDE_NUM * 4, vertexBuffer)
            vertexBuffer.position(POSITION_DIMENSION + startPos)
            GLES30.glVertexAttribPointer(V_COLOE, COLOR_DIMENSION, GLES30.GL_FLOAT, false, STRIDE_NUM * 4, vertexBuffer)
            GLES30.glVertexAttrib1f(V_SIZE, 20f)
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 2)

            GLES30.glDisableVertexAttribArray(V_COLOE)
            GLES30.glDisableVertexAttribArray(V_POSION)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
            val omFloats = FloatArray(16)
            val ratio = if (height < width) width.toFloat() / height else height.toFloat() / width
            if (height > width) {
                Matrix.orthoM(omFloats, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
            } else {
                Matrix.orthoM(omFloats, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
            }
            val perFloats = FloatArray(16)
            Matrix.perspectiveM(perFloats, 0, 45f, width.toFloat() / height, 1f, 10f)
//            Matrix.setIdentityM(perFloats, 0)
            Matrix.translateM(perFloats, 0, 0f, 0f, -3f)
//            Matrix.rotateM(perFloats, 0, 60f, 1f, 0f, 0f)
            Matrix.multiplyMM(floats, 0, perFloats, 0, omFloats, 0)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            program = GlUtils.initProgramWithShaderResource(context, R.raw.simple_vetex, R.raw.simple_fragment)
            GLES30.glClearColor(1f, 1f, 1f, 0f)
        }
    }
}