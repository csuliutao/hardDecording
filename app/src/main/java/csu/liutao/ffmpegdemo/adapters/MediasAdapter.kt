package csu.liutao.ffmpegdemo.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.ativities.MediaPlayActivity
import csu.liutao.ffmpegdemo.medias.MediaMgr
import java.io.File

class MediasAdapter(var context : Context) : RecyclerView.Adapter<MediasAdapter.Holder>() {
    private val medias = ArrayList<File>()
    private lateinit var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
        refreshData()
    }

    private fun refreshData() : Boolean {
        val files = MediaMgr.instance.getAllFiles()
        if (files.size != medias.size) {
            medias.clear()
            medias.addAll(files)
            return true
        }
        return false
    }

    fun updateView () {
        if (refreshData()) {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = inflater.inflate(R.layout.media_item, parent, false)
        return Holder(itemView)
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.button.text = medias.get(position).name
        holder.button.setOnClickListener {
            val intent = Intent(context, MediaPlayActivity::class.java)
            intent.putExtra(Utils.PLAY_FILE, medias.get(position))
            context.startActivity(intent)
        }

    }

    class Holder(item : View) : RecyclerView.ViewHolder(item) {
        lateinit var button : Button
        init {
            button = item as Button
        }
    }
}