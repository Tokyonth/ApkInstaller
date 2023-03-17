package com.tokyonth.installer.install

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.tokyonth.installer.R
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.AppHelper
import com.tokyonth.installer.utils.NotificationUtils
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import java.io.File

class InstallerServer : JobIntentService(), InstallCallback {

    companion object {
        private const val JOB_ID = 1

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, InstallerServer::class.java, JOB_ID, work)
        }
    }

    private var apkCommander: APKCommander? = null

    private var apkInfo: ApkInfoEntity? = null

    private var installLog: String = ""

    override fun onHandleWork(intent: Intent) {
        intent.data.let { uri ->
            if (uri == null) {
                toast(string(R.string.unable_install_apk))
            } else {
                if (SPDataManager.instance.isDefaultSilent()) {
                    val apkSource = AppHelper.reflectGetReferrer(applicationContext).toString()
                    apkCommander = APKCommander(uri, apkSource, this)
                    apkCommander?.startParse()
                    return
                }
            }
        }
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (!apkInfo.packageName.isNullOrEmpty()) {
            this.apkInfo = apkInfo
            apkCommander?.startInstall()
        } else {
            toast(string(R.string.unable_install_apk))
        }
    }

    override fun onApkPreInstall() {
        toast(string(R.string.start_install, apkInfo!!.appName))
    }

    override fun onApkInstalled(installStatus: InstallStatus) {
        var notificationSub = apkInfo!!.appName!!
        val status = when (installStatus) {
            InstallStatus.SUCCESS -> {
                if (SPDataManager.instance.isAutoDel()) {
                    File(apkInfo!!.filePath!!).delete()
                    notificationSub = string(R.string.auto_del_notification, notificationSub)
                }
                string(R.string.install_successful)
            }
            InstallStatus.FAILURE -> {
                notificationSub += " ($installLog)"
                string(R.string.install_failed_msg)
            }
        }
        NotificationUtils.sendNotification(
            this,
            status,
            notificationSub,
            apkInfo!!.icon!!
        )
    }

    override fun onInstallLog(installLog: String) {
        this.installLog = installLog
    }

}
