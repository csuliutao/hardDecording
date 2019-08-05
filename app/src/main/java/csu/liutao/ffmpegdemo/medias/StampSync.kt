package csu.liutao.ffmpegdemo.medias

import csu.liutao.ffmpegdemo.Utils

class StampSync {
    private var baseRelativeTime = -1L

    private fun init() {
        if (baseRelativeTime == -1L) baseRelativeTime = System.currentTimeMillis()
    }

    fun waitTime(curTime: Long) {
        init()
        val left = curTime - (System.currentTimeMillis() - baseRelativeTime)
        Utils.log("StampSync", "left ="+left)
        if (left > 0) Thread.sleep(left)
    }
}