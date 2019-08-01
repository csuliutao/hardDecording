package csu.liutao.ffmpegdemo.opgls.renders

import android.content.Context
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.opgls.programs.MalletProgram
import csu.liutao.ffmpegdemo.opgls.programs.TableProgram
import csu.liutao.ffmpegdemo.opgls.abs.AbsMutiProgramRender
import csu.liutao.ffmpegdemo.opgls.abs.IProgram

class TextureRender(context : Context) : AbsMutiProgramRender(context) {
    override fun initPrograms(): List<IProgram> {
        val programs = ArrayList<IProgram>()

        val table = TableProgram()
        table.prepare(context, R.raw.table_vertex, R.raw.table_frag)
        programs.add(table)

        val mallet = MalletProgram()
        mallet.prepare(context, R.raw.mallet_vertex, R.raw.mallet_frag)
        programs.add(mallet)

        return programs
    }
}