package csu.liutao.ffmpegdemo.opengles

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


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
//            glView!!.setRenderer(SimpleRender(this))
            glView!!.setRenderer(TextureRender(this))
            glView!!.preserveEGLContextOnPause = true
            setContentView(glView)
        } else {
            Toast.makeText(this, "not support gl3.0", Toast.LENGTH_SHORT)
            finish()
        }
    }
}