package com.tokyonth.installer.install

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.tokyonth.installer.R
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.NotificationUtils
import com.tokyonth.installer.utils.ktx.string
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
        //toast(string(R.string.unable_install_apk))
        intent.data.let { uri ->
            if (uri == null) {
                sendNotification(string(R.string.unable_install_apk))
            } else {
                apkCommander = APKCommander(uri, "", this)
                apkCommander?.startParse()
            }
        }
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (apkInfo.packageName.isNotEmpty()) {
            this.apkInfo = apkInfo
            apkCommander?.startInstall(apkInfo)
        } else {
            sendNotification(string(R.string.unable_install_apk))
        }
    }

    override fun onApkParsedFailed(msg: String) {
        sendNotification(string(R.string.unable_install_apk))
    }

    override fun onApkPreInstall() {
        sendNotification(string(R.string.start_install, apkInfo!!.appName))
    }

    override fun onApkInstalled(isInstalled: Boolean) {
        var notificationSub = apkInfo!!.appName
        val status = if (isInstalled) {
            if (SPDataManager.instance.isAutoDel()) {
                File(apkInfo!!.filePath).delete()
                notificationSub = string(R.string.auto_del_notification, notificationSub)
            }
            string(R.string.install_successful)
        } else {
            notificationSub += " ($installLog)"
            string(R.string.install_failed_msg)
        }
        NotificationUtils.sendNotification(
            this,
            status,
            notificationSub,
            apkInfo!!.icon!!
        )
    }

    private fun sendNotification(msg: String) {
        NotificationUtils.sendNotification(
            this,
            "安装中...",
            msg,
            apkInfo!!.icon!!
        )
    }

    override fun onInstallLog(installLog: String) {
        this.installLog = installLog
    }

}
