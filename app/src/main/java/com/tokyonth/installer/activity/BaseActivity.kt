package com.tokyonth.installer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.App
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.path.DocumentFileUriUtils
import com.tokyonth.installer.view.CustomizeDialog

import com.tokyonth.installer.Constants
import com.tokyonth.installer.utils.NotificationUtil

abstract class BaseActivity : AppCompatActivity() {

    private val permissionArr = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    abstract fun initView(): ViewBinding?

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        initView().let {
            if (it != null)
                setContentView(it.root)
        }
        check()
    }

    private fun check() {
        if (App.localData.isFirstBoot()) {
            CustomizeDialog.getInstance(this)
                    .setMessage(getString(R.string.use_app_warn))
                    .setNegativeButton(getString(R.string.exit_app)) { _, _ -> finish() }
                    .setPositiveButton(getString(R.string.dialog_btn_ok)) { _, _ ->
                        requestPermission()
                        NotificationUtil().checkNotification(this)
                    }
                    .setCancelable(false)
                    .show()
        } else {
            requestPermission()
            NotificationUtil().checkNotification(this)
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                initData()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, Constants.PERM_CODE)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, permissionArr[0]) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, permissionArr[1]) == PackageManager.PERMISSION_GRANTED) {
                initData()
            } else {
                ActivityCompat.requestPermissions(this, permissionArr, Constants.PERM_CODE)
            }
        } else {
            initData()
        }
    }

    private fun checkAndroidR() {
        if (!DocumentFileUriUtils.isGrant(this)) {
            DocumentFileUriUtils.startForRoot(this, Constants.API_R_CODE)
        } else {
            initData()
        }
    }

    fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        if (currentNightMode == 32) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorCard)
            App.localData.setNightMode(true)
        } else {
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
            App.localData.setNightMode(false)
        }
    }

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.API_R_CODE) {
            data?.let {
                contentResolver.takePersistableUriPermission(it.data!!, it.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                initData()
            }
        }

        if (requestCode == Constants.PERM_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                    checkAndroidR()
                } else {
                    initData()
                }
            } else {
                showToast(getString(R.string.no_permissions))
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.PERM_CODE) {
            if (ActivityCompat.checkSelfPermission(this, permissionArr[0]) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, permissionArr[1]) == PackageManager.PERMISSION_GRANTED) {
                initData()
            } else {
                showToast(getString(R.string.no_permissions))
                finish()
            }
        }
    }

}
