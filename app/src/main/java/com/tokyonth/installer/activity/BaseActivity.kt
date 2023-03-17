package com.tokyonth.installer.activity

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding

import com.tokyonth.installer.R
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.NotificationUtils
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import com.tokyonth.installer.utils.DialogUtils
import com.tokyonth.installer.utils.PermissionHelper

abstract class BaseActivity : AppCompatActivity() {

    abstract fun setBinding(): ViewBinding?

    abstract fun initView()

    open fun initData() {
        NotificationUtils.checkNotification(this)
    }

    private var permissionHelper: PermissionHelper? = null

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
            DialogUtils.permissionDialog(this) {
                if (it == DialogUtils.NEGATIVE_BUTTON) {
                    finish()
                } else {
                    requestPermission()
                }
            }
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        permissionHelper = PermissionHelper(this) {
            if (it) {
                initData()
            } else {
                toast(string(R.string.no_permissions))
            }
        }
        permissionHelper?.start()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionHelper?.dispose()
    }

}
