package csu.liutao.ffmpegdemo.opgls

import android.content.Context
import csu.liutao.ffmpegdemo.R

class SimpleRender(val context : Context) : AbsSingleProgramRender() {
    override fun initProgram(): IProgram {
        val program = SimpleProgram()
        program.prepare(context, R.raw.simple_vetex, R.raw.simple_fragment)
        return program
    }
}
