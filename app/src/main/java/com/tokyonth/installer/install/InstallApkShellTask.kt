package com.tokyonth.installer.install

import android.os.Build

import com.tokyonth.installer.Constants
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.utils.doAsync
import com.tokyonth.installer.utils.onUI

class InstallApkShellTask constructor(private val apkInfoEntity: ApkInfoEntity,
                                      private val installCallback: InstallCallback) {

    fun start() {
        installCallback.onApkPreInstall()
        if (Build.VERSION.SDK_INT >= 24) {
            ShellUtils.execWithRoot(Constants.SE_LINUX_COMMAND)
        }
        doAsync {
            val retCode = ShellUtils.execWithRoot(Constants.INSTALL_COMMAND + "\"" + apkInfoEntity.filePath!! + "\"" + "\n", object : ShellUtils.Result {
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

}
