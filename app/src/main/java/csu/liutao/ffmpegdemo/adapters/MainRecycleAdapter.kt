package csu.liutao.ffmpegdemo.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.ativities.PictureActivity
import csu.liutao.ffmpegdemo.ativities.RecordsActivity
import csu.liutao.ffmpegdemo.ativities.SurfaceImgActivity

class MainRecycleAdapter(val context : Context): RecyclerView.Adapter<MainRecycleAdapter.MainHolder>() {
    private val decs = ArrayList<String>()
    private val intents = ArrayList<Intent>()
    private lateinit var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
        decs.add("surface image view display")
        intents.add(Intent(context, SurfaceImgActivity::class.java))

        decs.add("audio record , audio track demo")
        intents.add(Intent(context, RecordsActivity::class.java))

        decs.add("camera take picture")
        intents.add(Intent(context, PictureActivity::class.java))
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