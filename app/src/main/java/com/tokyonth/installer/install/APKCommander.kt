package com.tokyonth.installer.install

import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.tokyonth.installer.Constants

import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.bean.permissions.PermInfoBean
import com.tokyonth.installer.utils.SPUtils

class APKCommander(private val activity: Activity, uri: Uri,
                   private val callback: CommanderCallback,
                   referrer: String) : ParseApkTask() {

    private val handler: Handler = Handler(Looper.getMainLooper())
    internal var apkInfo: ApkInfoBean? = null
    internal var permInfo: PermInfoBean? = null

    private fun getApkInfo(): ApkInfoBean? {
        return apkInfo
    }

    fun getPermInfo(): PermInfoBean? {
        return permInfo
    }

    override fun setApkInfo(mApkInfo: ApkInfoBean) {
        this.apkInfo = mApkInfo
    }

    override fun setPermInfo(permInfo: PermInfoBean) {
        this.permInfo = permInfo
    }

    init {
        startParseApkTask(uri, activity, handler, callback, referrer)
        start()
    }

    fun startInstall() {
        if (SPUtils.getData(Constants.SP_IS_SHIZUKU_MODE, false) as Boolean) {
            ApkShizukuInstaller(activity, handler, callback, getApkInfo()!!).start()
        } else {
            InstallApkTask(handler, callback, getApkInfo()!!).start()
        }
    }

}
