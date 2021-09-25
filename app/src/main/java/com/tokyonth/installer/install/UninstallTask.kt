package com.tokyonth.installer.install

import android.os.Handler
import android.os.Looper
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants
import com.tokyonth.installer.utils.ShellUtils

class UninstallTask(private val pkgName: String,
                    private val uninstallCallback: UninstallCallback) : Thread() {

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun run() {
        super.run()
        when (App.localData.getInstallMode()) {
            0 -> {
                ShellUtils.execWithRoot(Constants.UNINSTALL_COMMAND + pkgName).let {
                    handler.post { uninstallCallback.onUninstallResult(it) }
                }
            }
            1 -> {
                ShellUtils.execWithRoot(Constants.UNINSTALL_COMMAND + pkgName).let {
                    handler.post { uninstallCallback.onUninstallResult(it) }
                }
                /*ShizukuShell().exec(Shell.Command("pm", "uninstall", pkgName)).let {
                    handler.post { uninstallCallback.onUninstallResult(it.exitCode) }
                }*/
            }
        }
    }

}
