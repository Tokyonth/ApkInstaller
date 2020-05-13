package com.tokyonth.installer.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.tokyonth.installer.Constants;
import com.tokyonth.installer.utils.SPUtils;

public class BaseApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        SPUtils.getInstance(this, Constants.SP_FILE_NAME);

        int NIGHT_MODE = (boolean) SPUtils.getData(Constants.SP_NIGHT_MODE, false) ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(NIGHT_MODE);
    }

    public static Context getContext(){
        return context;
    }

}

