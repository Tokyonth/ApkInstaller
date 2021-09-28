package com.tokyonth.installer.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
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

    /**
     * 获取应用程序名称
     */
    fun getAppName(context: Context): String {
        return context.packageManager.let {
            val packageInfo = it.getPackageInfo(context.packageName, 0)
            context.resources.getString(packageInfo.applicationInfo.labelRes)
        }
    }

    /**
     * 获取应用程序版本名称信息
     */
    fun getVersionName(context: Context): String {
        return context.packageManager.let {
            val packageInfo = it.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        }
    }

    /**
     * 获取应用程序版本名称信息
     */
    fun getVersionCode(context: Context): Int {
        return context.packageManager.getPackageInfo(context.packageName, 0).let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                it.versionCode
            } else {
                it.longVersionCode.toInt()
            }
        }
    }

    /**
     * 获取应用程序版本名称信息
     */
    fun getPackageName(context: Context): String {
        return context.packageManager.let {
            val packageInfo = it.getPackageInfo(context.packageName, 0)
            packageInfo.packageName
        }
    }

    /**
     * 获取图标 bitmap
     */
    fun getBitmap(context: Context): Bitmap {
        return context.applicationContext.packageManager.let {
            val applicationInfo = it.getApplicationInfo(context.packageName, 0)
            it!!.getApplicationIcon(applicationInfo).run {
                this as BitmapDrawable
                this.bitmap
            }
        }
    }

}
