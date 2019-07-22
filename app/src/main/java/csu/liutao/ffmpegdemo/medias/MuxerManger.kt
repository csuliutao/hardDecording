package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import csu.liutao.ffmpegdemo.Utils
import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock

class MuxerManger (val path:String, val format : Int = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4){
    private var muxer : MediaMuxer? = null
    private var videoTrack = -1
    private var audioTrack = -1

    init {
        muxer = MediaMuxer(path, format)
    }

    fun start() {
        if (isReadyStart()) {
            muxer?.start()
            startCond.signalAll()
        }
    }

    fun release() {
        muxer?.release()
        muxer = null
    }

    fun stop() {
        muxer?.stop()
    }

    private fun isReadyStart() :Boolean {
        return (videoTrack != -1) and (audioTrack != -1)
    }

    fun addTrack(mediaFormat: MediaFormat, isVedio: Boolean = true) {
        Utils.log("add track")
        val trackId = muxer?.addTrack(mediaFormat) ?: -1
        if (isVedio) videoTrack = trackId else audioTrack = trackId
    }

    fun write(buffer: ByteBuffer, info : MediaCodec.BufferInfo, isVedio: Boolean = true) {
        lock.lock()
        if (!isReadyStart()) startCond.await()
        muxer?.writeSampleData(if(isVedio) videoTrack else audioTrack, buffer, info)
        lock.unlock()
    }

    companion object {
        val lock = ReentrantLock()
        val startCond = lock.newCondition()
    }
}