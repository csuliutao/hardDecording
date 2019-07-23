package csu.liutao.ffmpegdemo.ativities

import android.os.Bundle
import csu.liutao.ffmpegdemo.aac.AudioRecordManager
import csu.liutao.ffmpegdemo.aac.AudioTrackManager
import csu.liutao.ffmpegdemo.medias.CodecManager

class MediasActivity : VideosActivity(){
    init {
        recoderClass = MediaRecordActivity::class.java
        playerClass = MediaPlayerActivity::class.java
        isVideo = false
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioTrackManager.release()
        AudioRecordManager.release()

    }
}