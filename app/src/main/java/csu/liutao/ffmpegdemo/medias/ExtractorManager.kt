package csu.liutao.ffmpegdemo.medias

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

class ExtractorManager(val path : String, val mineType :String = MediaFormat.MIMETYPE_VIDEO_AVC) {
    private val extractor = MediaExtractor()
    private lateinit var format: MediaFormat

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

    fun release() {
        extractor.release()
    }

    fun read(buffer: ByteBuffer, offset : Int = 0) : Int{
        val size = extractor.readSampleData(buffer, offset)
        if (size > 0) extractor.advance()
        return size
    }
}