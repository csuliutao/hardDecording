package csu.liutao.ffmpegdemo.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.audios.AudioTrackMgr
import csu.liutao.ffmpegdemo.audios.AudioMgr
import java.io.File

class RecordAdapter(var context: Activity): RecyclerView.Adapter<RecordAdapter.ViewHolder>(), AudioTrackMgr.FinishListener {
    private val files = ArrayList<File>()
    private lateinit var inflater : LayoutInflater

    private var prePos : Int = AudioMgr.INVALID_POS
    private var curPos : Int = AudioMgr.INVALID_POS
    private var isPause = true

    init {
        inflater = LayoutInflater.from(context)
        AudioTrackMgr.instance.pauseListener = this
    }

    override fun onFinished() {
        context.runOnUiThread {
            isPause = true
            notifyItemChanged(curPos)
        }
    }

    fun updateData(list : List<File>) {
        files.clear()
        files.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = inflater.inflate(R.layout.record_item, parent, false)
        return ViewHolder(item)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = files.get(position).name
        var paused = true
        if (curPos == position) {
            paused = isPause
        }
        holder.setImgRes(paused)

        holder.imgView.setOnClickListener(ClickListener(this,position, holder))
    }

    class ClickListener(var adapter: RecordAdapter,val pos : Int,val holder: ViewHolder) : View.OnClickListener{

        override fun onClick(v: View?) {
            var isSamePos = adapter.curPos == pos
            if (!isSamePos) {
                adapter.prePos = adapter.curPos
                adapter.curPos = pos
                adapter.isPause = false
                AudioTrackMgr.instance.play(adapter.files.get(pos))
            } else {
                adapter.isPause = !adapter.isPause
                if (adapter.isPause) {
                    AudioTrackMgr.instance.pause()
                } else {
                    AudioTrackMgr.instance.replay()
                }
            }
            adapter.notifyItemChanged(adapter.curPos)
            if (adapter.prePos != AudioMgr.INVALID_POS) adapter.notifyItemChanged(adapter.prePos)
        }
    }

    class ViewHolder(item : View) : RecyclerView.ViewHolder(item){
        lateinit var imgView:ImageView
        lateinit var textView:TextView
        init {
            imgView = itemView.findViewById(R.id.image)
            textView = itemView.findViewById(R.id.name)
        }

        fun setImgRes(isPaused : Boolean) {
            if (isPaused) {
                imgView.setImageResource(R.drawable.play)
            } else {
                imgView.setImageResource(R.drawable.pause)
            }
        }
    }
}