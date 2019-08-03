package csu.liutao.ffmpegdemo.opgls.activities

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.activities.OpglBaseActivity
import csu.liutao.ffmpegdemo.opgls.renders.CameraRender

class OpglPictureActivity : AppCompatActivity() {
    private var glView : GLSurfaceView? = null
    private var render : CameraRender? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Utils.checkCameraPermission(this)) initContentView()
    }

    override fun onResume() {
        super.onResume()
        glView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView?.onPause()
    }

    private fun initContentView() {
        if (GlUtils.isSupportGLVersion(this)) {
            glView = GLSurfaceView(this)
            render = CameraRender(this)

            glView!!.setEGLContextClientVersion(3)
            glView!!.setRenderer(render)
            render!!.listener = SurfaceTexture.OnFrameAvailableListener {
                Utils.log("onFrameAvailable")
                render!!.handler.post {
                    glView!!.requestRender()
                }
            }
            glView!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            setContentView(glView)
        } else {
            Toast.makeText(this, "not support gles 3.0", Toast.LENGTH_LONG)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Utils.CAMERA_REQUESE_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initContentView() else finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        render?.release()
    }
}