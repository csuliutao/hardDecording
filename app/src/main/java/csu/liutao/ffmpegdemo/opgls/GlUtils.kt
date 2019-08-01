package csu.liutao.ffmpegdemo.opgls

import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30.*
import android.opengl.GLUtils
import android.opengl.Matrix
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
            val vertexShader = glCreateShader(GL_VERTEX_SHADER)
            glShaderSource(vertexShader, vertex)
            glCompileShader(vertexShader)
            checkCompileShaderInfo(vertexShader)

            val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
            glShaderSource(fragmentShader, fragment)
            glCompileShader(fragmentShader)
            checkCompileShaderInfo(fragmentShader)

            val program = glCreateProgram()
            glAttachShader(program, vertexShader)
            glAttachShader(program, fragmentShader)
            glLinkProgram(program)
            checkProgramLinkInfo(program)
            return program
        }

        fun checkCompileShaderInfo (shader : Int) {
            var status = IntArray(1)
            glGetShaderiv(shader, GL_SHADER_COMPILER, status, 0)
            Utils.log("shader "+ shader +", result ="+ status[0] +", compile info ="+ glGetShaderInfoLog(shader))
        }

        fun checkProgramLinkInfo(pg : Int) {
            var status = IntArray(1)
            glGetProgramiv(pg, GL_LINK_STATUS, status, 0)
            Utils.log("program "+ pg  +", result ="+ status[0] + ", info =" + glGetProgramInfoLog(pg))

            glValidateProgram(pg)
            glGetProgramiv(pg, GL_VALIDATE_STATUS, status, 0)
            Utils.log("program "+ pg  +", result ="+ status[0] + ", info =" + glGetProgramInfoLog(pg))
        }

        fun getBase3DMatrix(result : FloatArray, width : Int, height : Int, fovy : Float = 45f, near : Float = 2f, far : Float =10f, zoffset : Float = -3f) {
            val omFloats = FloatArray(16)
            getOMatrix(omFloats, width, height)
            val perFloats = FloatArray(16)
            Matrix.perspectiveM(perFloats, 0, fovy, width.toFloat() / height, 1f, 10f)
//            Matrix.setIdentityM(perFloats, 0)
            Matrix.translateM(perFloats, 0, 0f, 0f, zoffset)
//            Matrix.rotateM(perFloats, 0, 60f, 1f, 0f, 0f)
            Matrix.multiplyMM(result, 0, perFloats, 0, omFloats, 0)
        }


        fun getOMatrix(result: FloatArray, width: Int, height: Int) {
            val ratio = if (height < width) width.toFloat() / height else height.toFloat() / width
            if (height > width) {
                Matrix.orthoM(result, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
            } else {
                Matrix.orthoM(result, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
            }
        }

        fun loadTexture(contex : Context, imageId : Int) : Int {
            val ids = IntArray(1)
            ids[0] = -1

            val option = BitmapFactory.Options()
            option.inScaled = false
            val bitmap = BitmapFactory.decodeResource(contex.resources, imageId)

            glGenTextures(1, ids, 0)
            glBindTexture(GL_TEXTURE_2D, ids[0])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
            glGenerateMipmap(GL_TEXTURE_2D)
            glBindTexture(GL_TEXTURE_2D, ids[0])
            return ids[0]
        }

    }
}