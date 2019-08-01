package csu.liutao.ffmpegdemo.ativities

import android.content.Context
import android.opengl.GLSurfaceView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.opgls.AbsSingleProgramRender
import csu.liutao.ffmpegdemo.opgls.IProgram
import csu.liutao.ffmpegdemo.opgls.ImgProgram
import csu.liutao.ffmpegdemo.opgls.OpglBaseActivity

class OpglImageActivity :OpglBaseActivity() {
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