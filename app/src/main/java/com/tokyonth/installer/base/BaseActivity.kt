package com.tokyonth.installer.base

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.SPUtils.set
import com.tokyonth.installer.utils.ToastUtil
import com.tokyonth.installer.view.CustomizeDialog

abstract class BaseActivity : AppCompatActivity() {

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val permissionsRequestCode = 100

    abstract fun initView(): ViewBinding

    abstract fun hasView(): Boolean

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        if (hasView()) {
            setContentView(initView().root)
        }
        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                CustomizeDialog.getInstance(this)
                        .setTitle(getString(R.string.dialog_title_tips))
                        .setMessage(getString(R.string.app_get_perm_tips))
                        .setNegativeButton(getString(R.string.exit_app)) { _: DialogInterface?, _: Int -> finish() }
                        .setPositiveButton(getString(R.string.authorization_app)) { _: DialogInterface?, _: Int ->
                            ActivityCompat.requestPermissions(this, permissions, permissionsRequestCode) }
                        .setCancelable(false)
                        .show()
            } else {
                initData()
            }
        } else {
            initData()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == permissionsRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initData()
            } else {
                Toast.makeText(this, resources.getString(R.string.no_permissions), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        if (currentNightMode == 32) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorCard)
            set(Constants.SP_NIGHT_MODE, true)
        } else {
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
            set(Constants.SP_NIGHT_MODE, false)
        }
    }

    fun showToast(text: String) {
        ToastUtil.showToast(this, text, ToastUtil.DEFAULT_SITE)
    }

}
