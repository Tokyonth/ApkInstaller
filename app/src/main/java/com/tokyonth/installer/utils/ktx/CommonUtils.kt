package com.tokyonth.installer.utils.ktx

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.tokyonth.installer.App

object CommonUtils {

    fun getScreenHeight(): Int {
        val metrics = App.context.resources.displayMetrics
        return metrics.heightPixels
    }

    fun getScreenWidth(): Int {
        val metrics = App.context.resources.displayMetrics
        return metrics.widthPixels
    }

}

inline fun <reified V : View> V.click(interval: Long = 600L, noinline callback: V.() -> Unit) {

    var lastClickTime: Long = 0

    this.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            callback.invoke(this)
            lastClickTime = currentTime
        }
    }
}

inline fun <T : View> T.visibleOrGone(boolean: Boolean, onVisible: (T.() -> Unit) = {}) {
    visibility = if (boolean) {
        View.VISIBLE
    } else {
        View.GONE
    }
    if (boolean) {
        onVisible.invoke(this)
    }
}

fun toast(text: String) {
    Toast.makeText(App.context, text, Toast.LENGTH_SHORT).show()
}

fun color(@ColorRes colorId: Int): Int {
    return ContextCompat.getColor(App.context, colorId)
}

fun string(@StringRes stringId: Int, vararg args: Any?): String {
    return App.context.getString(stringId, *args)
}

fun Float.sp2px(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this,
        Resources.getSystem().displayMetrics
    )
}

fun Float.dp2px(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this,
        Resources.getSystem().displayMetrics
    )
}

fun Int.sp2px(): Float {
    return this.toFloat().sp2px()
}

fun Int.dp2px(): Float {
    return this.toFloat().dp2px()
}

fun Drawable.drawable2Bitmap(): Bitmap {
    return when (this) {
        is BitmapDrawable -> {
            this.bitmap
        }
        else -> {
            val config =
                if (this.opacity != PixelFormat.OPAQUE) {
                    Bitmap.Config.ARGB_8888
                } else {
                    Bitmap.Config.RGB_565
                }
            val bitmap = Bitmap.createBitmap(
                this.intrinsicWidth,
                this.intrinsicHeight,
                config
            )
            val canvas = Canvas(bitmap)
            this.setBounds(0, 0, canvas.width, canvas.height)
            this.draw(canvas)
            bitmap
        }
    }
}
