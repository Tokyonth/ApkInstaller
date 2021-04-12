package com.tokyonth.installer.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

import androidx.appcompat.app.AppCompatDelegate

import com.tokyonth.installer.Constants
import com.tokyonth.installer.utils.CrashHandler
import com.tokyonth.installer.utils.SPUtils.get

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        if (get(Constants.SP_NIGHT_FOLLOW_SYSTEM, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
             val nightMode = if (get(Constants.SP_NIGHT_MODE, false))
                AppCompatDelegate.MODE_NIGHT_YES
             else
                AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
        CrashHandler().initCrashHandler(this)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set

    }

}

