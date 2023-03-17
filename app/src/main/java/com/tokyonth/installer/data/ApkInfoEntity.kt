package com.tokyonth.installer.data

import android.content.pm.ActivityInfo
import android.graphics.Bitmap

class ApkInfoEntity {

    var icon: Bitmap? = null
    var filePath: String? = null
    var appName: String? = null
    var versionName: String? = null
    var versionCode: Int = 0
    var packageName: String? = null
    var isHasInstalledApp: Boolean = false
    var installedVersionName: String? = null
    var installedVersionCode: Int = 0

    var activities: Array<ActivityInfo>? = null
    var permissions: MutableList<PermissionInfoEntity>? = null

    val version: String
        get() = "$versionName($versionCode)"

    val installedVersion: String
        get() = if (isHasInstalledApp) "$installedVersionName($installedVersionCode)" else " -- "

}
