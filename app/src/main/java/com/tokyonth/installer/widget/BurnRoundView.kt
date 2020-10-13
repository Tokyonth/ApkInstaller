package com.tokyonth.installer.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.AssemblyUtils
import kotlin.math.min

class BurnRoundView : View {

    private var mWidth = 0
    private var mHeight = 0
    private var burnColor = 0
    private var isBurn = false
    private var burnSrc: Bitmap? = null
    private var mContext: Context? = null
    private var overlayPaint: Paint? = null
    private var mPaint: Paint? = null

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.mContext = context
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.mContext = context
        initView(attrs)
    }

    @SuppressLint("Recycle")
    private fun initView(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BurnRoundView)
        val imageId = array.getResourceId(R.styleable.BurnRoundView_burnSrc, 0)
        val color = array.getColor(R.styleable.BurnRoundView_burnColor, Color.RED)
        overlayPaint = Paint()
        mPaint = Paint()
        replaceImageColor(imageId, color)
        isBurn = array.getBoolean(R.styleable.BurnRoundView_isBurn, true)
        burnColor = if (isBurn) {
            color and 0x60FFFFFF
        } else {
            color
        }
    }

    private fun replaceImageColor(imageId: Int, color: Int) {
        burnSrc = AssemblyUtils.getBitmapFromDrawable(context, imageId)
        overlayPaint!!.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun setBurnColor(color: Int, isBurn: Boolean): Int {
        this.isBurn = isBurn
        burnColor = if (isBurn) {
            color and 0x60FFFFFF
        } else {
            color
        }
        invalidate()
        return color
    }

    fun setBurnSrc(burnSrc: Int, color: Int, isBurn: Boolean) {
        replaceImageColor(burnSrc, setBurnColor(color, isBurn))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //设置画笔的样式，空心STROKE
        mPaint!!.style = Paint.Style.FILL
        //设置抗锯齿
        mPaint!!.isAntiAlias = true
        mPaint!!.color = burnColor
        //width >> 1 与 height >> 1为圆心位置
        canvas.drawBitmap(burnSrc!!, (width shr 1) - (burnSrc!!.width shr 1).toFloat(),
                (height shr 1) - (burnSrc!!.height shr 1).toFloat(), overlayPaint)
        canvas.drawCircle((width shr 1.toFloat().toInt()).toFloat(),
                (height shr 1.toFloat().toInt()).toFloat(), (measuredWidth / 2.5).toFloat(), mPaint!!)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
        mWidth = measureWidth(widthMeasureSpec)
        mHeight = measureHeight(heightMeasureSpec)
    }

    private fun measureWidth(measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = DEFAULT_SIZE
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    private fun measureHeight(measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = DEFAULT_SIZE
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    companion object {
        private const val DEFAULT_SIZE = 72
    }

}
