package com.tokyonth.installer.install

import com.tokyonth.installer.data.ApkInfoEntity

object InstallerFactory {

    fun create(
        installMode: Int,
        apkInfoEntity: ApkInfoEntity,
        installCallback: InstallCallback
    ): BaseInstaller {
        return when (installMode) {
            0 -> InstallApkShellTask(apkInfoEntity, installCallback)
            1 -> InstallApkShizukuTask(apkInfoEntity, installCallback)
            2 -> InstallApkIceBoxTask(apkInfoEntity, installCallback)
            else -> throw IllegalArgumentException("not found install mode!")
        }
    }

}
