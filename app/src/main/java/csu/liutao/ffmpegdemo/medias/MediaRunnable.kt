package csu.liutao.ffmpegdemo.medias

interface MediaRunnable : Runnable {
    fun stop()
    fun release()
}