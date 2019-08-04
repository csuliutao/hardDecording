package csu.liutao.ffmpegdemo.opgls

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.ativities.MediaPlayerActivity
import java.io.File

class OpenVideosAdapter(val context: Context) : RecyclerView.Adapter<OpenVideosAdapter.OpenHolder>() {
    private lateinit var inflater: LayoutInflater
    private val fileList = ArrayList<File>()

    init {
        inflater = LayoutInflater.from(context)
        OpglFileManger.instance.initDir(context, false)
    }

    fun update() {
        val datas = OpglFileManger.instance.getVedioFiles()
        if (datas.size == 0 || fileList.size == datas.size) return
        fileList.clear()
        fileList.addAll(datas)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenHolder {
        val item = inflater.inflate(R.layout.media_item, parent, false)
        return OpenHolder(item)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: OpenHolder, position: Int) {
        holder.button.text = fileList.get(position).name
        holder.button.setOnClickListener{
            val intent = Intent(context, MediaPlayerActivity::class.java)
            intent.putExtra(Utils.PLAY_FILE, fileList.get(position))
            context.startActivity(intent)
        }
    }


    class OpenHolder(item : View) : RecyclerView.ViewHolder(item) {
        lateinit var button : Button
        init {
            button = item as Button
        }
    }
}