package com.tokyonth.installer.install

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.utils.CommonUtils
import com.tokyonth.installer.utils.path.DocumentFileUriUtils
import com.tokyonth.installer.utils.path.FileProviderPathUtil
import com.tokyonth.installer.utils.path.ParsingContentUtil
import java.util.*

class ParseApkTask(private var uri: Uri, private var referrer: String) {

    private val context = App.context
    private val packageManager = context.packageManager

    fun startParseApkTask(): ApkInfoEntity {
        val apkInfo = ApkInfoEntity()
        try {
            var apkSourcePath = ParsingContentUtil(referrer).getFile(context, uri).let {
                if (it == null) {
                    FileProviderPathUtil.getFileFromUri(context, uri).path
                } else {
                    it.path
                }
            }

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R && apkSourcePath.contains(Constants.ANDROID_DATA_STR)) {
                DocumentFileUriUtils.getDocumentFile(context, apkSourcePath).run {
                    apkSourcePath = FileProviderPathUtil.getPathFromInputStreamUri(context, uri, name)
                }
            }
            apkInfo.filePath = apkSourcePath

            packageManager.getPackageArchiveInfo(apkInfo.filePath!!, PackageManager.GET_ACTIVITIES)?.let {
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
                apkInfo.activities = it.activities.asList()
                apkInfo.setIcon(CommonUtils.drawableToBitmap(it.applicationInfo.loadIcon(packageManager)))
            }

            apkInfo.isHasInstalledApp = try {
                packageManager.getPackageInfo(apkInfo.packageName!!, PackageManager.GET_CONFIGURATIONS).let {
                    apkInfo.installedVersionName = it.versionName
                    apkInfo.installedVersionCode = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        it.versionCode
                    } else {
                        it.longVersionCode.toInt()
                    }
                }
                true
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                false
            }

            packageManager.getPackageArchiveInfo(apkInfo.filePath!!, PackageManager.GET_PERMISSIONS)?.let {
                apkInfo.permissions = it.requestedPermissions
                apkInfo.permissionsDescribe = getPermissionInfo(it.requestedPermissions.asList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //throw AndroidRuntimeException(e)
        }
        return apkInfo
    }

    private fun getPermissionInfo(permission: List<String>): Triple<ArrayList<String>, ArrayList<String>, ArrayList<String>> {
        val group = ArrayList<String>()
        val label = ArrayList<String>()
        val description = ArrayList<String>()
        for (str in permission) {
            try {
                val permissionInfo = packageManager.getPermissionInfo(str, 0)
                permissionInfo.group.let {
                    if (it != null) {
                        group.add(it)
                    } else {
                        group.add("")
                    }
                }

                val permissionLabel = permissionInfo.loadLabel(packageManager).toString()
                label.add(permissionLabel)

                val permissionDescription = permissionInfo.loadDescription(packageManager)
                if (permissionDescription == null) {
                    description.add("")
                } else {
                    description.add(permissionDescription as String)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                description.add("")
                label.add("")
                group.add("")
            }
        }
        return Triple(group, label, description)
    }

}
