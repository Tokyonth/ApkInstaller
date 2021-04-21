package com.tokyonth.installer.install

import android.content.Context
import android.os.Handler
import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.Constants.UNFREEZE_COMMAND
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.utils.shizuku.Shell
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.utils.shizuku.ShizukuShell

class UnInstallTask(private val mode: Int, private val pkgName: String, private val context: Context,
                    private val handler: Handler, private val commanderCallback: CommanderCallback) : Thread() {

    private val apkInfoBean: ApkInfoBean = ApkInfoBean()

    fun shellMode() {
        ShellUtils.execWithRoot(UNFREEZE_COMMAND + pkgName).let {
            commanderCallback.onApkInstalled(apkInfoBean, it)
        }
    }

    override fun run() {
        super.run()
        when (mode) {
            1 -> {
                ShizukuShell().exec(Shell.Command("pm", "uninstall", pkgName)).let {
                    handler.post { commanderCallback.onApkInstalled(apkInfoBean, it.exitCode) }
                }
            }
            2 -> if (IceBox.uninstallPackage(context, pkgName)) {
                handler.post { commanderCallback.onApkInstalled(apkInfoBean, 0) }
            } else {
                handler.post { commanderCallback.onApkInstalled(apkInfoBean, 1) }
            }
        }
    }

}