package csu.liutao.ffmpegdemo.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import csu.liutao.ffmpegdemo.PictureMgr
import csu.liutao.ffmpegdemo.R

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SurfaceImgView public constructor(context: Context, attrs: AttributeSet? = null, attrStyle: Int = 0, resStyle: Int = 0) :
    SurfaceView(context, attrs, attrStyle, resStyle), SurfaceHolder.Callback{

    constructor(context: Context):this(context, null, 0, 0)

    constructor(context: Context, attrs: AttributeSet? = null):this(context, attrs, 0 ,0)

    constructor(context: Context, attrs: AttributeSet? = null, attrStyle: Int = 0):this(context, attrs, attrStyle ,0)

    private lateinit var bitm : Bitmap
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        PictureMgr.instance.initDir(context)
        val file = PictureMgr.instance.getLastPic()
        if (file == null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ImgView)
            val imgId = typeArray.getResourceId(
                R.styleable.ImgView_imgResource,
                R.drawable.ic_launcher_background
            )
            typeArray.recycle()
            bitm = BitmapFactory.decodeResource(resources, imgId)
        } else {
            bitm = BitmapFactory.decodeFile(file.canonicalPath)
        }
        holder.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (holder != null) {
            val canvas = holder.lockCanvas()
            val wScale = canvas.width.toFloat() / bitm.width
            val hScale = canvas.height.toFloat() / bitm.height
            canvas.save()
            canvas.scale(wScale, hScale)
            canvas.drawBitmap(bitm, 0F, 0F, paint)
            canvas.restore()
            holder.unlockCanvasAndPost(canvas)
        }
    }

}