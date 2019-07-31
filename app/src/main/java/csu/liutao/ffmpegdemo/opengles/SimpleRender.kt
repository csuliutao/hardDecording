package csu.liutao.ffmpegdemo.opengles

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import csu.liutao.ffmpegdemo.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRender(val context : Context) : GLSurfaceView.Renderer {
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
