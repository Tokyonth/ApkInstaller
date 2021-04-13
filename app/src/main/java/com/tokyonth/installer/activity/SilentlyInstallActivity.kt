package com.tokyonth.installer.activity

import android.net.Uri
import android.util.Log
import androidx.viewbinding.ViewBinding

import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.install.CommanderCallback
import com.tokyonth.installer.utils.SPUtils.get

class SilentlyInstallActivity : BaseActivity(), CommanderCallback {

    private var apkCommander: APKCommander? = null

    override fun initView(): ViewBinding? {
        return null
    }

    override fun initData() {
        intent.data.also {
            if (it != null) {
                val apkSource = intent.getStringExtra(Constants.APK_SOURCE)
                apkCommander = APKCommander(this, this, it, apkSource as String)
            } else {
                showToast(getString(R.string.unable_to_install_apk))
                finish()
            }
        }
    }

    override fun onStartParseApk(uri: Uri) {
        showToast(getString(R.string.parsing))
    }

    override fun onApkParsed(apkInfo: ApkInfoBean) {
        apkCommander!!.startInstall()
    }

    override fun onApkPreInstall(apkInfo: ApkInfoBean) {
        showToast(getString(R.string.start_install, apkInfo.apkFile!!.path))
    }

    override fun onApkInstalled(apkInfo: ApkInfoBean, resultCode: Int) {
        if (resultCode == 0) {
            showToast(getString(R.string.apk_installed, apkInfo.appName))
            if (!apkInfo.isFakePath && get(Constants.SP_AUTO_DELETE, false)) {
                showToast(getString(R.string.apk_deleted, apkInfo.apkFile!!.name))
            }
        } else {
            showToast(getString(R.string.install_failed, apkInfo.appName))
        }
        finish()
    }

    override fun onInstallLog(apkInfo: ApkInfoBean, logText: String) {
        Log.e("SilentlyInstall", logText)
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

}