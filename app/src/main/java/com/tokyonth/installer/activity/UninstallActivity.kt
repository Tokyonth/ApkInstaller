package com.tokyonth.installer.activity

import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.R
import com.tokyonth.installer.install.UninstallCallback
import com.tokyonth.installer.install.UninstallTask
import com.tokyonth.installer.utils.PackageUtils
import com.tokyonth.installer.view.CustomizeDialog
import java.util.regex.Matcher
import java.util.regex.Pattern

class UninstallActivity : BaseActivity(), UninstallCallback {

    private lateinit var uninstallTask: UninstallTask
    private lateinit var pkgName: String

    override fun initView(): ViewBinding? {
        return null
    }

    override fun initData() {
        intent.dataString?.let {
            val pattern: Pattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)+")
            val matcher: Matcher = pattern.matcher(it)
            while (matcher.find()) {
                pkgName = matcher.group()
            }
        }

        val appName = PackageUtils.getAppNameByPackageName(this, pkgName)
        //val uninstallMode = get(Constants.SP_INSTALL_MODE, 0)
        uninstallTask = UninstallTask(pkgName, this)

        if (pkgName.isNotEmpty()) {
            val arr = resources.getStringArray(R.array.install_mode_arr)
            CustomizeDialog.getInstance(this)
                    /*.setTitle(getString(R.string.text_uninstall, {
                        when (uninstallMode) {
                            0 -> arr[0]
                            1 -> arr[1]
                            else -> ""
                        }
                    }))*/
                    .setMessage(getString(R.string.text_confirm_uninstall_app, appName))
                    .setPositiveButton(R.string.apk_uninstall) { _, _ ->
                        uninstallTask.start()
                    }
                    .setNegativeButton(R.string.dialog_btn_cancel) { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
        } else {
            showToast(getString(R.string.cannot_found_pkg))
        }
    }

    override fun onUninstallResult(resultCode: Int) {
        showToast(
                if (resultCode == 0)
                    getString(R.string.text_uninstall_complete)
                else
                    getString(R.string.text_uninstall_failure)
        )
        finish()
    }

}
