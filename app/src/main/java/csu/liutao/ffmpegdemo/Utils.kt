package csu.liutao.ffmpegdemo

import android.util.Log
import csu.liutao.ffmpegdemo.audios.AudioMgr
import csu.liutao.ffmpegdemo.audios.AudioRecordMgr
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Utils private constructor(){
    companion object{
        val TAG = "liutao_e"

        val TIME_FORMAT = "YYMMddHHmmss"

        fun log(str : String) : Unit {
            Log.e(TAG, str)
        }

        fun getNewFile(dir:String, endTag:String, format:String = TIME_FORMAT) : File {
            val formater = SimpleDateFormat(format)
            val date = Calendar.getInstance()
            val name = formater.format(date.time)+ endTag
            Log.e("liutao-e", name)
            val file = File(dir, name)
            file.createNewFile()
            return file
        }



    }
}