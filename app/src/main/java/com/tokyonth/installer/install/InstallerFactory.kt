package com.tokyonth.installer.install

object InstallerFactory {

    fun create(installMode: Int): MakeInstaller {
        return when (installMode) {
            0 -> InstallApkShellTask()
            1 -> InstallApkShizukuTask()
            2 -> InstallApkIceBoxTask()
            else -> throw IllegalArgumentException("not found install mode!")
        }
    }

}
