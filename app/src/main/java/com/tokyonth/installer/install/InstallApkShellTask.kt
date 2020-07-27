package com.tokyonth.installer.install

import android.os.Build
import android.os.Handler
import android.util.Log

import com.tokyonth.installer.Constants
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.utils.ShellUtils

class InstallApkShellTask internal constructor(private val handler: Handler,
                                               private val commanderCallback: CommanderCallback,
                                               private val mApkInfo: ApkInfoBean) : Thread() {

    private var retCode = -1

    override fun run() {
        super.run()
        handler.post { commanderCallback.onApkPreInstall(mApkInfo) }
        if (Build.VERSION.SDK_INT >= 24) {
            ShellUtils.execWithRoot(Constants.SE_LINUX_COMMAND)
        }
        retCode = ShellUtils.execWithRoot(Constants.INSTALL_COMMAND + "\"" + mApkInfo.apkFile!!.path + "\"" + "\n", object : ShellUtils.Result {
            override fun onStdout(text: String) {
                handler.post { commanderCallback.onInstallLog(mApkInfo, text) }
            }

            override fun onStderr(text: String) {
                handler.post { commanderCallback.onInstallLog(mApkInfo, text) }
            }

            override fun onCommand(command: String) {

            }

            override fun onFinish(resultCode: Int) {

            }
        })

        if (retCode == 0 && mApkInfo.isFakePath) {
            if (!mApkInfo.apkFile!!.delete()) {
                Log.e("InstallApkTask", "failed to delete！")
            }
        }
        handler.post { commanderCallback.onApkInstalled(mApkInfo, retCode) }
    }

}
