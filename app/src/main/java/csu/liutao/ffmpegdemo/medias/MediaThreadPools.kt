package csu.liutao.ffmpegdemo.medias

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MediaThreadPools private constructor(){
    private var threadPools : ThreadPoolExecutor? = null
    private var coreSize = 4
    private var maxSize  = 8
    private var aliveTime = 10L

    private lateinit var queue : LinkedBlockingDeque<Runnable>

    init {
        coreSize = Runtime.getRuntime().availableProcessors()
        maxSize = coreSize * 2 + 1
        queue = LinkedBlockingDeque(maxSize)
        threadPools = ThreadPoolExecutor(coreSize, maxSize, aliveTime, TimeUnit.SECONDS, queue)
    }

    fun submit(runnable: Runnable) {
        threadPools?.submit(runnable)
    }

    fun shutDown() {
        threadPools?.shutdown()
        threadPools = null
    }


    companion object {
        val instance = MediaThreadPools()
    }
}