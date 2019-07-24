package csu.liutao.ffmpegdemo.ativities

import csu.liutao.ffmpegdemo.aac.AudioRecordManager
import csu.liutao.ffmpegdemo.aac.AudioTrackManager

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