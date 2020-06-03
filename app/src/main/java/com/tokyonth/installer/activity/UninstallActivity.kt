package com.tokyonth.installer.activity

import android.os.Bundle

import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.database.SQLiteUtil
import com.tokyonth.installer.widget.CustomizeDialog
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.utils.GetAppInfoUtils

import java.util.Objects

class UninstallActivity : BaseActivity() {

    override fun setActivityView(): Int {
        return 0
    }

    override fun initActivity(savedInstanceState: Bundle?) {
        val pkgName = Objects.requireNonNull<String>(intent.dataString).replace("package:", "")
        CustomizeDialog.getInstance(this)
                .setTitle(R.string.text_uninstall)
                .setMessage(getString(R.string.text_confirm_uninstall_app, GetAppInfoUtils.getApplicationNameByPackageName(this, pkgName)))
                .setPositiveButton(R.string.text_uninstall) { _, _ ->
                    val result = ShellUtils.execWithRoot(Constants.UNINSTALL_COMMAND + pkgName)
                    val str = if (result == 0) getString(R.string.text_uninstall_complete) else getString(R.string.text_uninstall_failure)
                    showToast(str)
                    finish()
                }
                .setNeutralButton(getText(R.string.uninstall_dialog_disable)) { _, _ ->
                    val result = ShellUtils.execWithRoot(Constants.FREEZE_COMMAND + pkgName)
                    val appName = GetAppInfoUtils.getApplicationNameByPackageName(this, pkgName)
                    val str = if (result == 0) getString(R.string.freeze_app_name, appName) else getString(R.string.freeze_failure)
                    if (result == 0) {
                        SQLiteUtil.addData(this, pkgName)
                    }
                    showToast(str)
                    finish()
                }
                .setNegativeButton(R.string.dialog_btn_cancel) { _, _ -> finish() }
                .create().show()
    }

}