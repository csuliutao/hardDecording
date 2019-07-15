package csu.liutao.ffmpegdemo.audios

import java.io.File

class AudioTrackMgr private constructor(){
    private var curFile : File? = null
    var isPlaying = false

    fun play(file : File){
        curFile = file
        isPlaying = true
    }

    fun pause(){
        isPlaying = false
    }

    fun replay(){
        isPlaying = true
    }

    fun prepare() {
        isPlaying = false
    }

    fun release(){
        isPlaying = false
    }


    companion object{
        val instance = AudioTrackMgr()
    }
}