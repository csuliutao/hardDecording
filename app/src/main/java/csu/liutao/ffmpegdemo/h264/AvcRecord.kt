package csu.liutao.ffmpegdemo.h264

import android.content.Context
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import csu.liutao.ffmpegdemo.medias.*
import java.util.concurrent.LinkedBlockingDeque

class AvcRecord(var muxer: MuxerManger, queueSize : Int = 10)  {
    private lateinit var cameraMgr : Camera2Mgr
    private lateinit var codecMgr : CodecManager
    private lateinit var queue : LinkedBlockingDeque<MediaInfo>

    private var flag = MediaCodec.BUFFER_FLAG_CODEC_CONFIG

    private val codecCallback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (!codecMgr.isCodec()) return
            val buffer = codec.getOutputBuffer(index)
            muxer.write(buffer, info, true)
            if (codecMgr.isCodec()) codec.releaseOutputBuffer(index, false)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            if (!codecMgr.isCodec()) {
                queue.clear()
                return
            }
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            val info = queue.take()
            buffer.put(info.bytes, info.offset, info.size)
            muxer.setStartTime()
            if (codecMgr.isCodec()) codec.queueInputBuffer(index, info.offset, info.size, System.nanoTime() / 1000 - muxer.getStartTime(), flag)
            if (flag == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) flag = MediaCodec.BUFFER_FLAG_KEY_FRAME
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
        format.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 3)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        format.setInteger(MediaFormat.KEY_ROTATION, 90)
        codecMgr = CodecManager(format, codecCallback)
    }


    fun stop() {
        cameraMgr.stop(true)
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