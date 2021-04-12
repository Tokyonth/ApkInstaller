package com.tokyonth.installer.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.animation.LinearInterpolator
import kotlin.math.min

class ProgressDrawable: Drawable(), Animatable, ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
       // strokeCap = Paint.Cap.ROUND
    }

    private val colorList = ArrayList<Int>()

    private var colorIndex = 0

    private var radius = 0F

    private var strokeWidthWeight = 0.25F

    private var startAngle = 0F

    private var sweepAngle = 0F

    private var ovalBounds = RectF()

    private var rotateStep = 360F

    private var sweepStep = 300F

    private var lastAnimationProgress = 0F

    private var lastStep = 0F

    companion object {
        private const val TAG = "ProgressDrawable"
    }

    private val animator = ValueAnimator.ofFloat(0F, 2F).apply {
        addUpdateListener(this@ProgressDrawable)
        addListener(this@ProgressDrawable)
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        interpolator = LinearInterpolator()
    }

    override fun draw(canvas: Canvas) {
        if (colorList.isEmpty() || radius < 1) {
            return
        }
        //logD("draw() -> startAngle: $startAngle, sweepAngle: $sweepAngle")
        canvas.drawArc(ovalBounds, startAngle, sweepAngle, false, paint)
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        if (bounds == null) {
            return
        }
        radius = min(bounds.width(), bounds.height()) * 0.5F
        val strokeWidth = radius * strokeWidthWeight
        radius -= strokeWidth * 0.5F
        paint.strokeWidth = strokeWidth
        val top = bounds.exactCenterY() - radius
        val left = bounds.exactCenterX() - radius
        val right = bounds.exactCenterX() + radius
        val bottom = bounds.exactCenterY() + radius
        ovalBounds.set(left, top, right, bottom)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.TRANSPARENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    fun putColor(vararg colors: Int) {
        colorList.clear()
        for (color in colors) {
            colorList.add(color)
        }
    }

    fun progress(progress: Int, max: Int) {
        stop()
        paint.color = colorList[0]
        startAngle = 0F
        sweepAngle = 360F * progress / max
        invalidateSelf()
    }

    fun animatorDuration(value: Long) {
        animator.duration = value
    }

    override fun isRunning(): Boolean {
        return animator.isRunning
    }

    override fun start() {
        if (colorList.isEmpty()) {
            animator.cancel()
            return
        }
        animator.start()
        invalidateSelf()
    }

    override fun stop() {
        animator.cancel()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == animator) {
            val value = animator.animatedValue as Float
           // logD("onAnimationUpdate($value)")
            val isShrink = value > 1
            val sweepValue = if (value > 1) { 2 - value } else { value }
            val sweepLength = sweepStep * sweepValue
            val startValue = if (value < lastAnimationProgress) {
                2F - lastAnimationProgress + value
            } else {
                value - lastAnimationProgress
            }
            lastAnimationProgress = value

            val step = startValue * rotateStep
            startAngle += step
            if (step < 1) {
                startAngle += lastStep
            } else {
                lastStep = step
            }

            if (isShrink) {
                val diff = sweepAngle - sweepLength
                startAngle += diff
            }
            sweepAngle = sweepLength
            startAngle %= 360

            invalidateSelf()
        }
    }

    override fun onAnimationRepeat(animation: Animator?) {
        colorIndex ++
        colorIndex %= colorList.size
        paint.color = colorList[colorIndex]
        // logD("onAnimationRepeat($colorIndex)")
    }

    override fun onAnimationEnd(animation: Animator?) {}

    override fun onAnimationCancel(animation: Animator?) {}

    override fun onAnimationStart(animation: Animator?) {
        colorIndex ++
        colorIndex %= colorList.size
        paint.color = colorList[colorIndex]
        logD("onAnimationStart($colorIndex)")
    }

    private fun logD(value: String) {
        Log.d("Lollipop", "$TAG: $value")
    }

}

