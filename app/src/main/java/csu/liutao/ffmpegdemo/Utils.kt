package csu.liutao.ffmpegdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import csu.liutao.ffmpegdemo.audios.AudioMgr
import csu.liutao.ffmpegdemo.audios.AudioRecordMgr
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Utils private constructor(){
    companion object{
        val TAG = "liutao_e"

        val TIME_FORMAT = "YYMMddHHmmss"

        val CAMERA_REQUESE_CODE = 5

        val AUDIO_REQUESE_CODE = 6

        val PLAY_FILE = "PLAY_FILE"

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

        fun checkCameraPermission(activity : AppCompatActivity): Boolean {
            if (activity.packageManager.checkPermission(Manifest.permission.CAMERA, activity.packageName) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                activity.requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUESE_CODE)
                return false
            }
            return true
        }

        fun checkeAudioPermission(activity : AppCompatActivity): Boolean {
            if (activity.packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, activity.packageName) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                activity.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_REQUESE_CODE)
                return false
            }
            return true
        }


    }
}