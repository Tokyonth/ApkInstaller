package com.tokyonth.installer.install

import android.os.Build

import com.tokyonth.installer.Constants
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.utils.ktx.doAsync
import com.tokyonth.installer.utils.ktx.onUI

class InstallApkShellTask : MakeInstaller() {

    override fun install() {
        installCallback.onApkPreInstall()
        if (Build.VERSION.SDK_INT >= 24) {
            ShellUtils.execWithRoot(Constants.SE_LINUX_COMMAND)
        }
        doAsync {
            val retCode = ShellUtils.execWithRoot(
                Constants.INSTALL_COMMAND + "\"" + apkInfoEntity.filePath!! + "\"" + "\n",
                object : ShellUtils.Result {
                    override fun onStdout(text: String) {
                        onUI {
                            installCallback.onInstallLog(text)
                        }
                    }

                    override fun onStderr(text: String) {
                        onUI {
                            installCallback.onInstallLog(text)
                        }
                    }

                    override fun onCommand(command: String) {

                    }

                    override fun onFinish(resultCode: Int) {

                    }
                })
            onUI {
                installCallback.onApkInstalled(InstallStatus(retCode))
            }
        }
    }

    override fun unInstall(uninstallCallback: UninstallCallback) {

    }

}
