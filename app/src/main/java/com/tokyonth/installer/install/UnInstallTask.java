package com.tokyonth.installer.install;

import android.content.Context;
import android.os.Handler;

import com.catchingnow.icebox.sdk_client.IceBox;
import com.tokyonth.installer.Constants;
import com.tokyonth.installer.bean.ApkInfoBean;
import com.tokyonth.installer.utils.Shell;
import com.tokyonth.installer.utils.ShellUtils;
import com.tokyonth.installer.utils.ShizukuShell;

public class UnInstallTask extends Thread {

    private String pkgName;
    private int mode;
    private Context context;
    private CommanderCallback commanderCallback;
    private ApkInfoBean apkInfoBean;
    private Handler handler;

    public UnInstallTask(int mode, String pkgName, Context context, Handler handler, CommanderCallback commanderCallback) {
        this.mode = mode;
        this.pkgName = pkgName;
        this.context = context;
        this.handler = handler;
        this.commanderCallback = commanderCallback;
        fakeDate();
    }

    private void fakeDate() {
        apkInfoBean = new ApkInfoBean();
    }

    public void shellMode() {
        int resultCode = ShellUtils.execWithRoot(Constants.INSTANCE.getUNFREEZE_COMMAND() + pkgName);
        commanderCallback.onApkInstalled(apkInfoBean, resultCode);
    }

    @Override
    public void run() {
        super.run();
        switch (mode) {
            case 1:
                ShizukuShell shell = new ShizukuShell();
                Shell.Result result = shell.exec(new Shell.Command("pm", "uninstall", pkgName));
                handler.post(() -> commanderCallback.onApkInstalled(apkInfoBean, result.exitCode));
                break;
            case 2:
                if (IceBox.uninstallPackage(context, pkgName)) {
                    handler.post(() -> commanderCallback.onApkInstalled(apkInfoBean, 0));
                } else {
                    handler.post(() -> commanderCallback.onApkInstalled(apkInfoBean, 1));
                }
                break;
        }
    }

}
