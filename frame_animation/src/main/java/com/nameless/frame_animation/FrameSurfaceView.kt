package com.nameless.frame_animation

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * @author Zhang
 * @data: 2020/12/31 14:20
 * 类说明：
 */
class FrameSurfaceView : SurfaceView, SurfaceHolder.Callback {

    private var handlerThread: HandlerThread? = null
    private var surfaceHandler: Handler? = null
    private var isCreate = false

    private var frameBitmap: Bitmap? = null
    private var filePath: List<String>? = null
    private val srcRect = Rect()
    private val dstRect = Rect()
    private val options = BitmapFactory.Options()
    private val paint = Paint()
    private var bitmapIndex = 0
    private val cleanMode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val srcMode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    var onCompletionsListener: OnCompletionsListener? = null
    private var rootWidth = 0
    private var rootHeight = 0
    var alreadyShowTime = 0
    var shouldShowTime = 1
    var frameRateTime = 10L

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setZOrderOnTop(true)
        options.inMutable = true

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isCreate = false
        setZOrderOnTop(false)

        handlerThread?.quitSafely()

        filePath = null
        if (frameBitmap != null && !frameBitmap!!.isRecycled) {
            frameBitmap?.recycle()
            frameBitmap = null
        }
        options.inBitmap = null
        surfaceHandler = null
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isCreate = true
        handlerThread = HandlerThread("FrameSurfaceViewThread")
        handlerThread?.start()
        surfaceHandler = Handler(handlerThread!!.looper)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        rootHeight = height
        rootWidth = width
    }

    //本地文件的路径 手机sd卡上路径
    fun setList(filePath: List<String>?) {
        if(filePath!=null){
            this.filePath = filePath
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath[0], options)
            srcRect.set(0, 0, options.outWidth, options.outHeight)
            //16比9显示
            val aspectRatio = 16 / 9
            frameRateTime = 1000L / 30  //一秒30次
            val canvasHeight = width * aspectRatio
            dstRect.set(0, rootHeight - canvasHeight, width, rootHeight)
        }
    }


    fun start() {
        bitmapIndex = 0
        alreadyShowTime = 0
        surfaceHandler?.post(FrameRunnable())
    }

    inner class FrameRunnable : Runnable {

        override fun run() {

            //1.获取画布
            val canvas = holder.lockCanvas()
            //2.绘制一帧
            drawFrame(canvas)
            //3.将帧数据提交
            holder.unlockCanvasAndPost(canvas)
            if (filePath?.size ?: 0 > bitmapIndex) {
                if (isCreate) {
                    surfaceHandler?.postDelayed(this, frameRateTime)
                }
            } else {
                alreadyShowTime++
//                Log.e("ssss", "已经完成${alreadyShowTime}次")
                onCompletionsListener?.onEveryFinishTime(alreadyShowTime)
                if (shouldShowTime > alreadyShowTime) {
                    bitmapIndex = 0
                    if (isCreate) {
                        surfaceHandler?.postDelayed(this, frameRateTime)
                    }
                } else {
//                    Log.e("ssss", "已经全部完成")
                    drawClean()
                    onCompletionsListener?.onTotalFinish()
                }
            }

        }

        private fun drawFrame(canvas: Canvas) {
            //清空之前画布
            paint.xfermode = cleanMode
            canvas.drawPaint(paint)
            paint.xfermode = srcMode
            if (bitmapIndex == 0) {
                onCompletionsListener?.onEveryStartTime(alreadyShowTime)
            }
            frameBitmap = BitmapFactory.decodeFile(filePath!![bitmapIndex], options)

            //复用上一帧Bitmap的内存
            options.inBitmap = frameBitmap
            if (frameBitmap != null) {
                canvas.drawBitmap(frameBitmap!!, srcRect, dstRect, paint)
            }
            bitmapIndex++
        }

        private fun drawClean() {
            val canvas = holder.lockCanvas()
            paint.xfermode = cleanMode
            canvas.drawPaint(paint)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun frameRelease() {
        onCompletionsListener = null
        holder.removeCallback(this)
    }

    interface OnCompletionsListener {
        //每次结束
        fun onEveryFinishTime(int: Int)

        //全部结束
        fun onTotalFinish()

        //每次开始
        fun onEveryStartTime(int: Int)
    }

}