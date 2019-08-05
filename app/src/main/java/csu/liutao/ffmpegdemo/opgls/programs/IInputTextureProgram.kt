package csu.liutao.ffmpegdemo.opgls.programs

import android.content.Context

interface IInputTextureProgram {
    fun prepare(context: Context, vetexId : Int, fragmentId : Int)
    fun prepare(textureId : Int)
    fun draw()
    fun initScreenSize(width : Int, height : Int)
}