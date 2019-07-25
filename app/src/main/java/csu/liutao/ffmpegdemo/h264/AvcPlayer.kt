package csu.liutao.ffmpegdemo.h264

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.medias.ExtractorManager
import csu.liutao.ffmpegdemo.medias.LockCodecCallback
import csu.liutao.ffmpegdemo.medias.MediaInfo
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.locks.ReentrantReadWriteLock

class AvcPlayer (val path : String, queueSize : Int = 10){
    private val tag = "AvcPlayer"
    private lateinit var extractorMgr: ExtractorManager
    private var codecMgr : CodecManager? = null
    private lateinit var queue : LinkedBlockingDeque<MediaInfo>
    private val lock = ReentrantReadWriteLock()

    private val callback = object : LockCodecCallback(lock){

        override fun onInput(codec: MediaCodec, index: Int) {
            if (codecMgr == null || !codecMgr!!.isCodec()) {
                queue.clear()
                return
            }
            val buffer = codec.getInputBuffer(index)
            buffer.clear()
            val info = ExtractorManager.Info()
            val length = extractorMgr.read(buffer, info)
            Utils.log(tag, "input length="+ length)
            if (length > 0) codec.queueInputBuffer(index, 0, length, info.time, info.flag)

        }

        override fun onOutput(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (codecMgr != null && codecMgr!!.isCodec()) codec.releaseOutputBuffer(index, true)
        }
    }

    init {
        queue = LinkedBlockingDeque(queueSize)
        extractorMgr = ExtractorManager(path, MediaFormat.MIMETYPE_VIDEO_AVC)
    }

    fun prepare(surface: SurfaceTexture) {
        val format = extractorMgr.getExtractorFormat()
        val width = format.getInteger(MediaFormat.KEY_WIDTH)
        val height = format.getInteger(MediaFormat.KEY_HEIGHT)
        surface.setDefaultBufferSize(width, height)
        codecMgr = CodecManager(format,callback ,Surface(surface), 0)
    }

    fun start() {
        codecMgr!!.start()
    }

    fun stop() {
        lock.writeLock().lock()
        codecMgr?.stop()
        lock.writeLock().unlock()
    }

    fun release() {
        lock.writeLock().lock()
        codecMgr?.release()
        lock.writeLock().unlock()
    }
}