package com.tokyonth.installer;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.tokyonth.installer.utils.SPUtils;

public class BaseApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        SPUtils.getInstance(this, Config.SP_FILE_NAME);

        boolean bool = (boolean)SPUtils.getData(Config.SP_NIGHT_MODE, false);
        if (bool) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static Context getContext(){
        return context;
    }

}
