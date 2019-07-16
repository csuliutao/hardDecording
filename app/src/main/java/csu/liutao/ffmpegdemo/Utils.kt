package csu.liutao.ffmpegdemo

import android.util.Log

class Utils private constructor(){
    companion object{
        val TAG = "liutao_e"
        fun log(str : String) : Unit {
            Log.e(TAG, str)
        }



    }
}