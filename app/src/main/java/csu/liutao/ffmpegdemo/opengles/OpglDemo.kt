package csu.liutao.ffmpegdemo.opengles

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
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

        val vertexs = floatArrayOf(
            0.5f, 0.5f,
            0.5f, -0.5f,
            -0.5f,-0.5f,
            -0.5f, 0.5f)
        private lateinit var vertexBuffer: FloatBuffer
        private lateinit var vertexString : String
        private lateinit var fragmentString :String
        private var vertexShader = 0
        private var fragmentShader = 0
        private var program = 0


        init {
            vertexBuffer = ByteBuffer.allocateDirect(vertexs.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            vertexBuffer.position(0)
            vertexBuffer.put(vertexs)
        }

        private fun checkCompileShaderInfo (shader : Int) {
            var status = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_SHADER_COMPILER, status, 0)
            Utils.log("shader "+ shader +", result ="+ status[0] +", compile info ="+ GLES30.glGetShaderInfoLog(shader))
        }

        private fun checkProgramLinkInfo(pg : Int) {
            var status = IntArray(1)
            GLES30.glGetProgramiv(pg, GLES30.GL_LINK_STATUS, status, 0)
            Utils.log("program "+ pg  +", result ="+ status[0] + ", info =" + GLES30.glGetProgramInfoLog(pg))

            GLES30.glValidateProgram(pg)
            GLES30.glGetProgramiv(pg, GLES30.GL_VALIDATE_STATUS, status, 0)
            Utils.log("program "+ pg  +", result ="+ status[0] + ", info =" + GLES30.glGetProgramInfoLog(pg))
        }


        override fun onDrawFrame(gl: GL10?) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
            GLES30.glUseProgram(program)
            GLES30.glVertexAttrib4f(V_COLOE, 0f, 1f, 0f, 0f)
            vertexBuffer.position(0)
            GLES30.glVertexAttribPointer(V_POSION, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
            GLES30.glEnableVertexAttribArray(V_POSION)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4)
            GLES30.glDisableVertexAttribArray(V_POSION)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            vertexString = GlUtils.readShaderStringFromRaw(context, R.raw.simple_vetex)
            vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
            GLES30.glShaderSource(vertexShader, vertexString)
            GLES30.glCompileShader(vertexShader)
            checkCompileShaderInfo(vertexShader)

            fragmentString = GlUtils.readShaderStringFromRaw(context, R.raw.simple_fragment)
            fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
            GLES30.glShaderSource(fragmentShader, fragmentString)
            GLES30.glCompileShader(fragmentShader)
            checkCompileShaderInfo(fragmentShader)

            program = GLES30.glCreateProgram()
            GLES30.glAttachShader(program, vertexShader)
            GLES30.glAttachShader(program, fragmentShader)
            GLES30.glLinkProgram(program)
            checkProgramLinkInfo(program)

            GLES30.glClearColor(1f, 1f, 1f, 0f)
        }
    }
}