package com.tokyonth.installer.install

import android.net.Uri

import com.tokyonth.installer.bean.ApkInfoBean

interface CommanderCallback {

    fun onStartParseApk(uri: Uri)

    fun onApkParsed(apkInfo: ApkInfoBean)

    fun onApkPreInstall(apkInfo: ApkInfoBean)

    fun onApkInstalled(apkInfo: ApkInfoBean, resultCode: Int)

    fun onInstallLog(apkInfo: ApkInfoBean, logText: String)

}
