package com.tokyonth.installer.utils

import android.content.Context
import android.graphics.drawable.Drawable

import com.tokyonth.installer.Constants

object PackageUtils {

    fun isShizukuClientAvailable(context: Context): Boolean {
        return isAppClientAvailable(context, Constants.SHIZUKU_PKG_NAME)
    }

    fun isIceBoxClientAvailable(context: Context): Boolean {
        return isAppClientAvailable(context, Constants.ICEBOX_PKG_NAME)
    }

    fun isAppClientAvailable(context: Context, pkgName: String): Boolean {
        context.packageManager.let {
            for (name in it.getInstalledPackages(0)) {
                if (name.packageName == pkgName) {
                    return true
                }
            }
        }
        return false
    }

    fun getAppNameByPackageName(context: Context, packageName: String?): String {
        return context.packageManager.let {
            it.getApplicationLabel(it.getApplicationInfo(packageName!!, 0)).toString()
        }
    }

    fun getAppIconByPackageName(context: Context, packageName: String?): Drawable {
        return context.packageManager.let {
            it.getApplicationIcon(it.getApplicationInfo(packageName!!, 0))
        }
    }

    fun getVersionNameByPackageName(context: Context, packageName: String?): String {
        return context.packageManager.let {
            val packageInfo = it.getPackageInfo(packageName!!, 0)
            packageInfo.versionName
        }
    }

    fun getVersionName(context: Context): String {
        return context.packageManager.let {
            val packageInfo = it.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        }
    }

}
