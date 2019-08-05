package csu.liutao.ffmpegdemo.opgls

import android.media.MediaCodec
import csu.liutao.ffmpegdemo.Utils

class ValidStartTime {
    private var time = 0L

    fun checkValid (info: MediaCodec.BufferInfo){
        Utils.log("opgl input presentationTimeUs= "+ info.presentationTimeUs)
        if (time == 0L || info.presentationTimeUs == 0L) {
            time = info.presentationTimeUs
            info.presentationTimeUs = 0L
        } else {
            info.presentationTimeUs -= time;
        }
        Utils.log("opgl info.presentationTimeUs = "+ info.presentationTimeUs)
    }
}