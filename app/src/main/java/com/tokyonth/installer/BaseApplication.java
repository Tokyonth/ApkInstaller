package com.tokyonth.installer;

import android.app.Application;
import android.content.Context;

import com.tokyonth.installer.utils.SPUtils;

public class BaseApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        SPUtils.getInstance(this, "conf");
    }

    public static Context getContext(){
        return context;
    }

}
