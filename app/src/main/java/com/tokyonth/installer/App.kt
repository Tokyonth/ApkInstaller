package com.tokyonth.installer

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.tokyonth.installer.data.LocalDataRepo
import me.weishu.reflection.Reflection

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        localData = LocalDataRepo(applicationContext)
        if (localData.isFollowSystem()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    if (localData.isNightMode())
                        AppCompatDelegate.MODE_NIGHT_YES
                    else
                        AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(this)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var localData: LocalDataRepo

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

    }

}
