package csu.liutao.ffmpegdemo.audios

import android.media.MediaCodec
import java.nio.ByteBuffer

interface CodecOutputListener {
    fun output(byteBuf : ByteBuffer,bufferInfo : MediaCodec.BufferInfo)
    fun onfinish()
}