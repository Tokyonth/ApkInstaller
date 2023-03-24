package com.tokyonth.installer.data

import android.graphics.Bitmap

class ApkInfoEntity {

    var icon: Bitmap? = null
    var filePath: String = ""
    var appName: String = ""
    var isArm64: Boolean = false
    var isFakePath: Boolean = false
    var versionName: String = ""
    var versionCode: Int = 0
    var packageName: String = ""
    var isHasInstalledApp: Boolean = false
    var installedVersionName: String = ""
    var installedVersionCode: Int = 0

    var activities: MutableList<String>? = null
    var permissions: MutableList<PermissionInfoEntity>? = null

    val version: String
        get() = "$versionName($versionCode)"

    val installedVersion: String
        get() = if (isHasInstalledApp) "$installedVersionName($installedVersionCode)" else " -- "

}
