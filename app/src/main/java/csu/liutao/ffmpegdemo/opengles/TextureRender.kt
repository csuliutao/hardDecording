package csu.liutao.ffmpegdemo.opengles

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import csu.liutao.ffmpegdemo.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TextureRender(val context : Context) : GLSurfaceView.Renderer {
    private val table = TableProgram()
    private val mallet = MalletProgram()

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        table.draw()
        mallet.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        table.initScreenSize(width, height)
        mallet.initScreenSize( width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        table.prepare(context, R.raw.table_vertex, R.raw.table_frag)
        mallet.prepare(context, R.raw.mallet_vertex, R.raw.mallet_frag)
        GLES30.glClearColor(1f,1f,1f,0f)
    }
}