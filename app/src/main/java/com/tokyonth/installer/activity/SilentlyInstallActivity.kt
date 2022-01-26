package com.tokyonth.installer.activity

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.viewbinding.ViewBinding

import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.install.InstallCallback
import com.tokyonth.installer.install.InstallStatus
import com.tokyonth.installer.utils.AppHelper
import com.tokyonth.installer.utils.NotificationUtils
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import java.io.File

class SilentlyInstallActivity : BaseActivity(), InstallCallback {

    private lateinit var apkCommander: APKCommander
    private lateinit var apkInfoEntity: ApkInfoEntity

    private var installLog = ""

    override fun setBinding(): ViewBinding? = null

    override fun initView() {
        if (LocalDataRepo.instance.isFirstBoot()) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    override fun initData() {
        val apkSource = AppHelper.reflectGetReferrer(this)!!
        if (intent.getBooleanExtra(Constants.IS_FORM_INSTALL_ACT, false)) {
            apkInfoEntity = intent.getParcelableExtra(Constants.APK_INFO)!!
            apkCommander = APKCommander(apkInfoEntity, this)
            apkCommander.startParse()
            return
        }
        intent.data.let { uri ->
            if (uri == null) {
                toast(string(R.string.unable_install_apk))
                finish()
            } else {
                if (LocalDataRepo.instance.isDefaultSilent()) {
                    apkCommander = APKCommander(uri, apkSource, this)
                    apkCommander.startParse()
                    return
                }
                Intent(this, InstallerActivity::class.java).let {
                    it.data = uri
                    it.putExtra(Constants.APK_SOURCE, apkSource)
                    startActivity(it)
                    finish()
                }
            }
        }
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (!apkInfo.packageName.isNullOrEmpty()) {
            apkInfoEntity = apkInfo
            apkCommander.startInstall()
        } else {
            toast(string(R.string.unable_install_apk))
            finish()
        }
    }

    override fun onApkPreInstall() {
        toast(string(R.string.start_install, apkInfoEntity.appName))
    }

    override fun onApkInstalled(installStatus: InstallStatus) {
        var notificationSub = apkInfoEntity.appName!!
        val status = when (installStatus) {
            InstallStatus.SUCCESS -> {
                if (LocalDataRepo.instance.isAutoDel()) {
                    File(apkInfoEntity.filePath!!).delete()
                    notificationSub = (string(R.string.auto_del_notification, notificationSub))
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
            apkInfoEntity.getIcon()!!
        )
        finish()
    }

    override fun onInstallLog(installLog: String) {
        this.installLog = installLog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val attr = window.attributes.apply {
            height = 0
            width = 0
        }
        window.apply {
            attributes = attr
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }
    }

}
