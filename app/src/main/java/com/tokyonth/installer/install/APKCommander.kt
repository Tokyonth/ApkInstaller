package com.tokyonth.installer.install

import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.tokyonth.installer.Constants

import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.bean.permissions.PermInfoBean
import com.tokyonth.installer.utils.SPUtils.get

class APKCommander(private val activity: Activity, private val callback: CommanderCallback,
                   uri: Uri, referrer: String) : ParseApkTask() {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var apkInfo: ApkInfoBean
    private lateinit var permInfo: PermInfoBean

    fun getApkInfo(): ApkInfoBean {
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
        when (activity[Constants.SP_INSTALL_MODE, 0]) {
            0 -> {
                InstallApkShellTask(handler, callback, getApkInfo()).start()
            }
            1 -> {
                InstallApkShizukuTask(activity, handler, callback, getApkInfo()).start()
            }
            2 -> {
                InstallApkIceBoxTask(uri, activity, handler, callback, getApkInfo()).start()
            }
        }
    }

}
