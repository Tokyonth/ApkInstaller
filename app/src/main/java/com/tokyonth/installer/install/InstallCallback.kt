package com.tokyonth.installer.install

import com.tokyonth.installer.data.ApkInfoEntity

interface InstallCallback {

    fun onApkParsed(apkInfo: ApkInfoEntity)

    fun onApkPreInstall()

    fun onApkInstalled(installStatus: InstallStatus)

    fun onInstallLog(installLog: String)

}
