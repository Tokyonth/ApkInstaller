package com.tokyonth.installer.activity

import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.R
import com.tokyonth.installer.install.UninstallCallback
import com.tokyonth.installer.install.UninstallTask
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import com.tokyonth.installer.utils.DialogUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

class UninstallActivity : BaseActivity(), UninstallCallback {

    private lateinit var pkgName: String

    override fun setBinding(): ViewBinding? = null

    override fun initView() {

    }

    override fun initData() {
        intent.dataString?.let {
            val pattern: Pattern =
                Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)+")
            val matcher: Matcher = pattern.matcher(it)
            while (matcher.find()) {
                pkgName = matcher.group()
            }
        }

        if (pkgName.isNotEmpty()) {
            DialogUtils.unInstallDialog(this, pkgName) {
                if (it == DialogUtils.POSITIVE_BUTTON) {
                    UninstallTask(pkgName, this).start()
                } else {
                    finish()
                }
            }
        } else {
            toast(string(R.string.cannot_found_pkg))
        }
    }

    override fun onUninstallResult(resultCode: Int) {
        toast(
            if (resultCode == 0)
                string(R.string.text_uninstall_complete)
            else
                string(R.string.text_uninstall_failure)
        )
        finish()
    }

}
