package com.tokyonth.installer.activity.crash

import android.content.ClipData
import android.content.ClipboardManager

import com.tokyonth.installer.R
import com.tokyonth.installer.activity.BaseActivity
import com.tokyonth.installer.databinding.ActivityCrashErrorBinding
import com.tokyonth.installer.utils.ktx.lazyBind
import com.tokyonth.installer.utils.ktx.toast

class ErrorActivity : BaseActivity() {

    private val binding: ActivityCrashErrorBinding by lazyBind()

    override fun setBinding() = binding

    override fun initView() {
        val crashInfo = ActivityOnCrash.getErrorDetails(this, intent)
        binding.run {
            tvCrashInfo.text = crashInfo
            btnErrorCopy.setOnClickListener {
                copyErrorToClipboard(crashInfo)
                toast(getString(R.string.copy_error_log))
            }
            btnErrorExit.setOnClickListener {
                ActivityOnCrash.closeApplication(this@ErrorActivity)
            }
        }
    }

    override fun initData() {

    }

    private fun copyErrorToClipboard(info: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("apkInstallerError", info)
        clipboard.setPrimaryClip(clip)
    }

}
