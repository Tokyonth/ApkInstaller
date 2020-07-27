package com.tokyonth.installer.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

class CircleImageView : AppCompatImageView {

    private var mRadius = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun getIconBitmap(drawable: Drawable): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            } else if (drawable is AdaptiveIconDrawable) {
                val bitmapIcon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmapIcon)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                return bitmapIcon
            }
        } else {
            return (drawable as BitmapDrawable).bitmap
        }
        return null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //由于是圆形，宽高应保持一致
        val size = min(measuredWidth, measuredHeight)
        mRadius = size / 2
        setMeasuredDimension(size, size)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val bitmap: Bitmap?
        val mPaint = Paint()
        val drawable = drawable
        if (null != drawable) {
            bitmap = getIconBitmap(drawable)
            //初始化BitmapShader，传入bitmap对象
            val bitmapShader: BitmapShader
            if (bitmap != null) {
                bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                //计算缩放比例
                //图片的宿放比例
                val mScale = mRadius * 2.0f / min(bitmap.height, bitmap.width)
                val matrix = Matrix()
                matrix.setScale(mScale, mScale)
                bitmapShader.setLocalMatrix(matrix)
                mPaint.shader = bitmapShader
                //画圆形，指定好坐标，半径，画笔
                canvas.drawCircle(mRadius.toFloat(), mRadius.toFloat(), mRadius.toFloat(), mPaint)
            }
        } else {
            super.onDraw(canvas)
        }
    }
}