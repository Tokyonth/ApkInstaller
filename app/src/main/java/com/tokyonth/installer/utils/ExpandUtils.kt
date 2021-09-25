package com.tokyonth.installer.utils

import android.app.Activity
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object ExpandUtils {

    /**
     * 异步线程池
     */
    private val threadPool: Executor by lazy {
        Executors.newScheduledThreadPool(5)
    }

    /**
     * 主线程的handler
     */
    private val mainThread: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    /**
     * 异步任务
     */
    fun <T> doAsync(task: Task<T>) {
        threadPool.execute(task.runnable)
    }

    /**
     * 主线程
     */
    fun <T> onUI(task: Task<T>) {
        mainThread.post(task.runnable)
    }

    /**
     * 延迟任务
     */
    fun <T> delay(delay: Long, task: Task<T>) {
        mainThread.postDelayed(task.runnable, delay)
    }

    /**
     * 移除任务
     */
    fun <T> remove(task: Task<T>) {
        mainThread.removeCallbacks(task.runnable)
    }

    /**
     * 将一组对象打印合并为一个字符串
     */
    fun print(value: Array<out Any>): String {
        if (value.isEmpty()) {
            return ""
        }
        val iMax = value.size - 1
        val b = StringBuilder()
        var i = 0
        while (true) {
            b.append(value[i].toString())
            if (i == iMax) {
                return b.toString()
            }
            b.append(" ")
            i++
        }
    }

    /**
     * 包装的任务类
     * 包装的意义在于复用和移除任务
     * 由于Handler任务可能造成内存泄漏，因此在生命周期结束时，有必要移除任务
     * 由于主线程的Handler使用了全局的对象，移除不必要的任务显得更为重要
     * 因此包装了任务类，以任务类为对象来保留任务和移除任务
     */
    class Task<T>(
            private val target: T,
            private val err: ((Throwable) -> Unit) = {},
            private val run: T.() -> Unit
    ) {

        val runnable = Runnable {
            try {
                run(target)
            } catch (e: Throwable) {
                err(e)
            }
        }

        fun cancel() {
            remove(this)
        }

        fun run() {
            doAsync(this)
        }

        fun sync() {
            onUI(this)
        }

        fun delay(time: Long) {
            delay(time, this)
        }
    }

}

/**
 * 用于创建一个任务对象
 */
inline fun <reified T> T.task(
        noinline err: ((Throwable) -> Unit) = {},
        noinline run: T.() -> Unit
) = ExpandUtils.Task(this, err, run)

/**
 * 异步任务
 */
inline fun <reified T> T.doAsync(
        noinline err: ((Throwable) -> Unit) = {},
        noinline run: T.() -> Unit
): ExpandUtils.Task<T> {
    val task = task(err, run)
    ExpandUtils.doAsync(task)
    return task
}

/**
 * 主线程
 */
inline fun <reified T> T.onUI(
        noinline err: ((Throwable) -> Unit) = {},
        noinline run: T.() -> Unit
): ExpandUtils.Task<T> {
    val task = task(err, run)
    ExpandUtils.onUI(task)
    return task
}

/**
 * 延迟任务
 */
inline fun <reified T> T.delay(
        delay: Long,
        noinline err: ((Throwable) -> Unit) = {},
        noinline run: T.() -> Unit
): ExpandUtils.Task<T> {
    val task = task(err, run)
    ExpandUtils.delay(delay, task)
    return task
}

inline fun <reified T : ViewBinding> Activity.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> Fragment.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> View.lazyBind(): Lazy<T> = lazy { bind() }

inline fun <reified T : ViewBinding> Activity.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> Fragment.bind(): T {
    return this.layoutInflater.bind()
}

inline fun <reified T : ViewBinding> View.bind(): T {
    return LayoutInflater.from(this.context).bind()
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

inline fun <reified T : ViewBinding> View.withThis(inflate: Boolean = false): Lazy<T> = lazy {
    val bindingClass = T::class.java
    val view: View = this
    if (view is ViewGroup && inflate) {
        val bindMethod = bindingClass.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.javaPrimitiveType
        )
        val bindObj = bindMethod.invoke(null, LayoutInflater.from(context), view, true)
        if (bindObj is T) {
            return@lazy bindObj
        }
    } else {
        val bindMethod = bindingClass.getMethod(
                "bind",
                View::class.java
        )
        val bindObj = bindMethod.invoke(null, view)
        if (bindObj is T) {
            return@lazy bindObj
        }
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

/**
 * 对一个颜色值设置它的透明度
 * 只支持#AARRGGBB格式排列的颜色值
 */
fun Int.alpha(a: Int): Int {
    return this and 0xFFFFFF or ((a % 255) shl 24)
}

/**
 * 以浮点数的形式，以当前透明度为基础，
 * 调整颜色值的透明度
 */
fun Int.alpha(f: Float): Int {
    return this.alpha(((this shr 24) * f).toInt().range(0, 255))
}

/**
 * 将一个浮点数，以dip为单位转换为对应的像素值
 */
fun Float.dp2px(): Float {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this,
            Resources.getSystem().displayMetrics
    )
}

/**
 * 将一个浮点数，以sp为单位转换为对应的像素值
 */
fun Float.sp2px(): Float {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, this,
            Resources.getSystem().displayMetrics
    )
}

/**
 * 将一个整数，以dip为单位转换为对应的像素值
 */
fun Int.dp2px(): Float {
    return this.toFloat().dp2px()
}

/**
 * 将一个整数，以dip为单位转换为对应的像素值
 */
fun Int.sp2px(): Float {
    return this.toFloat().sp2px()
}

/**
 * 一个整形的范围约束
 */
fun Int.range(min: Int, max: Int): Int {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}
