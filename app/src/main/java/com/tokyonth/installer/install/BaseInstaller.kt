package com.tokyonth.installer.install

import com.tokyonth.installer.data.ApkInfoEntity

abstract class BaseInstaller(
    val apkInfoEntity: ApkInfoEntity,
    val installCallback: InstallCallback
) {

    init {
        installCallback.onApkPreInstall()
    }

    abstract suspend fun install()

}
