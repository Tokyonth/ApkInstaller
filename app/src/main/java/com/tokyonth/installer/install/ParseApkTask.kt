package com.tokyonth.installer.install

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.system.Os
import com.tokyonth.installer.App
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.PermissionInfoEntity
import com.tokyonth.installer.utils.PackageUtils
import com.tokyonth.installer.utils.drawable2Bitmap
import com.tokyonth.installer.utils.path.UriPath
import com.tokyonth.installer.utils.path.copyFromStreamUri
import java.io.File
import java.util.zip.ZipFile

class ParseApkTask(
    private var uri: Uri,
    private var referrer: String,
    private var parse: (ApkInfoEntity?) -> Unit
) {

    private val context = App.context

    private val packageManager = context.packageManager

    fun startParseApkTask() {
        packageManager.packageInstaller
        getUriPathByFd {
            if (File(it).exists()) {
                parseApkFile(it, false)
            } else {
                UriPath.getFile(App.context, referrer,  uri).let { it1 ->
                    val name = uri.authority + ".apk"
                    if (it1 == null) {
                        val path = uri.copyFromStreamUri(name).path
                        parseApkFile(path, true)
                    } else {
                        parseApkFile(it1.path, false)
                    }
                }
            }
        }
    }

    private fun getUriPathByFd(success: (String) -> Unit) {
        try {
            App.context.contentResolver.openFileDescriptor(uri, "rw")?.use {
                val fdLink = "/proc/self/fd/${it.fd}"
                val path = Os.readlink(fdLink)
                success.invoke(path)
            }
        } catch (e: Exception) {
            success.invoke("")
        }
    }

    private fun parseApkFile(path: String?, isFake: Boolean) {
        if (path == null) {
            parse.invoke(null)
        } else {
            val apkInfo = ApkInfoEntity().apply {
                filePath = path
                isFakePath = isFake
            }
            parseApkAbi(apkInfo)
            parseApkVersion(apkInfo)
            parseApkInstalled(apkInfo)
            parseApkActivity(apkInfo)
            parseApkPermission(apkInfo)
            parse.invoke(apkInfo)
        }
    }

    private fun parseApkAbi(apkInfo: ApkInfoEntity) {
        val zip = ZipFile(apkInfo.filePath)
        val is64Bit = zip.entries().asSequence().filter {
            it.name.startsWith("lib/arm64-v8a")
        }.toMutableList().isNotEmpty()
        apkInfo.isArm64 = is64Bit
    }

    private fun parseApkVersion(apkInfo: ApkInfoEntity) {
        packageManager.getPackageArchiveInfo(apkInfo.filePath, 0)?.let {
            it.applicationInfo.sourceDir = apkInfo.filePath
            it.applicationInfo.publicSourceDir = apkInfo.filePath
            apkInfo.appName = packageManager.getApplicationLabel(it.applicationInfo).toString()
            apkInfo.packageName = it.applicationInfo.packageName
            apkInfo.versionName = it.versionName
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                apkInfo.versionCode = it.versionCode
            } else {
                apkInfo.versionCode = it.longVersionCode.toInt()
            }
            apkInfo.icon = it.applicationInfo.loadIcon(
                packageManager
            ).drawable2Bitmap()
        }
    }

    private fun parseApkInstalled(apkInfo: ApkInfoEntity) {
        if (PackageUtils.isAppClientAvailable(context, apkInfo.packageName)) {
            packageManager.getPackageInfo(apkInfo.packageName, 0)?.let {
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
    }

    private fun parseApkActivity(apkInfo: ApkInfoEntity) {
        packageManager.getPackageArchiveInfo(
            apkInfo.filePath,
            PackageManager.GET_ACTIVITIES
        )?.activities?.let { it ->
            apkInfo.activities = it.map {
                it.name
            }.toMutableList()
        }
    }

    private fun parseApkPermission(apkInfo: ApkInfoEntity) {
        packageManager.getPackageArchiveInfo(
            apkInfo.filePath,
            PackageManager.GET_PERMISSIONS
        )?.requestedPermissions?.let {
            apkInfo.permissions = getPermissionInfo(it)
        }
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
