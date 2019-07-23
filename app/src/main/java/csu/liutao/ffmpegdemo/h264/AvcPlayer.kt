package csu.liutao.ffmpegdemo.h264

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.medias.ExtractorManager
import csu.liutao.ffmpegdemo.medias.MediaInfo
import java.util.concurrent.LinkedBlockingDeque

class AvcPlayer (val path : String, queueSize : Int = 10){
    private lateinit var extractorMgr: ExtractorManager
    private lateinit var codecMgr : CodecManager
    private lateinit var queue : LinkedBlockingDeque<MediaInfo>

    private val callback = object : MediaCodec.Callback(){
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            codec.releaseOutputBuffer(index, true)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            val length = extractorMgr.read(buffer, 0)
            codec.queueInputBuffer(index, 0, length, 0, MediaCodec.BUFFER_FLAG_KEY_FRAME)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) = Unit

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) = Unit
    }

    init {
        queue = LinkedBlockingDeque(queueSize)
        extractorMgr = ExtractorManager(path, MediaFormat.MIMETYPE_VIDEO_AVC)
    }

    fun prepare(surface: Surface) {
        codecMgr = CodecManager(extractorMgr.getExtractorFormat(),callback ,surface, 0)
    }

    fun start() {
        codecMgr.start()
    }

    fun stop() {
        codecMgr.stop()
    }

    fun release() {
        codecMgr.release()
    }
}