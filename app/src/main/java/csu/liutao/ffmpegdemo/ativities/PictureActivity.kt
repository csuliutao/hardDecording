package csu.liutao.ffmpegdemo.ativities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.PictureMgr
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.Camera2Mgr
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PictureActivity :AppCompatActivity() {
    private lateinit var surfaceView :SurfaceView
    private lateinit var capture : Button

    private lateinit var cameraMgr : Camera2Mgr

    private val listener = object : Camera2Mgr.ImageListener{
        override fun handleImage(image: Image) {
            savePicAndShow(image)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        surfaceView = findViewById(R.id.picture)
        capture = findViewById(R.id.capture)

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraMgr.release()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (holder == null) finish()
                cameraMgr = Camera2Mgr.Builder()
                    .surface(holder!!.surface, true)
                    .imageReader(surfaceView.width, surfaceView.height, listener)
                    .build()
                if (Utils.checkCameraPermission(this@PictureActivity)) cameraMgr.openCamera(this@PictureActivity)
            }
        })

        capture.setOnClickListener{
            takePicture()
        }
    }

    private fun takePicture() {
        cameraMgr.take()
    }

    private fun savePicAndShow(image : Image) {
        cameraMgr.stop()
        val byteBuffer = image.planes[0].buffer
        PictureMgr.instance.initDir(this)
        val file = PictureMgr.instance.getFile()

        val fileWrite = FileOutputStream(file)
        val channel = fileWrite.channel
        if (byteBuffer.hasRemaining()) {
            Utils.log("bytebuffer times")
            channel.position(0)
            channel.write(byteBuffer)
            fileWrite.flush()
        }

        channel.close()
        fileWrite.close()
        Utils.log("imageReader img available")
        finish()
        startActivity(Intent(this, SurfaceImgActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Utils.CAMERA_REQUESE_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            } else {
                cameraMgr.openCamera(this)
            }
        }
    }
}