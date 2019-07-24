package csu.liutao.ffmpegdemo.ativities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.adapters.MediasAdapter
import csu.liutao.ffmpegdemo.medias.MediaMgr

open class VideosActivity : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView

    private lateinit var button: Button

    private lateinit var adpter : MediasAdapter

    protected var recoderClass: Class<*> = VideoRecordActivity::class.java

    protected var playerClass: Class<*> = VideoPlayActivity::class.java

    /**
     * 是h264 还是h264+aac
     */
    protected var isVideo = true

    protected val code = 10

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
        startActivityForResult(Intent(this, recoderClass), code)
//        startActivity(Intent(this, recoderClass))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.log("onActivityResult")
        val file = data?.getStringExtra(MediaMgr.instance.FILE_PATH)
        if (file == null) return
        if (requestCode == code) {
            if (MediaMgr.instance.saveRecordFile(file)){
                Utils.log("onActivityResult saveFile")
                adpter.updateView()
            }
        }
    }
}