package com.tokyonth.installer.activity.model

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.install.InstallCallback

class InstallerViewModel : ViewModel(), InstallCallback {

    val apkParsedLiveData = MutableLiveData<ApkInfoEntity>()

    val apkParsedFailedLiveData = MutableLiveData<String>()

    val apkPreInstallLiveData = MutableLiveData<Boolean>()

    val apkInstalledLiveData = MutableLiveData<Boolean>()

    val apkInstallLogLiveData = MutableLiveData<String>()

    private var apkCommander: APKCommander? = null

    fun startParse(uri: Uri,referrer:String) {
        apkCommander = APKCommander(uri, referrer,this)
        apkCommander?.startParse()
    }

    fun startInstall(apkInfo: ApkInfoEntity) {
        apkCommander?.startInstall(apkInfo)
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        apkParsedLiveData.postValue(apkInfo)
    }

    override fun onApkParsedFailed(msg: String) {
        apkParsedFailedLiveData.postValue(msg)
    }

    override fun onApkPreInstall() {
        apkPreInstallLiveData.postValue(true)
    }

    override fun onApkInstalled(isInstalled: Boolean) {
        apkInstalledLiveData.postValue(isInstalled)
    }

    override fun onInstallLog(installLog: String) {
        apkInstallLogLiveData.postValue(installLog)
    }

}
