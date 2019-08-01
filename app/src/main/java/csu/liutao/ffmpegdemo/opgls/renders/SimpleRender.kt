package csu.liutao.ffmpegdemo.opgls.renders

import android.content.Context
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.opgls.programs.SimpleProgram
import csu.liutao.ffmpegdemo.opgls.abs.AbsSingleProgramRender
import csu.liutao.ffmpegdemo.opgls.abs.IProgram

class SimpleRender(val context : Context) : AbsSingleProgramRender() {
    override fun initProgram(): IProgram {
        val program = SimpleProgram()
        program.prepare(context, R.raw.simple_vetex, R.raw.simple_fragment)
        return program
    }
}
