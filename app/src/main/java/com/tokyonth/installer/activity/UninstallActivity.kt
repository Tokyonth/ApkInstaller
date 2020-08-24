package com.tokyonth.installer.activity

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.install.CommanderCallback
import com.tokyonth.installer.install.UnInstallTask
import com.tokyonth.installer.utils.GetAppInfoUtils
import com.tokyonth.installer.utils.SPUtils
import com.tokyonth.installer.widget.CustomizeDialog
import java.util.*

class UninstallActivity : BaseActivity(), CommanderCallback {

    private lateinit var unInstallTask: UnInstallTask
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun setActivityView(): Int {
        return 0
    }

    override fun initActivity(savedInstanceState: Bundle?) {
        val intentStr = intent.dataString
        val strStartIndex: Int = intentStr!!.indexOf(":")
        val strEndIndex: Int = intentStr.indexOf("#")
        val pkgName = if (strEndIndex > 0 && strStartIndex > 0) {
            intentStr.substring(intentStr.indexOf(":") + 1, intentStr.lastIndexOf("#"))
        } else {
            Objects.requireNonNull<String>(intent.dataString).replace("package:", "")
        }

        val unMode = SPUtils.getData(Constants.SP_INSTALL_MODE, 0) as Int
        unInstallTask = UnInstallTask(unMode, pkgName, this, handler,this)
        var mode = ""
        when (unMode) {
            0 -> mode = getString(R.string.text_shell)
            1 -> mode = getString(R.string.text_shizuku)
            2 -> mode = getString(R.string.text_icebox)
        }

        CustomizeDialog.getInstance(this)
                .setTitle(getString(R.string.text_uninstall, mode))
                .setMessage(getString(R.string.text_confirm_uninstall_app, GetAppInfoUtils.getApplicationNameByPackageName(this, pkgName)))
                .setPositiveButton(R.string.text_uninstall_btn) { _, _ ->
                    if (unMode == 0) {
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
                .setNegativeButton(R.string.dialog_btn_cancel) { _: DialogInterface, _: Int ->
                    finish()
                }
                .setCancelable(false)
                .create().show()
    }

    override fun onStartParseApk(uri: Uri) {

    }

    override fun onApkParsed(apkInfo: ApkInfoBean) {

    }

    override fun onApkPreInstall(apkInfo: ApkInfoBean) {

    }

    override fun onApkInstalled(apkInfo: ApkInfoBean, resultCode: Int) {
        val str = if (resultCode == 0)
            getString(R.string.text_uninstall_complete) else getString(R.string.text_uninstall_failure)
        showToast(str)
        finish()
    }

    override fun onInstallLog(apkInfo: ApkInfoBean, logText: String) {

    }

}