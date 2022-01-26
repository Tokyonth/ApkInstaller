package com.tokyonth.installer.data

import com.tokyonth.installer.App
import com.tokyonth.installer.BuildConfig
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.AppHelper
import com.tokyonth.installer.utils.SPUtils.getSP
import com.tokyonth.installer.utils.SPUtils.putSP

class LocalDataRepo {

    companion object {

        val instance: LocalDataRepo by lazy {
            LocalDataRepo()
        }

    }

    fun setDefaultSilent(isDefaultSilent: Boolean) {
        putSP("defaultSilent", isDefaultSilent)
    }

    fun isDefaultSilent(): Boolean {
        return getSP("defaultSilent", false)
    }

    fun setInstallMode(installMode: Int) {
        putSP("installMode", installMode)
    }

    fun getInstallMode(): Int {
        return getSP("installMode", 0)
    }

    fun setSystemPkg(pkgName: String) {
        putSP("systemPkgName", pkgName)
    }

    fun getSystemPkg(): String {
        val pkgName = if (AppHelper.isMiuiOS()) {
            Constants.MIUI_SYS_PKG_NAME
        } else {
            Constants.DEFAULT_SYS_PKG_NAME
        }
        return getSP("systemPkgName", pkgName)
    }

    fun setUseSystemPkg(isUse: Boolean) {
        putSP("useSystemPkg", isUse)
    }

    fun isUseSystemPkg(): Boolean {
        return getSP("useSystemPkg", false)
    }

    fun setAutoDel(isAutoDel: Boolean) {
        putSP("autoDelete", isAutoDel)
    }

    fun isAutoDel(): Boolean {
        return getSP("autoDelete", false)
    }

    fun setShowPermission(isShow: Boolean) {
        putSP("showPermission", isShow)
    }

    fun isShowPermission(): Boolean {
        return getSP("showPermission", true)
    }

    fun setShowActivity(isShow: Boolean) {
        putSP("showActivity", isShow)
    }

    fun isShowActivity(): Boolean {
        return getSP("showActivity", true)
    }

    fun setNightMode(isNightMode: Boolean) {
        putSP("nightMode", isNightMode)
    }

    fun isNightMode(): Boolean {
        return getSP("nightMode", false)
    }

    fun setFollowSystem(isFollowSys: Boolean) {
        putSP("nightFollowSystem", isFollowSys)
    }

    fun isFollowSystem(): Boolean {
        return getSP("nightFollowSystem", false)
    }

    fun setNeverShowTip() {
        putSP("neverVersionTips", true)
    }

    fun isNeverShowTip(): Boolean {
        return getSP("neverVersionTips", false)
    }

    fun setNeverShowUsePkg() {
        putSP("neverUseSystemPkg", true)
    }

    fun isNeverShowUsePkg(): Boolean {
        return getSP("neverUseSystemPkg", false)
    }

    fun setNotFirstBoot() {
        putSP("isFirstBootTAG", BuildConfig.VERSION_NAME)
    }

    fun isFirstBoot(): Boolean {
        if (BuildConfig.needRemindUser) {
            return getSP("isFirstBootTAG", "") != BuildConfig.VERSION_NAME
        }
        return false
    }

    fun getInstallName(): String {
        val arr = App.context.resources.getStringArray(R.array.install_mode_arr)
        return arr[getInstallMode()]
    }

}
