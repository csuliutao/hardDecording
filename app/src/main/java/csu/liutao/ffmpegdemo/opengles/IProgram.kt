package csu.liutao.ffmpegdemo.opengles

import android.content.Context

interface IProgram {
    fun prepare(context: Context, vetexId : Int, fragmentId : Int)
    fun draw()
    fun initScreenSize(width : Int, height : Int)
}