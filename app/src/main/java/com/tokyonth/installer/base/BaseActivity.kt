package com.tokyonth.installer.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.SPUtils
import com.tokyonth.installer.utils.StatusBarColorUtils
import com.tokyonth.installer.utils.ToastUtil

abstract class BaseActivity : AppCompatActivity() {

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private var isAuthorize = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarColorUtils.setStatusBarDarkIcon(this, !(SPUtils.getData(Constants.SP_NIGHT_MODE, false) as Boolean))
        if (setActivityView() != 0)
            setContentView(setActivityView())
        if (intent.data == null) {
            finish()
        } else {
            if (checkPermission()) {
                initActivity(savedInstanceState)
            } else {
                Toast.makeText(this, resources.getString(R.string.no_permissions), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    abstract fun setActivityView(): Int

    abstract fun initActivity(savedInstanceState: Bundle?)

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                startRequestPermission()
            } else {
                isAuthorize = true
            }
        } else {
            isAuthorize = true
        }
        return isAuthorize
    }

    private fun startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, Constants.PERMISSION_REQUEST_CODE)
    }

    fun showToast(text: String) {
        ToastUtil.showToast(this, text, ToastUtil.DEFAULT_SITE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == Constants.PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isAuthorize = grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }
    }

}
