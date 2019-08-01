package csu.liutao.ffmpegdemo.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.ativities.*
import csu.liutao.ffmpegdemo.opgls.OpglBaseActivity

class MainRecycleAdapter(val context : Context): RecyclerView.Adapter<MainRecycleAdapter.MainHolder>() {
    private val decs = ArrayList<String>()
    private val intents = ArrayList<Intent>()
    private lateinit var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
        decs.add("surface展示图片")
        intents.add(Intent(context, SurfaceImgActivity::class.java))

        decs.add("aac音频录制播放")
        intents.add(Intent(context, AudioRecordsActivity::class.java))

        decs.add("camera2照片")
        intents.add(Intent(context, PictureActivity::class.java))

        /*decs.add("camera vedio")
        intents.add(Intent(context, VideosActivity::class.java))*/

        decs.add("aac+h264 音视频录制播放")
        intents.add(Intent(context, MediasActivity::class.java))

        decs.add("opengl es 学习使用")
        intents.add(Intent(context, OpglBaseActivity::class.java))

        decs.add("opengl es 图片展示")
        intents.add(Intent(context, OpglImageActivity::class.java))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val itemView = inflater.inflate(R.layout.main_item, parent, false)
        return MainHolder(itemView)
    }

    override fun getItemCount(): Int {
        return decs.size
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        holder.button.text = decs.get(position)
        val intent = intents.get(position)
        holder.button.setOnClickListener {
            context.startActivity(intent)
        }
    }



    class MainHolder(view : View) : RecyclerView.ViewHolder(view){
        lateinit var button : Button
        init {
            button = itemView as Button
        }
    }


}