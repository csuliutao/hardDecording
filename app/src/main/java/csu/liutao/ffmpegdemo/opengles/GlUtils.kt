package csu.liutao.ffmpegdemo.opengles

import android.app.ActivityManager
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

class GlUtils private constructor(){

    companion object {

        fun isSupportGLVersion(context: Context, version : Int = 0x30000) : Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = manager.deviceConfigurationInfo
            return info.reqGlEsVersion >= version
        }

        fun readShaderStringFromRaw(context: Context, id : Int) :String {
            val input = context.resources.openRawResource(id)
            val builder = StringBuilder()
            val bufferReader = BufferedReader(InputStreamReader(input))
            var str = bufferReader.readLine()
            while (str != null) {
                builder.append(str).append('\n')
                str = bufferReader.readLine()
            }
            bufferReader.close()
            return builder.toString()
        }
    }
}