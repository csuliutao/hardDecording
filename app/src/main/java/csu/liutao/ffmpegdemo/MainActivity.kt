package csu.liutao.ffmpegdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import csu.liutao.ffmpegmgr.FfmpegMgr
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.sample_text)
        textView.text = FfmpegMgr.getInstance().getStringFromNative(666)
    }
}
