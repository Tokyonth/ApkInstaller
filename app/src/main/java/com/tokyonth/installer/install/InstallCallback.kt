package com.tokyonth.installer.install

import com.tokyonth.installer.data.ApkInfoEntity

interface InstallCallback {

    fun onApkParsed(apkInfo: ApkInfoEntity)

    fun onApkParsedFailed(msg: String)

    fun onApkPreInstall()

    fun onApkInstalled(isInstalled: Boolean)

    fun onInstallLog(installLog: String)

}
