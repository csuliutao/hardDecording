package csu.liutao.ffmpegdemo.ativities

class MediasActivity : VideosActivity(){
    init {
        recoderClass = MediaRecordActivity::class.java
        playerClass = MediaPlayerActivity::class.java
        isVideo = false
    }
}