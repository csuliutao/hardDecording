package csu.liutao.ffmpegdemo.opgls

import android.content.Context
import csu.liutao.ffmpegdemo.Utils
import java.io.File
import java.io.FileFilter

class OpglFileManger private constructor(){
    private val JPEG_PATH = "opglpicture"
    private val JPEG_TAG = ".jpg"

    private val VEDIO_PATH = "opglvedio"
    private val VEDIO_TAG = ".mp4"

    private lateinit var picDir :String

    private lateinit var vedioDir :String


    fun initDir(context : Context, isPicture : Boolean = false) {
        var file : File
        if (isPicture) {
            picDir = context.externalCacheDir.canonicalPath + File.separator + JPEG_PATH
            file = File(picDir)
        } else {
            picDir = context.externalCacheDir.canonicalPath + File.separator + VEDIO_PATH
            file = File(vedioDir)
        }
        if (!file.exists()) file.mkdir()
    }

    fun getLastPic() : File? {
        val file = File(picDir)
        if (!file.exists()) return null
        val files = file.listFiles()
        if(files.isEmpty()) return null
        return files[files.size - 1]
    }

    fun getFile(isPic : Boolean = false) : File = if (isPic) Utils.getNewFile(picDir, JPEG_TAG) else Utils.getNewFile(vedioDir, VEDIO_TAG)

    fun getVedioFiles() : List<File> {
        val file = File(vedioDir)
        val lists = file.listFiles {
                pathname -> pathname != null && pathname.exists() && pathname.length() > 0
        }
        return lists.asList()
    }

    companion object {
        val instance = OpglFileManger()
    }
}