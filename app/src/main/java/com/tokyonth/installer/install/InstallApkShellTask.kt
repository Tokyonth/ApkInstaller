package com.tokyonth.installer.install

import android.os.Build

import com.tokyonth.installer.Constants
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.utils.ShellUtils

class InstallApkShellTask(
    apkInfoEntity: ApkInfoEntity,
    installCallback: InstallCallback
) : BaseInstaller(apkInfoEntity, installCallback) {

    override suspend fun install() {
        if (Build.VERSION.SDK_INT >= 24) {
            ShellUtils.execWithRoot(Constants.SE_LINUX_COMMAND)
        }
        val retCode = ShellUtils.execWithRoot(
            Constants.INSTALL_COMMAND + "\"" + apkInfoEntity.filePath + "\"" + "\n",
            object : ShellUtils.Result {
                override fun onStdout(text: String) {
                    installCallback.onInstallLog(text)
                }

                override fun onStderr(text: String) {
                    installCallback.onInstallLog(text)
                }

                override fun onCommand(command: String) {

                }

                override fun onFinish(resultCode: Int) {

                }
            })
        installCallback.onApkInstalled(retCode == 0)
    }

}
