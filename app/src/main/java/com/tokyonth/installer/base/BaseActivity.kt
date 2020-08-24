package com.tokyonth.installer.base

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.SPUtils
import com.tokyonth.installer.utils.ToastUtil

abstract class BaseActivity : AppCompatActivity() {

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private var isAuthorize = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        if (setActivityView() != 0)
            setContentView(setActivityView())
        if (checkPermission()) {
            initActivity(savedInstanceState)
        } else {
            Toast.makeText(this, resources.getString(R.string.no_permissions), Toast.LENGTH_SHORT).show()
            finish()
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
        ActivityCompat.requestPermissions(this, permissions, 100)
    }

    fun showToast(text: String) {
        ToastUtil.showToast(this, text, ToastUtil.DEFAULT_SITE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isAuthorize = grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        if (currentNightMode == 32) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.colorCard)
            SPUtils.putData(Constants.SP_NIGHT_MODE, true)
        } else {
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            }
            SPUtils.putData(Constants.SP_NIGHT_MODE, false)
        }
    }

}
