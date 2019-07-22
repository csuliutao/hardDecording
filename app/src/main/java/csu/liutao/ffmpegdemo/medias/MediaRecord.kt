package csu.liutao.ffmpegdemo.medias

import csu.liutao.ffmpegdemo.aac.AacRecordRunnable
import csu.liutao.ffmpegdemo.h264.AvcRecordRunnable

class MediaRecord(val path: String) {

    private lateinit var muxerManger: MuxerManger
    private lateinit var aacRecord : AacRecordRunnable
    private lateinit var avcRecord : AvcRecordRunnable
    init {
        aacRecord = AacRecordRunnable(muxerManger)
        avcRecord = AvcRecordRunnable(muxerManger)
    }

    fun startRecord() {
        Thread(aacRecord).start()
        Thread(avcRecord).start()
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