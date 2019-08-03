package csu.liutao.ffmpegdemo.opgls.camera

import android.content.Context
import android.graphics.SurfaceTexture

interface ICameraProgram {
    fun onSurfaceCreated(context: Context, texture : SurfaceTexture, id : Int)
    fun onSurfaceChanged(width: Int, height: Int)
    fun onDrawFrame()
}