package com.tokyonth.installer.activity

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.tokyonth.installer.R
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.NotificationUtils
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import com.tokyonth.installer.utils.PermissionHelper

abstract class BaseActivity : AppCompatActivity() {

    abstract fun setBinding(): ViewBinding?

    abstract fun initView()

    open fun initData() {
        Log.e("打印-->", "检查通知")
        NotificationUtils.checkNotification(this)
    }

    open var permissionHelper: PermissionHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val uiMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        val isNightMode = uiMode == 0x20

        isNightMode.let {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !it
            }
            SPDataManager.instance.setNightMode(it)
        }

        setBinding()?.let {
            setContentView(it.root)
        }
        initView()
        checkPermission()
    }

    private fun checkPermission() {
        if (SPDataManager.instance.isFirstBoot()) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.use_app_warn)
                .setNegativeButton(R.string.exit_app) { _, _ ->
                    finish()
                }
                .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                    requestPermission()
                }
                .setCancelable(false)
                .show()
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        permissionHelper = PermissionHelper(this)
        permissionHelper?.registerCallback { all, _ ->
            if (all) {
                initData()
            } else {
                toast(string(R.string.no_permissions))
            }
        }
        permissionHelper?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionHelper?.dispose()
    }

}
