package com.tokyonth.installer.data

import com.tokyonth.installer.App
import com.tokyonth.installer.BuildConfig
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.AppHelper
import com.tokyonth.installer.utils.SPUtils.getSP
import com.tokyonth.installer.utils.SPUtils.putSP

class SPDataManager {

    companion object {
        val instance: SPDataManager by lazy {
            SPDataManager()
        }
    }

    fun setDefaultSilent(isDefaultSilent: Boolean) {
        putSP(Constants.SP_SILENT_KEY, isDefaultSilent)
    }

    fun isDefaultSilent(): Boolean {
        return getSP(Constants.SP_SILENT_KEY, false)
    }

    fun setInstallMode(installMode: Int) {
        putSP(Constants.SP_INSTALL_MODE_KEY, installMode)
    }

    fun getInstallMode(): Int {
        return getSP(Constants.SP_INSTALL_MODE_KEY, 0)
    }

    fun setSystemPkg(pkgName: String) {
        putSP(Constants.SP_CUSTOM_PKG_KEY, pkgName)
    }

    fun getSystemPkg(): String {
        val pkgName = if (AppHelper.isMiuiOS()) {
            Constants.MIUI_SYS_PKG_NAME
        } else {
            Constants.DEFAULT_SYS_PKG_NAME
        }
        return getSP(Constants.SP_CUSTOM_PKG_KEY, pkgName)
    }

    fun setUseSystemPkg(isUse: Boolean) {
        putSP(Constants.SP_IS_SYSTEM_PKG_KEY, isUse)
    }

    fun isUseSystemPkg(): Boolean {
        return getSP(Constants.SP_IS_SYSTEM_PKG_KEY, false)
    }

    fun setAutoDel(isAutoDel: Boolean) {
        putSP(Constants.SP_AUTO_DELETE_KEY, isAutoDel)
    }

    fun isAutoDel(): Boolean {
        return getSP(Constants.SP_AUTO_DELETE_KEY, false)
    }

    fun setShowPermission(isShow: Boolean) {
        putSP(Constants.SP_SHOW_PERMISSION_KEY, isShow)
    }

    fun isShowPermission(): Boolean {
        return getSP(Constants.SP_SHOW_PERMISSION_KEY, true)
    }

    fun setShowActivity(isShow: Boolean) {
        putSP(Constants.SP_SHOW_ACTIVITY_KEY, isShow)
    }

    fun isShowActivity(): Boolean {
        return getSP(Constants.SP_SHOW_ACTIVITY_KEY, true)
    }

    fun setNightMode(isNightMode: Boolean) {
        putSP(Constants.SP_NIGHT_MODE_KEY, isNightMode)
    }

    fun isNightMode(): Boolean {
        return getSP(Constants.SP_NIGHT_MODE_KEY, false)
    }

    fun setFollowSystem(isFollowSys: Boolean) {
        putSP(Constants.SP_FOLLOW_SYSTEM_NIGHT_KEY, isFollowSys)
    }

    fun isFollowSystem(): Boolean {
        return getSP(Constants.SP_FOLLOW_SYSTEM_NIGHT_KEY, false)
    }

    fun setNeverShowTip() {
        putSP(Constants.SP_NEVER_VERSION_TIP_KEY, true)
    }

    fun isNeverShowTip(): Boolean {
        return getSP(Constants.SP_NEVER_VERSION_TIP_KEY, false)
    }

    fun setNeverShowUsePkg() {
        putSP(Constants.SP_NEVER_SYSTEM_PKG_KEY, true)
    }

    fun isNeverShowUsePkg(): Boolean {
        return getSP(Constants.SP_NEVER_SYSTEM_PKG_KEY, false)
    }

    fun getInstallName(): String {
        val arr = App.context.resources.getStringArray(R.array.install_mode_arr)
        return arr[getInstallMode()]
    }

}
