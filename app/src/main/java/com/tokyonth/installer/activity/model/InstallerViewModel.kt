package com.tokyonth.installer.activity.model

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.install.InstallCallback
import com.tokyonth.installer.install.InstallStatus

class InstallerViewModel : ViewModel(), InstallCallback {

    val apkParsedLiveData = MutableLiveData<ApkInfoEntity>()

    val apkPreInstallLiveData = MutableLiveData<Boolean>()

    val apkInstalledLiveData = MutableLiveData<InstallStatus>()

    val apkInstallLogLiveData = MutableLiveData<String>()

    private var apkCommander: APKCommander? = null

    fun startParse(uri: Uri, source: String) {
        apkCommander = APKCommander(uri, source, this)
        apkCommander?.startParse()
    }

    fun startInstall() {
        apkCommander?.startInstall()
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        apkParsedLiveData.value = apkInfo
    }

    override fun onApkPreInstall() {
        apkPreInstallLiveData.value = true
    }

    override fun onApkInstalled(installStatus: InstallStatus) {
        apkInstalledLiveData.value = installStatus
    }

    override fun onInstallLog(installLog: String) {
        apkInstallLogLiveData.value = installLog
    }

}
