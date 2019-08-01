package csu.liutao.ffmpegdemo.opgls.activities

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.renders.TextureRender


open class OpglBaseActivity : AppCompatActivity() {
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

    open protected fun getRender() : GLSurfaceView.Renderer {
        return TextureRender(this)
    }


    private fun initGLView() {
        if (GlUtils.isSupportGLVersion(this)) {
            glView = GLSurfaceView(this)
            glView!!.setEGLContextClientVersion(3)
//            glView!!.setRenderer(SimpleRender(this))
            glView!!.setRenderer(getRender())
            glView!!.preserveEGLContextOnPause = true
            setContentView(glView)
        } else {
            Toast.makeText(this, "not support gl3.0", Toast.LENGTH_SHORT)
            finish()
        }
    }
}