package com.tokyonth.installer.apk;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.tokyonth.installer.Constants;
import com.tokyonth.installer.bean.ApkInfoBean;
import com.tokyonth.installer.utils.ShellUtils;

public class InstallApkTask extends Thread {

    private ApkInfoBean mApkInfo;
    private Handler handler;
    private CommanderCallback commanderCallback;

    InstallApkTask(Handler handler, CommanderCallback commanderCallback, ApkInfoBean mApkInfo) {
        this.handler = handler;
        this.mApkInfo = mApkInfo;
        this.commanderCallback = commanderCallback;
    }

    @Override
    public void run() {
        super.run();
        handler.post(() -> commanderCallback.onApkPreInstall(mApkInfo));
        if (Build.VERSION.SDK_INT >= 24) {
            ShellUtils.execWithRoot(Constants.SE_LINUX_COMMAND);
        }
        final int retCode = ShellUtils.execWithRoot(Constants.INSTALL_COMMAND + "\"" + mApkInfo.getApkFile().getPath() + "\"" + "\n", new ShellUtils.Result() {
            @Override
            public void onStdout(final String text) {
                handler.post(() -> commanderCallback.onInstallLog(mApkInfo, text));
            }

            @Override
            public void onStderr(final String text) {
                handler.post(() -> commanderCallback.onInstallLog(mApkInfo, text));
            }

            @Override
            public void onCommand(String command) {

            }

            @Override
            public void onFinish(int resultCode) {

            }
        });
        if (retCode == 0 && mApkInfo.isFakePath()) {
            if (!mApkInfo.getApkFile().delete()) {
                Log.e("InstallApkTask", "failed to delete！");
            }
        }
        handler.post(() -> commanderCallback.onApkInstalled(mApkInfo, retCode));
    }

}
