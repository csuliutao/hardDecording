package csu.liutao.ffmpegdemo.h264

import android.content.Context
import android.media.Image
import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import csu.liutao.ffmpegdemo.medias.*
import java.util.concurrent.LinkedBlockingDeque

class AvcRecord(var muxer: MuxerManger, queueSize : Int = 10)  {
    private lateinit var cameraMgr : Camera2Mgr
    private lateinit var codecMgr : CodecManager
    private lateinit var queue : LinkedBlockingDeque<MediaInfo>

    private val codecCallback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val buffer = codec.getOutputBuffer(index)
            muxer.write(buffer, info, true)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            val info = queue.take()
            buffer.put(info.bytes, info.offset, info.size)
            codec.queueInputBuffer(index, info.offset, info.size, 0, MediaCodec.BUFFER_FLAG_KEY_FRAME)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            muxer.addTrack(format, true)
            muxer.start()
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) = Unit
    }

    private val imageListener = object : Camera2Mgr.ImageListener {
        override fun handleImage(image: Image) {
            val byte = VideoMgr.instance.imageToNV21(image)
            queue.offer(MediaInfo(byte, 0, byte.size))
        }
    }

    init {
        queue = LinkedBlockingDeque(queueSize)
    }

    fun prepare(context : Context, surface: Surface, width : Int, height: Int) {
        cameraMgr = Camera2Mgr.Builder()
            .surface(surface, false)
            .imageReader(width, height, imageListener)
            .build()
        cameraMgr.openCamera(context)
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        codecMgr = CodecManager(format, codecCallback)
    }


    fun stop() {
        cameraMgr.stop(true)
        queue.clear()
        codecMgr.stop()
    }

    fun release() {
        cameraMgr.release()
        codecMgr.release()
    }

    fun start() {
        cameraMgr.take()
        codecMgr.start()
    }
}