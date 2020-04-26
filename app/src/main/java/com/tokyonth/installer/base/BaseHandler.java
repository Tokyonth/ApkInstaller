package com.tokyonth.installer.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tokyonth.installer.apk.CommanderCallback;

import org.jetbrains.annotations.NotNull;

public class BaseHandler extends Handler {

    private CommanderCallback callBack;

    public interface CallBack {
        void handleMessage(Message msg);
    }

    public BaseHandler(CommanderCallback callBack, Looper looper) {
        super(looper);
        this.callBack = callBack;
    }

    @Override
    public void handleMessage(@NotNull Message msg) {
        if(null != callBack){
           // callBack.onApkInstalled(msg);
        }
    }

}
