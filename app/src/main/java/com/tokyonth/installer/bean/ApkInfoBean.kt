package com.tokyonth.installer.bean

import android.graphics.drawable.Drawable

import java.io.File

class ApkInfoBean {

    var apkFile: File? = null
    var appName: String? = null
    var icon: Drawable? = null
    var versionName: String? = null
    var versionCode: Int = 0
    var packageName: String? = null
    var isHasInstalledApp: Boolean = false
    var installedVersionName: String? = null
    var installedVersionCode: Int = 0
    var isFakePath: Boolean = false

    var permissions: Array<String>? = null
    var activities: List<String>? = null

    val version: String
        get() = "$versionName($versionCode)"

    val installedVersion: String
        get() = if (isHasInstalledApp) "$installedVersionName($installedVersionCode)" else "NO"

    val fileName: String?
        get() = if (apkFile == null) null else apkFile!!.name

    fun hasInstalledApp(): Boolean {
        return isHasInstalledApp
    }

}
