package csu.liutao.ffmpegdemo.audios

interface CodecOutputListener {
    fun output(bytes : ByteArray)
}