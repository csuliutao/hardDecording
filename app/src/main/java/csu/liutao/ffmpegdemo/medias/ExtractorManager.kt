package csu.liutao.ffmpegdemo.medias

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

class ExtractorManager(val path : String, val mineType :String = MediaFormat.MIMETYPE_VIDEO_AVC) {
    private val extractor = MediaExtractor()
    private lateinit var format: MediaFormat
    private var isStarted = true

    init {
        extractor.setDataSource(path)
        var count = extractor.trackCount
        while (count > 0) {
            count--
            format = extractor.getTrackFormat(count)
            val type = format.getString(MediaFormat.KEY_MIME)
            if (type.startsWith(mineType)) {
                extractor.selectTrack(count)
                break
            }
        }
    }

    fun getExtractorFormat() : MediaFormat{
        return format
    }

    fun stop () {
        isStarted = false
    }

    fun restart() {
        isStarted = true
    }

    fun release() {
        extractor.release()
    }

    /**
     * 返回-1已经播放完毕， -2仍存在数据，但是当前状态为暂停
     */
    fun read(buffer: ByteBuffer,info: Info, offset : Int = 0) : Int{
        val size = extractor.readSampleData(buffer, offset)
        if (!isStarted) return if (size < 0) -1 else -2
        info.flag = extractor.sampleFlags
        info.time = extractor.sampleTime
        if (size > 0) extractor.advance()
        return size
    }

    data class Info(var time : Long = 0,var flag : Int = 0)
}