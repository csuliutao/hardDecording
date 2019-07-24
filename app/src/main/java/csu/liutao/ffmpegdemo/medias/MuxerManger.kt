package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.Looper
import csu.liutao.ffmpegdemo.Utils
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque

class MuxerManger (val path:String, val format : Int = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4){
    private var muxer : MediaMuxer? = null
    private var videoTrack = -1
    private var audioTrack = -1
    private var queue = LinkedBlockingDeque<Info>(10)

    @Volatile
    private var isStarted = false

    private var runnable = object : Runnable {
        override fun run() {
            val bufferInfo = MediaCodec.BufferInfo()
            var trackId = videoTrack
            while (isStarted || muxer == null) {
                val infos = queue.poll()
                if (null == infos) {
                    Thread.sleep(50)
                    continue
                }
                bufferInfo.set(0, infos.bytes.size, infos.time, infos.flag)
                trackId = if(infos.isVedio) videoTrack else audioTrack
                muxer!!.writeSampleData(trackId, ByteBuffer.wrap(infos.bytes), bufferInfo)
            }
        }
    }

    init {
        muxer = MediaMuxer(path, format)
    }

    fun start() {
        if (isReadyStart()) {
            isStarted = true
            muxer?.start()
            Thread(runnable).start()
        }
    }

    fun release() {
        queue.clear()
        muxer?.release()
        muxer = null
    }

    fun stop() {
        isStarted = false
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
        if (!isStarted) return
        Utils.log("muxer", "isVedio = "+ isVedio+ ",time =" + info.presentationTimeUs)
        val bytes = ByteArray(info.size)
        buffer.get(bytes, info.offset, info.size)
        val infos = Info(bytes, info.presentationTimeUs, info.flags, isVedio)
        queue.offer(infos)
    }

    data class Info(val bytes: ByteArray ,val time : Long, val flag: Int,val isVedio: Boolean)
}