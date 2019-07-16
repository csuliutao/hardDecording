package csu.liutao.ffmpegdemo.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import androidx.annotation.RequiresApi
import csu.liutao.ffmpegdemo.R
import csu.liutao.ffmpegdemo.audios.AudioRecordMgr

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class AudioRecordView(con: Context, attrs: AttributeSet?, attrStyle: Int, resStyle: Int) :
    Button(con, attrs, attrStyle, resStyle) {

    var state = AudioRecordMgr.RecordState.RELEASE
        set(value) {
            field = value
            text = value.display
            invalidate()
        }
        get() = field

    private var isTodash = false
    private var startY = 0F


    constructor(con: Context, attrs: AttributeSet?, attrStyle: Int): this(con, attrs, attrStyle, 0)
    constructor(con: Context, attrs: AttributeSet?):this(con, attrs, R.attr.buttonStyle, 0)
    constructor(con:Context) : this(con, null, R.attr.buttonStyle, 0)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        super.onTouchEvent(event)
        when(event!!.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = event!!.y
                isTodash = false
                AudioRecordMgr.instance.paly()
                state = AudioRecordMgr.instance.curState
            }
            MotionEvent.ACTION_MOVE -> {
                val curY = event!!.y
                if (curY < startY) {
                    isTodash = true
                    state = AudioRecordMgr.RecordState.CANCEL
                } else {
                    state = AudioRecordMgr.instance.curState
                }
            }
            MotionEvent.ACTION_UP ->{
                AudioRecordMgr.instance.pause()
                state = AudioRecordMgr.instance.curState
            }
        }
        return true
    }
}