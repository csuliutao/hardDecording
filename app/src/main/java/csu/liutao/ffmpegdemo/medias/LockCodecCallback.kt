package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import java.util.concurrent.locks.ReentrantReadWriteLock

abstract class LockCodecCallback(val lock : ReentrantReadWriteLock) : MediaCodec.Callback() {
    final override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        lock.readLock().lock()
        onOutput(codec, index, info)
        lock.readLock().unlock()
    }

    final override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        lock.readLock().lock()
        onInput(codec, index)
        lock.readLock().unlock()
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        Utils.log("onOutputFormatChanged")
    }

    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
        Utils.log("onError")
    }

    abstract fun onInput(codec: MediaCodec, index: Int)

    abstract fun onOutput(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo)


}