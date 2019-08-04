package csu.liutao.ffmpegdemo.opgls.activities

import android.content.Intent
import android.media.AudioRecord
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.aac.AudioRecordManager
import csu.liutao.ffmpegdemo.aac.AudioTrackManager
import csu.liutao.ffmpegdemo.medias.CodecManager
import csu.liutao.ffmpegdemo.opgls.OpenVideosAdapter

class OpglVediosActivity : AppCompatActivity(){
    private lateinit var recycleView : RecyclerView
    private lateinit var button : Button
    private lateinit var adapter : OpenVideosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opgl_vedios)
        recycleView = findViewById(R.id.video_list)
        button = findViewById(R.id.go_to_record)

        recycleView.layoutManager = LinearLayoutManager(this)
        adapter = OpenVideosAdapter(this)
        adapter.update()
        recycleView.adapter = adapter

        button.setOnClickListener {
            this.startActivity(Intent(this, OpglRecorderActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.update()
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioRecordManager.release()
        AudioTrackManager.release()
        CodecManager.releaseThread()
    }
}