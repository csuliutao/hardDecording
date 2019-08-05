package csu.liutao.ffmpegdemo.h264

import android.content.Context
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import csu.liutao.ffmpegdemo.medias.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.ReentrantReadWriteLock

class AvcRecord(var muxer: MuxerManger, queueSize : Int = 10)  {
    private var cameraMgr : Camera2Mgr? = null
    private var codecMgr : CodecManager? = null
    private lateinit var queue : LinkedBlockingDeque<MediaInfo>
    private val lock = ReentrantReadWriteLock()

    @Volatile
    private var flag = MediaCodec.BUFFER_FLAG_CODEC_CONFIG

    private val codecCallback = object : LockCodecCallback(lock) {
        private val time = ValidStartTime()

        override fun onInput(codec: MediaCodec, index: Int) {
            if (codecMgr == null || !codecMgr!!.isCodec()) {
                queue.clear()
                return
            }
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            val info = queue.take()
            buffer.put(info.bytes, info.offset, info.size)
            muxer.setStartTime()
            codec.queueInputBuffer(index, info.offset, info.size, System.nanoTime() / 1000 - muxer.getStartTime(), flag)
            if (flag == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) flag = MediaCodec.BUFFER_FLAG_KEY_FRAME

        }

        override fun onOutput(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (codecMgr == null || !codecMgr!!.isCodec()) return
            val buffer = codec.getOutputBuffer(index)
            time.checkValid(info)
            muxer.write(buffer, info, true)
            codec.releaseOutputBuffer(index, false)

        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            muxer.addTrack(format, true)
            muxer.start()
        }
    }

    private val imageListener = object : Camera2Mgr.ImageListener {
        override fun handleImage(image: Image) {
            if (codecMgr == null) {
                val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, image.height, image.width)
                format.setInteger(MediaFormat.KEY_BIT_RATE, image.width * image.height * 5)
                format.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
//                format.setInteger(MediaFormat.KEY_ROTATION, 90) // 只有在输出到surface时才有效
                codecMgr = CodecManager(format, codecCallback)
                codecMgr!!.start()
            }
            val srcByte = VideoMgr.instance.imageToNV2190(image)
            queue.offer(MediaInfo(srcByte, 0, srcByte.size))
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
        cameraMgr?.openCamera(context)
    }


    fun stop() {
        lock.writeLock().lock()
        cameraMgr?.stop(true)
        codecMgr?.stop()
        lock.writeLock().unlock()
    }

    fun release() {
        lock.writeLock().lock()
        cameraMgr?.release()
        codecMgr?.release()
        lock.writeLock().lock()
    }

    fun start() {
        cameraMgr?.take()
    }
}