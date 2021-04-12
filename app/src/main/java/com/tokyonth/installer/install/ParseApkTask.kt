package com.tokyonth.installer.install

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.AndroidRuntimeException
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.bean.permissions.PermInfoBean
import com.tokyonth.installer.utils.path.FileProviderPathUtil
import com.tokyonth.installer.utils.path.ParsingContentUtil
import java.io.File
import java.util.*

abstract class ParseApkTask : Thread() {

    var uri: Uri? = null
    private var handler: Handler? = null
    private var activity: Activity? = null
    private var referrer: String? = null
    private var packageManager: PackageManager? = null
    private var commanderCallback: CommanderCallback? = null

    private var permInfo: PermInfoBean? = null
    private var mApkInfo: ApkInfoBean? = null

    fun startParseApkTask(uri: Uri, activity: Activity, handler: Handler, commanderCallback: CommanderCallback, referrer: String) {
        this.uri = uri
        this.handler = handler
        this.activity = activity
        this.referrer = referrer
        this.commanderCallback = commanderCallback
        packageManager = activity.packageManager
    }

    protected abstract fun setApkInfo(mApkInfo: ApkInfoBean)

    protected abstract fun setPermInfo(permInfo: PermInfoBean)

    @Suppress("DEPRECATION")
    override fun run() {
        super.run()
        try {
            handler!!.post { commanderCallback!!.onStartParseApk(uri!!) }
            mApkInfo = ApkInfoBean()
            permInfo = PermInfoBean()

            val queryContent = ParsingContentUtil(referrer).getFile(activity, uri)
            val apkSourcePath = if (queryContent == null) {
                FileProviderPathUtil.getFileFromUri(activity, uri).path
            } else {
                queryContent.path
            }
            mApkInfo!!.apkFile = File(apkSourcePath)

            val pkgInfo = packageManager!!.getPackageArchiveInfo(mApkInfo!!.apkFile!!.path, PackageManager.GET_ACTIVITIES)
            if (pkgInfo != null) {
                pkgInfo.applicationInfo.sourceDir = mApkInfo!!.apkFile!!.path
                pkgInfo.applicationInfo.publicSourceDir = mApkInfo!!.apkFile!!.path
                mApkInfo!!.appName = packageManager!!.getApplicationLabel(pkgInfo.applicationInfo).toString()
                mApkInfo!!.packageName = pkgInfo.applicationInfo.packageName
                mApkInfo!!.versionName = pkgInfo.versionName
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    mApkInfo!!.versionCode = pkgInfo.versionCode
                } else {
                    mApkInfo!!.versionCode = pkgInfo.longVersionCode.toInt()
                }
                mApkInfo!!.icon = pkgInfo.applicationInfo.loadIcon(packageManager)

                val activityList = ArrayList<String>()
                if (pkgInfo.activities != null) {
                    for (activity in pkgInfo.activities) {
                        activityList.add(activity.name)
                    }
                    mApkInfo!!.activities = activityList
                }
                try {
                    val installedPkgInfo = packageManager!!.getPackageInfo(mApkInfo!!.packageName!!, PackageManager.GET_CONFIGURATIONS)
                    mApkInfo!!.installedVersionName = installedPkgInfo.versionName
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        mApkInfo!!.installedVersionCode = installedPkgInfo.versionCode
                    } else {
                        mApkInfo!!.installedVersionCode = installedPkgInfo.longVersionCode.toInt()
                    }
                    mApkInfo!!.isHasInstalledApp = true
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    mApkInfo!!.isHasInstalledApp = false
                }

                val permPkgInfo = packageManager!!.getPackageArchiveInfo(mApkInfo!!.apkFile!!.path, PackageManager.GET_PERMISSIONS)!!
                mApkInfo!!.permissions = permPkgInfo.requestedPermissions
                val strList = ArrayList<String>()
                if (permPkgInfo.requestedPermissions != null) {
                    Collections.addAll(strList, *permPkgInfo.requestedPermissions)
                    getPermissionInfo(strList)
                }
            }
            handler!!.post { commanderCallback!!.onApkParsed(mApkInfo!!) }
            setApkInfo(mApkInfo!!)
        } catch (e: Exception) {
            //handler!!.post { commanderCallback!!.onApkParsed(null!!) }
            e.printStackTrace()
            throw AndroidRuntimeException(e)
        }

    }

    private fun getPermissionInfo(permission: List<String>) {
        val group = ArrayList<String>()
        val label = ArrayList<String>()
        val description = ArrayList<String>()
        for (str in permission) {
            try {
                val permissionInfo = packageManager!!.getPermissionInfo(str, 0)
                group += permissionInfo.group
                //permissionInfo.group?.let { group.add(it) }

                val permissionLabel = permissionInfo.loadLabel(packageManager!!).toString()
                label.add(permissionLabel)

                val permissionDescription = permissionInfo.loadDescription(packageManager!!)
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
        permInfo!!.permissionDescription = description
        permInfo!!.permissionGroup = group
        permInfo!!.permissionLabel = label
        setPermInfo(permInfo!!)
    }

}
