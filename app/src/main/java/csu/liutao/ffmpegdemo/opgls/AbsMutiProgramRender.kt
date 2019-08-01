package csu.liutao.ffmpegdemo.opgls

import android.content.Context
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES30.*

abstract class AbsMutiProgramRender(val context: Context) : GLSurfaceView.Renderer {
    private val map = ArrayList<IProgram>()

    final override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT)
        loadMap()
        for (program in map) {
            program.draw()
        }
    }

    final override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        loadMap()
        for (program in map) {
            program.initScreenSize(width, height)
        }
    }

    final override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(1f, 1f, 1f, 0f)
    }

    private fun loadMap() {
        if (map.size == 0) {
            val temp = initPrograms()
            if (temp.size != 0) map.addAll(temp)
        }
    }

    /**
     * 每个程序需要调用prepare方法后返回
     */
    open abstract fun initPrograms() : List<IProgram>
}