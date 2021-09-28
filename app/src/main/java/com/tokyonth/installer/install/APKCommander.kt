package com.tokyonth.installer.install

import android.net.Uri
import android.util.Log

import com.tokyonth.installer.App
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.utils.doAsync
import com.tokyonth.installer.utils.onUI

class APKCommander {

    private var installCallback: InstallCallback
    private lateinit var apkInfoEntity: ApkInfoEntity

    private var uri: Uri? = null
    private var referrer: String? = null

    constructor(apkInfoEntity: ApkInfoEntity, installCallback: InstallCallback) {
        this.apkInfoEntity = apkInfoEntity
        this.installCallback = installCallback
    }

    constructor(uri: Uri, referrer: String, installCallback: InstallCallback) {
        this.uri = uri
        this.referrer = referrer
        this.installCallback = installCallback
    }

    fun start() {
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
        when (App.localData.getInstallMode()) {
            0 -> InstallApkShellTask(apkInfoEntity, installCallback).start()

            1 -> InstallApkShizukuTask(apkInfoEntity, installCallback).start()

            2 -> InstallApkIceBoxTask(apkInfoEntity, installCallback).start()
        }
    }

}
