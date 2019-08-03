package csu.liutao.ffmpegdemo.opgls.activities

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.opgls.OpglFileManger
import csu.liutao.ffmpegdemo.opgls.abs.AbsSingleProgramRender
import csu.liutao.ffmpegdemo.opgls.abs.IProgram
import csu.liutao.ffmpegdemo.opgls.programs.ImgProgram

class OpglImageActivity : OpglBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpglFileManger.instance.initDir(this, true)
    }

    override fun getRender(): GLSurfaceView.Renderer {
        return ImageRender(this)
    }

    class ImageRender(val context : Context) : AbsSingleProgramRender(){
        override fun initProgram(): IProgram {
            val program = ImgProgram()
            program.prepare(context, R.raw.img_base_vertex, R.raw.img_base_frag)
            return program
        }
    }
}