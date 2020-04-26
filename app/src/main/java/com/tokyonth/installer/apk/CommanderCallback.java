package com.tokyonth.installer.apk;

import android.net.Uri;

import com.tokyonth.installer.bean.ApkInfoBean;

public interface CommanderCallback {

    void onStartParseApk(Uri uri);

    void onApkParsed(ApkInfoBean apkInfo);

    void onApkPreInstall(ApkInfoBean apkInfo);

    void onApkInstalled(ApkInfoBean apkInfo, int resultCode);

    void onInstallLog(ApkInfoBean apkInfo, String logText);

}
