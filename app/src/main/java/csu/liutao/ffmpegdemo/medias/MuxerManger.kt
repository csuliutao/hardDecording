package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import csu.liutao.ffmpegdemo.Utils
import java.nio.ByteBuffer

class MuxerManger (val path:String, val format : Int = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4){
    private var muxer : MediaMuxer? = null
    private var videoTrack = -1
    private var audioTrack = -1

    init {
        muxer = MediaMuxer(path, format)
    }

    fun start() {
        muxer?.start()
    }

    fun release() {
        muxer?.release()
        muxer = null
    }

    fun stop() {
        muxer?.stop()
    }

    fun addTrack(mediaFormat: MediaFormat, isVedio: Boolean = true) {
        Utils.log("add track")
        val trackId = muxer?.addTrack(mediaFormat) ?: -1
        if (isVedio) videoTrack = trackId else audioTrack = trackId
    }

    fun write(buffer: ByteBuffer, info : MediaCodec.BufferInfo, isVedio: Boolean = true) {
        synchronized(lock) {
            muxer?.writeSampleData(if(isVedio) videoTrack else audioTrack, buffer, info)
        }
    }

    companion object {
        val lock = Object()
    }
}