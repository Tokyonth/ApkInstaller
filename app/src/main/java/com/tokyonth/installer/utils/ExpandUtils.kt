package com.tokyonth.installer.utils

import android.app.Activity
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding

inline fun <reified T : ViewBinding> Activity.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> LayoutInflater.bind(): T {
    val layoutInflater: LayoutInflater = this
    val bindingClass = T::class.java
    val inflateMethod = bindingClass.getMethod("inflate", LayoutInflater::class.java)
    val invokeObj = inflateMethod.invoke(null, layoutInflater)
    if (invokeObj is T) {
        return invokeObj
    }
    throw InflateException("Cant inflate ViewBinding ${bindingClass.name}")
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
