package com.tokyonth.installer.install

import android.app.Activity
import android.net.Uri
import android.os.Handler
import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.R
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.utils.HelperTools
import com.tokyonth.installer.utils.ToastUtil

class InstallApkIceBoxTask(private val uri: Uri, private val activity: Activity,
                           private val handler: Handler,
                           private val commanderCallback: CommanderCallback,
                           private val mApkInfo: ApkInfoBean) : Thread() {

    private var retCode : Int = -1

    override fun run() {
        super.run()
        handler.post { commanderCallback.onApkPreInstall(mApkInfo) }
        val state = IceBox.querySupportSilentInstall(activity)
        handler.post { commanderCallback.onInstallLog(mApkInfo, state.toString() + "\n")}
        if (HelperTools.requestPermissionByIcebox(activity)) {
            handler.post { commanderCallback.onInstallLog(mApkInfo, "IceBox installation mode")}
            retCode = if (IceBox.installPackage(activity, uri)) {
                0
            } else {
                1
            }
        } else {
            ToastUtil.showToast(activity, activity.getString(R.string.no_permissions), ToastUtil.DEFAULT_SITE)
        }
        handler.post { commanderCallback.onApkInstalled(mApkInfo, retCode) }
    }

}