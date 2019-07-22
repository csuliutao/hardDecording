package csu.liutao.ffmpegdemo.ativities

class MediasActivity : VideosActivity(){
    init {
        recoderClass = VideoRecordActivity::class.java
        playerClass = VideoPlayActivity::class.java
    }
}