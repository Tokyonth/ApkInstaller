package com.tokyonth.installer.install

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.system.Os
import android.util.Log
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.PermissionInfoEntity
import com.tokyonth.installer.utils.AppHelper
import com.tokyonth.installer.utils.PackageUtils
import com.tokyonth.installer.utils.path.DocumentFileUriUtils
import com.tokyonth.installer.utils.path.FileProviderPathUtil
import com.tokyonth.installer.utils.path.ParsingContentUtil
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.util.*

class ParseApkTask(
    private var uri: Uri,
    private var referrer: String
) {

    private val context = App.context
    private val packageManager = context.packageManager

    fun startParseApkTask(): ApkInfoEntity {
        val apkInfo = ApkInfoEntity()
        try {
            val apkSourcePath = ParsingContentUtil(referrer).getFile(context, uri).let {
                if (it == null) {
                    FileProviderPathUtil.getFileFromUri(context, uri).path
                } else {
                    it.path
                }
            }
            apkInfo.filePath = apkSourcePath
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            packageManager.getPackageArchiveInfo(apkInfo.filePath!!, 0)?.let {
                it.applicationInfo.sourceDir = apkInfo.filePath!!
                it.applicationInfo.publicSourceDir = apkInfo.filePath!!
                apkInfo.appName = packageManager.getApplicationLabel(it.applicationInfo).toString()
                apkInfo.packageName = it.applicationInfo.packageName
                apkInfo.versionName = it.versionName
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    apkInfo.versionCode = it.versionCode
                } else {
                    apkInfo.versionCode = it.longVersionCode.toInt()
                }
                apkInfo.icon = AppHelper.drawableToBitmap(
                    it.applicationInfo.loadIcon(
                        packageManager
                    )
                )
            }
            packageManager.getPackageArchiveInfo(
                apkInfo.filePath!!,
                PackageManager.GET_ACTIVITIES
            )?.activities?.let {
                apkInfo.activities = it
            }
            packageManager.getPackageArchiveInfo(
                apkInfo.filePath!!,
                PackageManager.GET_PERMISSIONS
            )?.requestedPermissions?.let {
                apkInfo.permissions = getPermissionInfo(it)
            }
            if (PackageUtils.isAppClientAvailable(context, apkInfo.packageName!!)) {
                packageManager.getPackageInfo(apkInfo.packageName!!, 0)?.let {
                    apkInfo.installedVersionName = it.versionName
                    apkInfo.installedVersionCode =
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            it.versionCode
                        } else {
                            it.longVersionCode.toInt()
                        }
                    apkInfo.isHasInstalledApp = true
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return apkInfo
    }

    private fun getPermissionInfo(permission: Array<String>): MutableList<PermissionInfoEntity> {
        val list = mutableListOf<PermissionInfoEntity>()
        for (str in permission) {
            val item = PermissionInfoEntity()
            try {
                item.permissionName = str
                packageManager.getPermissionInfo(str, 0).run {
                    group.let {
                        if (it != null) {
                            item.permissionGroup = it
                        }
                    }
                    loadLabel(packageManager).let {
                        item.permissionLabel = it.toString()
                    }
                    loadDescription(packageManager).let {
                        if (it != null) {
                            item.permissionDesc = it.toString()
                        }
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            list.add(item)
        }
        return list
    }

}
