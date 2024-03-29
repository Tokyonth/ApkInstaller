package com.tokyonth.installer.activity

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.tokyonth.installer.R
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import com.tokyonth.installer.utils.PermissionHelper

abstract class BaseActivity : AppCompatActivity() {

    abstract fun setBinding(): ViewBinding?

    abstract fun initView()

    abstract fun initData()

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

        permissionHelper = PermissionHelper(this)
        setBinding()?.let {
            setContentView(it.root)
        }
        initView()
        checkPermission()
    }

    private fun checkPermission() {
        if (!permissionHelper!!.check()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.label_notice)
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
