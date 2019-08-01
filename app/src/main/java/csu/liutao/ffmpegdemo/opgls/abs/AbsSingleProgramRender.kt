package csu.liutao.ffmpegdemo.opgls.abs

import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class AbsSingleProgramRender() : GLSurfaceView.Renderer {
    private var program : IProgram? = null

    final override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        loadProgram()
        program!!.draw()
    }

    final override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
       glViewport(0, 0, width, height)
       loadProgram()
       program!!.initScreenSize(width, height)
    }

    final override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1f, 1f, 1f, 0f)
    }

    private fun loadProgram() {
        if (program == null) {
            program = initProgram()
        }
    }

    /**
     * 程序需要调用prepare方法后返回
     */
    open abstract fun initProgram() : IProgram
}