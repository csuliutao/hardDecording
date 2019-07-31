package csu.liutao.ffmpegdemo.opengles

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.R
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
        private val program = SimpleProgram()

        override fun onDrawFrame(gl: GL10?) {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
            program.draw()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
            program.initScreenSize(width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            program.prepare(context, R.raw.simple_vetex, R.raw.simple_fragment)
            GLES30.glClearColor(1f, 1f, 1f, 0f)
        }
    }
}