package com.tokyonth.installer.utils.ktx

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object TaskJobKtx {

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
) = TaskJobKtx.Task(this, err, run)

/**
 * 异步任务
 */
inline fun <reified T> T.doAsync(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): TaskJobKtx.Task<T> {
    val task = task(err, run)
    TaskJobKtx.doAsync(task)
    return task
}

/**
 * 主线程
 */
inline fun <reified T> T.onUI(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): TaskJobKtx.Task<T> {
    val task = task(err, run)
    TaskJobKtx.onUI(task)
    return task
}

/**
 * 延迟任务
 */
inline fun <reified T> T.delay(
    delay: Long,
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): TaskJobKtx.Task<T> {
    val task = task(err, run)
    TaskJobKtx.delay(delay, task)
    return task
}
