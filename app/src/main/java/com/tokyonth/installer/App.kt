package com.tokyonth.installer

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.tokyonth.installer.data.LocalDataRepo
import me.weishu.reflection.Reflection

class App : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        LocalDataRepo.instance.run {
            if (isFollowSystem()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                AppCompatDelegate.setDefaultNightMode(
                    if (isNightMode())
                        AppCompatDelegate.MODE_NIGHT_YES
                    else
                        AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(this)
    }

}
