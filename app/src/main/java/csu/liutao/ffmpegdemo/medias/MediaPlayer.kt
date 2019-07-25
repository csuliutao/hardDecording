package csu.liutao.ffmpegdemo.medias

import android.graphics.SurfaceTexture
import csu.liutao.ffmpegdemo.aac.AacPlayer
import csu.liutao.ffmpegdemo.h264.AvcPlayer

class MediaPlayer(val path : String){
    private lateinit var aacPlayer: AacPlayer
    private lateinit var avcPlayer: AvcPlayer

    init {
        aacPlayer = AacPlayer(path)
        avcPlayer = AvcPlayer(path)
    }

    fun prapare(surface: SurfaceTexture) = avcPlayer.prepare(surface)

    fun play(){
        aacPlayer.start()
        avcPlayer.start()
    }

    fun stop(){
        aacPlayer.stop()
        avcPlayer.stop()
    }

    fun release(){
        aacPlayer.release()
        avcPlayer.release()
    }
}