package com.tokyonth.installer.install

import android.content.Context
import android.os.Handler
import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.Constants.UNFREEZE_COMMAND
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.utils.Shell
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.utils.ShizukuShell

class UnInstallTask(private val mode: Int, private val pkgName: String, private val context: Context,
                    private val handler: Handler, private val commanderCallback: CommanderCallback) : Thread() {
    private val apkInfoBean: ApkInfoBean = ApkInfoBean()
    fun shellMode() {
        val resultCode = ShellUtils.execWithRoot(UNFREEZE_COMMAND + pkgName)
        commanderCallback.onApkInstalled(apkInfoBean, resultCode)
    }

    override fun run() {
        super.run()
        when (mode) {
            1 -> {
                val result = ShizukuShell().exec(Shell.Command("pm", "uninstall", pkgName))
                handler.post { commanderCallback.onApkInstalled(apkInfoBean, result.exitCode) }
            }
            2 -> if (IceBox.uninstallPackage(context, pkgName)) {
                handler.post { commanderCallback.onApkInstalled(apkInfoBean, 0) }
            } else {
                handler.post { commanderCallback.onApkInstalled(apkInfoBean, 1) }
            }
        }
    }

}