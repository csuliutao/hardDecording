package csu.liutao.ffmpegdemo.opengles

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES30
import csu.liutao.ffmpegdemo.Utils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GlUtils private constructor(){

    companion object {

        fun isSupportGLVersion(context: Context, version : Int = 0x30000) : Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = manager.deviceConfigurationInfo
            return info.reqGlEsVersion >= version
        }

        fun getDirectFloatBuffer(byteArray: FloatArray, offset : Int = 0, length : Int = byteArray.size) : FloatBuffer {
            val floatBuffer = ByteBuffer.allocateDirect(length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            floatBuffer.put(byteArray, offset, length)
            return floatBuffer
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

        fun initProgramWithShaderResource(context: Context, vertexId : Int, fragmentId : Int) : Int {
            return initProgramWithShaderString(readShaderStringFromRaw(context, vertexId),
                readShaderStringFromRaw(context, fragmentId))
        }


        fun initProgramWithShaderString(vertex : String, fragment :String ) : Int{
            val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
            GLES30.glShaderSource(vertexShader, vertex)
            GLES30.glCompileShader(vertexShader)
            checkCompileShaderInfo(vertexShader)

            val fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
            GLES30.glShaderSource(fragmentShader, fragment)
            GLES30.glCompileShader(fragmentShader)
            checkCompileShaderInfo(fragmentShader)

            val program = GLES30.glCreateProgram()
            GLES30.glAttachShader(program, vertexShader)
            GLES30.glAttachShader(program, fragmentShader)
            GLES30.glLinkProgram(program)
            checkProgramLinkInfo(program)
            return program
        }

        fun checkCompileShaderInfo (shader : Int) {
            var status = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_SHADER_COMPILER, status, 0)
            Utils.log("shader "+ shader +", result ="+ status[0] +", compile info ="+ GLES30.glGetShaderInfoLog(shader))
        }

        fun checkProgramLinkInfo(pg : Int) {
            var status = IntArray(1)
            GLES30.glGetProgramiv(pg, GLES30.GL_LINK_STATUS, status, 0)
            Utils.log("program "+ pg  +", result ="+ status[0] + ", info =" + GLES30.glGetProgramInfoLog(pg))

            GLES30.glValidateProgram(pg)
            GLES30.glGetProgramiv(pg, GLES30.GL_VALIDATE_STATUS, status, 0)
            Utils.log("program "+ pg  +", result ="+ status[0] + ", info =" + GLES30.glGetProgramInfoLog(pg))
        }


    }
}