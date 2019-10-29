package com.tokyonth.installer.apk;

import android.net.Uri;

import com.tokyonth.installer.bean.ApkInfo;

public interface ICommanderCallback {

    void onStartParseApk(Uri uri);

    void onApkParsed(ApkInfo apkInfo);

    void onApkPreInstall(ApkInfo apkInfo);

    void onApkInstalled(ApkInfo apkInfo, int resultCode);

    void onInstallLog(ApkInfo apkInfo, String logText);

}
