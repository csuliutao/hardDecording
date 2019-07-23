package csu.liutao.ffmpegdemo.medias

import android.content.Context
import android.view.Surface
import csu.liutao.ffmpegdemo.aac.AacRecordRunnable
import csu.liutao.ffmpegdemo.h264.AvcRecord

class MediaRecord(val path: String) {

    private lateinit var muxerManger: MuxerManger
    private lateinit var aacRecord : AacRecordRunnable
    private lateinit var avcRecord : AvcRecord
    init {
        muxerManger = MuxerManger(path)
        aacRecord = AacRecordRunnable(muxerManger)
        avcRecord = AvcRecord(muxerManger)
    }

    fun prepare(context : Context, surface: Surface, width : Int, height: Int) = avcRecord.prepare(context, surface, width, height)

    fun startRecord() {
        Thread(aacRecord).start()
        avcRecord.start()
    }
    fun saveRecord(){
        muxerManger.stop()
        aacRecord.stop()
        avcRecord.stop()
    }
    fun release(){
        muxerManger.release()
        aacRecord.release()
        avcRecord.release()
    }
}