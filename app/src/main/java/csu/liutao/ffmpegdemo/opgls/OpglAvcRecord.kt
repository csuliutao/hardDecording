package csu.liutao.ffmpegdemo.opgls

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.*
import csu.liutao.ffmpegdemo.opgls.renders.CameraRender
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.ReentrantReadWriteLock

class OpglAvcRecord(val muxer : MuxerManger , queueSize : Int = 15) {
    private var cameraRender: CameraRender? = null
    private var codecMgr : CodecManager? = null
    private lateinit var queue : LinkedBlockingDeque<MediaInfo>
    private val lock = ReentrantReadWriteLock()

    @Volatile
    private var flag = MediaCodec.BUFFER_FLAG_CODEC_CONFIG

    private val codecCallback = object : LockCodecCallback(lock) {

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
            muxer.write(buffer, info, true)
            codec.releaseOutputBuffer(index, false)

        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            muxer.addTrack(format, true)
            muxer.start()
        }
    }

    private val frameListener = object : CameraRender.OnSaveFrameListener {
        override fun onSave(bytes: ByteArray) {
            Utils.log("start codec")
            queue.offer(MediaInfo(bytes, 0, bytes.size))
        }
    }

    init {
        queue = LinkedBlockingDeque(queueSize)
    }

    fun prepare(context : Context) : CameraRender{
        cameraRender = CameraRender(context, false)
        cameraRender!!.setSizeChangeListener(object : CameraRender.OnSizeChangeListener {
            override fun onSizeChanged(width: Int, height: Int) {
                CodecManager.start()
                val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, height, width)
                format.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5)
                format.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                codecMgr = CodecManager(format, codecCallback)
                codecMgr!!.start()
            }
        })
        return cameraRender!!
    }


    fun stop() {
        cameraRender?.stop()
        lock.writeLock().lock()
        codecMgr?.stop()
        lock.writeLock().unlock()
    }

    fun release() {
        lock.writeLock().lock()
        cameraRender?.release()
        codecMgr?.release()
        lock.writeLock().lock()
    }

    fun start() {
        cameraRender?.startRecord(frameListener)
    }
}