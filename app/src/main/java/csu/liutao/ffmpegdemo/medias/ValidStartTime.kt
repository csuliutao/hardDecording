package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec

class ValidStartTime {
    private var time = 0L

    fun checkValid (info: MediaCodec.BufferInfo){
        if (time == 0L || info.presentationTimeUs == 0L) {
            time = info.presentationTimeUs
            info.presentationTimeUs = 0L
        } else {
            info.presentationTimeUs -= time
        }
    }
}