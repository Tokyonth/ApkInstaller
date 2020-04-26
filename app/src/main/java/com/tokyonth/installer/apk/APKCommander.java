package com.tokyonth.installer.apk;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.tokyonth.installer.bean.ApkInfoBean;
import com.tokyonth.installer.bean.permissions.PermInfoBean;

public class APKCommander extends ParseApkTask {

    private CommanderCallback callback;
    private Handler handler;
    private ApkInfoBean ApkInfo;
    private PermInfoBean permInfo;

    public ApkInfoBean getApkInfo() {
        return ApkInfo;
    }

    public PermInfoBean getPermInfo() {
        return permInfo;
    }

    @Override
    protected void setApkInfo(ApkInfoBean mApkInfo) {
        this.ApkInfo = mApkInfo;
    }

    @Override
    protected void setPermInfo(PermInfoBean permInfo) {
        this.permInfo = permInfo;
    }

    public APKCommander(Context context, Uri uri, CommanderCallback commanderCallback, String referrer) {
        this.callback = commanderCallback;
        handler = new Handler(Looper.getMainLooper());
        StartParseApkTask(uri, context, handler, callback, referrer);
        start();
    }

    public void startInstall() {
        new InstallApkTask(handler, callback, getApkInfo()).start();
    }

}
