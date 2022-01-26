package com.tokyonth.installer.install

import android.net.Uri
import android.util.Log

import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.utils.ktx.doAsync
import com.tokyonth.installer.utils.ktx.onUI

class APKCommander {

    private lateinit var apkInfoEntity: ApkInfoEntity

    private var uri: Uri? = null
    private var referrer: String? = null
    private var installCallback: InstallCallback

    constructor(apkInfoEntity: ApkInfoEntity, installCallback: InstallCallback) {
        this.apkInfoEntity = apkInfoEntity
        this.installCallback = installCallback
    }

    constructor(uri: Uri, referrer: String, installCallback: InstallCallback) {
        this.uri = uri
        this.referrer = referrer
        this.installCallback = installCallback
    }

    fun startParse() {
        if (this::apkInfoEntity.isInitialized) {
            installCallback.onApkParsed(apkInfoEntity)
            return
        }
        ParseApkTask(uri!!, referrer!!).let {
            doAsync({
                Log.e("ParseApkError->", it.message!!)
            }, {
                apkInfoEntity = it.startParseApkTask()
                onUI {
                    installCallback.onApkParsed(apkInfoEntity)
                }
            })
        }
    }

    fun startInstall() {
        InstallerFactory.create(LocalDataRepo.instance.getInstallMode()).apply {
            make(installCallback, apkInfoEntity)
            install()
        }
    }

}
