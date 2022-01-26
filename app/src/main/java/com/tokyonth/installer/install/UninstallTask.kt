package com.tokyonth.installer.install

import android.os.Handler
import android.os.Looper
import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.utils.ShellUtils

class UninstallTask(
    private val pkgName: String,
    private val uninstallCallback: UninstallCallback
) : Thread() {

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun run() {
        super.run()
        when (LocalDataRepo.instance.getInstallMode()) {
            0 -> {
                ShellUtils.execWithRoot(Constants.UNINSTALL_COMMAND + pkgName).let {
                    handler.post { uninstallCallback.onUninstallResult(it) }
                }
            }
            1 -> {
                ShellUtils.execWithRoot(Constants.UNINSTALL_COMMAND + pkgName).let {
                    handler.post { uninstallCallback.onUninstallResult(it) }
                }
            }
            2 -> {
                IceBox.uninstallPackage(App.context, pkgName).let {
                    val result = if (it) {
                        0
                    } else {
                        -1
                    }
                    handler.post { uninstallCallback.onUninstallResult(result) }
                }
            }
        }
    }

}
