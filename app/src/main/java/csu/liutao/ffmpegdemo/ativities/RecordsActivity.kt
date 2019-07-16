package csu.liutao.ffmpegdemo.ativities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.audios.AudioMgr
import csu.liutao.ffmpegdemo.adapters.RecordAdapter
import csu.liutao.ffmpegdemo.audios.AudioRecordMgr
import csu.liutao.ffmpegdemo.audios.AudioTrackMgr

class RecordsActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter :RecordAdapter

    val recordPermissionCode = 10

    val hanlder = Handler() {

        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.records_activity)
        AudioMgr.mgr.initRecordDir(this)


        adapter = RecordAdapter(this)
        adapter.updateData(AudioMgr.mgr.getFiles())

        recyclerView = findViewById(R.id.recycle_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val hasAudio = packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, packageName) == PackageManager.PERMISSION_GRANTED
        if (!hasAudio && Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), recordPermissionCode)
        }

        AudioRecordMgr.instance.callback = object :AudioRecordMgr.OnRecordSucess {
            override fun onSucess() {
                runOnUiThread {
                    adapter.updateData(AudioMgr.mgr.getFiles())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioTrackMgr.instance.release()
    }
}