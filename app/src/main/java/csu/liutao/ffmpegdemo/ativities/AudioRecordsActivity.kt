package csu.liutao.ffmpegdemo.ativities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.Utils
import csu.liutao.ffmpegdemo.audios.AudioMgr
import csu.liutao.ffmpegdemo.adapters.RecordAdapter
import csu.liutao.ffmpegdemo.audios.AudioRecordMgr
import csu.liutao.ffmpegdemo.audios.AudioTrackMgr

class AudioRecordsActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter :RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.records_activity)
        AudioMgr.mgr.initRecordDir(this)


        adapter = RecordAdapter(this)
        adapter.updateData(AudioMgr.mgr.getFiles())

        recyclerView = findViewById(R.id.recycle_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        Utils.checkAudioPermission(this)

        AudioRecordMgr.instance.callback = object :AudioRecordMgr.OnRecordSucess {
            override fun onSucess() {
                runOnUiThread {
                    adapter.updateData(AudioMgr.mgr.getFiles())
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Utils.AUDIO_REQUESE_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioRecordMgr.instance.release()
        AudioTrackMgr.instance.release()
    }
}