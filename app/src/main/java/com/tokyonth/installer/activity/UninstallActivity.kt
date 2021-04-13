package com.tokyonth.installer.activity

import android.content.DialogInterface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.install.CommanderCallback
import com.tokyonth.installer.install.UnInstallTask
import com.tokyonth.installer.utils.AppPackageUtils
import com.tokyonth.installer.utils.SPUtils.get
import com.tokyonth.installer.view.CustomizeDialog
import java.util.*

class UninstallActivity : BaseActivity(), CommanderCallback {

    private lateinit var unInstallTask: UnInstallTask
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun initView(): ViewBinding? {
        return null
    }

    override fun initData() {
        val intentStr = intent.dataString
        val strStartIndex: Int = intentStr!!.indexOf(":")
        val strEndIndex: Int = intentStr.indexOf("#")
        val pkgName = if (strEndIndex > 0 && strStartIndex > 0) {
            intentStr.substring(intentStr.indexOf(":") + 1, intentStr.lastIndexOf("#"))
        } else {
            Objects.requireNonNull<String>(intent.dataString).replace("package:", "")
        }

        get(Constants.SP_INSTALL_MODE, 0).also {
            unInstallTask = UnInstallTask(it, pkgName, this, handler, this)

            CustomizeDialog.getInstance(this)
                    .setTitle(getString(R.string.text_uninstall, {
                        when (it) {
                            0 -> getString(R.string.text_shell)
                            1 -> getString(R.string.text_shizuku)
                            2 -> getString(R.string.text_icebox)
                            else -> ""
                        }
                    }))
                    .setMessage(getString(R.string.text_confirm_uninstall_app, AppPackageUtils.getAppNameByPackageName(this, pkgName)))
                    .setPositiveButton(R.string.text_uninstall_btn) { _, _ ->
                        if (it == 0) {
                            unInstallTask.shellMode()
                        } else {
                            unInstallTask.start()
                        }
                    }
                    /*.setNeutralButton(getText(R.string.uninstall_dialog_disable)) { _: DialogInterface, _: Int ->
                        val result = ShellUtils.execWithRoot(Constants.FREEZE_COMMAND + pkgName)
                        val appName = GetAppInfoUtils.getApplicationNameByPackageName(this, pkgName)
                        val str =
                        if (result == 0) {
                            SQLiteUtil.addData(this, pkgName)
                        }
                        showToast(str)
                        finish()
                    }
                     */
                    .setNegativeButton(R.string.dialog_btn_cancel) { _: DialogInterface, _: Int -> finish() }
                    .setCancelable(false)
                    .create().show()
        }
    }

    override fun onStartParseApk(uri: Uri) {

    }

    override fun onApkParsed(apkInfo: ApkInfoBean) {

    }

    override fun onApkPreInstall(apkInfo: ApkInfoBean) {

    }

    override fun onApkInstalled(apkInfo: ApkInfoBean, resultCode: Int) {
        showToast(
                if (resultCode == 0)
                    getString(R.string.text_uninstall_complete)
                else
                    getString(R.string.text_uninstall_failure)
        )
        finish()
    }

    override fun onInstallLog(apkInfo: ApkInfoBean, logText: String) {

    }

}