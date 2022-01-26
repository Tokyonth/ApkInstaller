package com.tokyonth.installer.install

import com.tokyonth.installer.data.ApkInfoEntity

abstract class MakeInstaller {

    protected lateinit var apkInfoEntity: ApkInfoEntity

    protected lateinit var installCallback: InstallCallback

    fun make(installCallback: InstallCallback, apkInfoEntity: ApkInfoEntity) {
        this.apkInfoEntity = apkInfoEntity
        this.installCallback = installCallback
    }

    abstract fun install()

    abstract fun unInstall(uninstallCallback: UninstallCallback)

}
