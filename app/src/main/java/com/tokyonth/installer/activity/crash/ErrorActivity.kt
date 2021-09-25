package com.tokyonth.installer.activity.crash

import android.content.ClipData
import android.content.ClipboardManager
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.BaseActivity
import com.tokyonth.installer.databinding.ActivityCrashErrorBinding
import com.tokyonth.installer.utils.lazyBind

class ErrorActivity : BaseActivity() {

    private val vb: ActivityCrashErrorBinding by lazyBind()

    override fun initView(): ViewBinding {
        return vb
    }

    override fun initData() {
        val crashInfo = ActivityOnCrash.getAllErrorDetailsFromIntent(this, intent)
        vb.tvCrashInfo.text = crashInfo
        vb.btnErrorCopy.setOnClickListener {
            copyErrorToClipboard(crashInfo)
            showToast(getString(R.string.copy_error_log))
        }
        vb.btnErrorExit.setOnClickListener {
            ActivityOnCrash.closeApplication(this)
        }
    }

    private fun copyErrorToClipboard(info: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("error", info)
        clipboard.setPrimaryClip(clip)
    }

}