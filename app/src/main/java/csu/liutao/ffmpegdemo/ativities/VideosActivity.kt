package csu.liutao.ffmpegdemo.ativities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.adapters.MediasAdapter
import csu.liutao.ffmpegdemo.medias.MediaMgr

open class VideosActivity : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView

    private lateinit var button: Button

    private lateinit var adpter : MediasAdapter

    protected var recoderClass: Class<*> = VideoRecordActivity::class.java

    protected var playerClass: Class<*> = VideoPlayActivity::class.java

    protected var isVideo = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medias)
        MediaMgr.instance.initDir(this, isVideo)

        recyclerView = findViewById(R.id.vedio_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adpter = MediasAdapter(this, isVideo)
        adpter.playerClass = playerClass
        recyclerView.adapter = adpter

        button = findViewById(R.id.go_to_record)
        button.setOnClickListener{
            onClick()
        }
    }

    private fun onClick() {
        startActivity(Intent(this, recoderClass))
    }

    override fun onResume() {
        super.onResume()
        adpter.updateView()
    }
}