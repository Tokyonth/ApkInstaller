package com.tokyonth.installer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.path.DocumentFileUriUtils
import com.tokyonth.installer.Constants
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.utils.NotificationUtils
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import com.tokyonth.installer.utils.DialogUtils

abstract class BaseActivity : AppCompatActivity() {

    private val permissionArr = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    abstract fun setBinding(): ViewBinding?

    abstract fun initView()

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val uiMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        val isNightMode = uiMode == 0x20

        isNightMode.let {
            if (it) {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.colorCard)
            }
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                isAppearanceLightStatusBars = !it
            }
            LocalDataRepo.instance.setNightMode(it)
        }

        setBinding().let {
            if (it != null)
                setContentView(it.root)
        }
        initView()
        checkPermission()
    }

    private fun checkPermission() {
        if (LocalDataRepo.instance.isFirstBoot()) {
            DialogUtils.permissionDialog(this) {
                if (it == DialogUtils.NEGATIVE_BUTTON) {
                    finish()
                } else {
                    requestPermission()
                    NotificationUtils.checkNotification(this)
                }
            }
        } else {
            requestPermission()
            NotificationUtils.checkNotification(this)
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
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permissionArr[0]
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    permissionArr[1]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
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

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.API_R_CODE) {
            data?.let {
                contentResolver.takePersistableUriPermission(
                    it.data!!,
                    it.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                )
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
                toast(string(R.string.no_permissions))
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.PERM_CODE) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permissionArr[0]
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    permissionArr[1]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                initData()
            } else {
                toast(string(R.string.no_permissions))
                finish()
            }
        }
    }

}
