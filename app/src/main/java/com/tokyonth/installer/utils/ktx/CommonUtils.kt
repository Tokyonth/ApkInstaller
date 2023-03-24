package com.tokyonth.installer.utils.ktx

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.tokyonth.installer.App
import com.tokyonth.installer.R

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
