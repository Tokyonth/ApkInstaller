package com.tokyonth.installer.data

import android.content.Context
import com.tokyonth.installer.BuildConfig
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.SPUtils.get
import com.tokyonth.installer.utils.SPUtils.set

class LocalDataRepo(val context: Context) {

    fun setDefaultSilent(boolean: Boolean) {
        context["defaultSilent"] = boolean
    }

    fun isDefaultSilent(): Boolean {
        return context["defaultSilent", false]
    }

    fun setInstallMode(int: Int) {
        context["installMode"] = int
    }

    fun getInstallMode(): Int {
        return context["installMode", 0]
    }

    fun setSystemPkg(pkgName: String) {
        context["systemPkgName"] = pkgName
    }

    fun getSystemPkg(): String {
        return context["systemPkgName", Constants.DEFAULT_SYS_PKG_NAME]
    }

    fun setUseSystemPkg(boolean: Boolean) {
        context["useSystemPkg"] = boolean
    }

    fun isUseSystemPkg(): Boolean {
        return context["useSystemPkg", false]
    }

    fun setAutoDel(boolean: Boolean) {
        context["autoDelete"] = boolean
    }

    fun isAutoDel(): Boolean {
        return context["autoDelete", false]
    }

    fun setShowPermission(boolean: Boolean) {
        context["showPermission"] = boolean
    }

    fun isShowPermission(): Boolean {
        return context["showPermission", true]
    }

    fun setShowActivity(boolean: Boolean) {
        context["showActivity"] = boolean
    }

    fun isShowActivity(): Boolean {
        return context["showActivity", true]
    }

    fun setNightMode(boolean: Boolean) {
        context["nightMode"] = boolean
    }

    fun isNightMode(): Boolean {
        return context["nightMode", false]
    }

    fun setFollowSystem(boolean: Boolean) {
        context["nightFollowSystem"] = boolean
    }

    fun isFollowSystem(): Boolean {
        return context["nightFollowSystem", false]
    }

    fun setNeverShowTip() {
        context["neverVersionTips"] = true
    }

    fun isNeverShowTip(): Boolean {
        return context["neverVersionTips", false]
    }

    fun setNeverShowUsePkg() {
        context["neverUseSystemPkg"] = true
    }

    fun isNeverShowUsePkg(): Boolean {
        return context["neverUseSystemPkg", false]
    }

    fun setNotFirstBoot() {
        context["isFirstBootTAG"] = BuildConfig.VERSION_NAME
    }

    fun isFirstBoot(): Boolean {
        if (BuildConfig.needRemindUser) {
            return context["isFirstBootTAG", ""] != BuildConfig.VERSION_NAME
        }
        return false
    }

    fun getInstallName(): String {
        val arr = context.resources.getStringArray(R.array.install_mode_arr)
        return arr[getInstallMode()]
    }

}
