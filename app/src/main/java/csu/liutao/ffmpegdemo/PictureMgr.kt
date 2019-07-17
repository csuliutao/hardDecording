package csu.liutao.ffmpegdemo

import android.content.Context
import csu.liutao.ffmpegdemo.audios.AudioRecordMgr
import java.io.File

class PictureMgr private constructor(){
    private val PATH = "pics"
    private val END_TAG = ".jpg"

    private lateinit var picDir :String


    fun initDir(context : Context) {
        picDir = context.filesDir.canonicalPath + File.separator + PATH
        val file = File(picDir)
        if (!file.exists()) file.mkdir()
    }

    fun getLastPic() : File? {
        val file = File(picDir)
        if (!file.exists()) return null
        val files = file.listFiles()
        if(files.size == 0) return null
        return files[files.size - 1]
    }

    fun getFile() : File {
        return Utils.getNewFile(AudioRecordMgr.TIME_FORMAT, picDir, END_TAG)
    }


    companion object {
        val instance = PictureMgr()
    }
}