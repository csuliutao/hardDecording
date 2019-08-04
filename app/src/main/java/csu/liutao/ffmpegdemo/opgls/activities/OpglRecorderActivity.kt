package csu.liutao.ffmpegdemo.opgls.activities

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.opgls.GlUtils
import csu.liutao.ffmpegdemo.opgls.OpglFileManger
import csu.liutao.ffmpegdemo.opgls.OpglMediaRecord

class OpglRecorderActivity : AppCompatActivity() {
    private var glView : GLSurfaceView? = null
    private var record : OpglMediaRecord? = null
    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!GlUtils.isSupportGLVersion(this))  finish()
        if (Utils.checkMediaPermission(this)) initRecord()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Utils.MEDIA_REQUESE_CODE) {
            var size = grantResults.size
            while (size > 0) {
                size--
                if (grantResults[size] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                    return
                }
            }
            initRecord()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        if (!isStarted) {
            isStarted = true
            record?.startRecord()
        } else {
            record?.saveRecord()
            finish()
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        record?.saveRecord()
    }

    fun initRecord() {
        glView = GLSurfaceView(this)
        glView?.setEGLContextClientVersion(3)

        record = OpglMediaRecord(OpglFileManger.instance.getFile(false).canonicalPath)
        record!!.prepare(this)
        val render = record!!.prepare(this)
        render.listener = SurfaceTexture.OnFrameAvailableListener {
            render!!.handler.post {
                glView!!.requestRender()
            }
        }
        glView!!.setRenderer(render)
        glView?.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        setContentView(glView)
    }

    override fun onResume() {
        super.onResume()
        glView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        record?.release()
    }
}