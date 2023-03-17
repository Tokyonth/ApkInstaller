package com.tokyonth.installer.activity.crash

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.annotation.RestrictTo
import com.tokyonth.installer.utils.PackageUtils.getVersionName
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import kotlin.system.exitProcess

object ActivityOnCrash {

    private val TAG = ActivityOnCrash::class.java.simpleName

    private const val DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os"

    private const val STACK_TRACE_STRING_INTENT = "stackTraceString"

    private const val MAX_STACK_TRACE_SIZE = 131071 //128 KB

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun install(application: Application) {
        try {
            val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (oldHandler != null && !oldHandler.javaClass.name.startsWith(
                    DEFAULT_HANDLER_PACKAGE_NAME
                )
            ) {
                Log.e(TAG, "IMPORTANT WARNING! You already have an UncaughtExceptionHandler.")
            }
            Thread.setDefaultUncaughtExceptionHandler { _: Thread?, throwable: Throwable ->
                val intent = Intent(application, ErrorActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                var stackTraceString = sw.toString()
                if (stackTraceString.length > MAX_STACK_TRACE_SIZE) {
                    val disclaimer = " [stack trace too large]"
                    stackTraceString = stackTraceString.substring(
                        0,
                        MAX_STACK_TRACE_SIZE - disclaimer.length
                    ) + disclaimer
                }
                intent.putExtra(STACK_TRACE_STRING_INTENT, stackTraceString)
                application.startActivity(intent)
            }
        } catch (t: Throwable) {
            Log.e(TAG, t.toString())
        }
    }

    fun getErrorDetails(context: Context, intent: Intent): String {
        return buildString {
            append("Build Version: ${getVersionName(context)}")
            append("\n")
            append("SDK Version: ${Build.VERSION.SDK_INT}")
            append("\n")
            append("Device: ${getDeviceModelName()}")
            append("\n")
            append("Stack Trace:")
            append("\n")
            append(intent.getStringExtra(STACK_TRACE_STRING_INTENT))
        }
    }

    fun closeApplication(activity: Activity) {
        activity.finish()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    private fun getDeviceModelName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            first.uppercaseChar().toString() + s.substring(1)
        }
    }

}
