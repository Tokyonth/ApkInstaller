package com.tokyonth.installer.install

import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tokyonth.installer.Constants

import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.bean.permissions.PermInfoBean
import com.tokyonth.installer.utils.SPUtils

class APKCommander(private val activity: Activity, uri: Uri,
                   private val callback: CommanderCallback, referrer: String) : ParseApkTask() {

    private val handler: Handler = Handler(Looper.getMainLooper())
    internal lateinit var apkInfo: ApkInfoBean
    internal lateinit var permInfo: PermInfoBean

    private fun getApkInfo(): ApkInfoBean {
        return apkInfo
    }

    fun getPermInfo(): PermInfoBean {
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
        when (SPUtils.getData(Constants.SP_INSTALL_MODE_KEY, 0) as Int) {
            0 -> {
                InstallApkShellTask(handler, callback, getApkInfo()).start()
            }
            1 -> {
                InstallApkShizukuTask(activity, handler, callback, getApkInfo()).start()
            }
            2 -> {
                uri?.let { InstallApkIceBoxTask(it, activity, handler, callback, getApkInfo()).start() }
            }
        }
    }

}
