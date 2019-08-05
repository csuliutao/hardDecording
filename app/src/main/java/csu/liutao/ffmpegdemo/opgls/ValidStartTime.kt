package csu.liutao.ffmpegdemo.opgls

import android.media.MediaCodec
import csu.liutao.ffmpegdemo.Utils

class ValidStartTime {
    private var time = -1L

    fun checkValid (info: MediaCodec.BufferInfo){
        if (time == -1L) {
            time = info.presentationTimeUs
            info.presentationTimeUs = 0L
        } else {
            info.presentationTimeUs -= time;
        }
        Utils.log("")
    }
}