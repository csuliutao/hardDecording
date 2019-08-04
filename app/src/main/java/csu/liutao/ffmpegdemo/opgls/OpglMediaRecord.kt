package csu.liutao.ffmpegdemo.opgls

import android.content.Context
import csu.liutao.ffmpegdemo.aac.AacRecordRunnable
import csu.liutao.ffmpegdemo.medias.MuxerManger
import csu.liutao.ffmpegdemo.opgls.renders.CameraRender

class OpglMediaRecord(val path: String) {
    private lateinit var muxerManger: MuxerManger
    private lateinit var aacRecord : AacRecordRunnable
    private lateinit var avcRecord : OpglAvcRecord

    init {
        muxerManger = MuxerManger(path)
        aacRecord = AacRecordRunnable(muxerManger)
        avcRecord = OpglAvcRecord(muxerManger)
    }

    fun prepare(context : Context) : CameraRender{
        return avcRecord.prepare(context)
    }

    fun startRecord() {
        aacRecord.start()
        Thread(aacRecord).start()
        avcRecord.start()
    }
    fun saveRecord(){
        aacRecord.stop()
        avcRecord.stop()
        muxerManger.stop()
    }
    fun release(){
        aacRecord.release()
        avcRecord.release()
        muxerManger.release()
    }
}