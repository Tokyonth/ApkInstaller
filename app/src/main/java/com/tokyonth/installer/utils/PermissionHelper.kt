package com.tokyonth.installer.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import moe.shizuku.api.ShizukuApiConstants

object PermissionHelper {

    fun requestPermissionByIcebox(activity: Activity): Boolean {
        var havePermission = false
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(IceBox.SDK_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0x233)
        } else {
            havePermission = true
        }
        return havePermission
    }

    fun requestPermissionByShizuku(activity: Activity): Boolean {
        var havePermission = false
        val permission = ShizukuApiConstants.PERMISSION
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ToastUtil.showToast(activity, activity.getString(R.string.shizuku_permission_request), ToastUtil.DEFAULT_SITE)
            }
            ActivityCompat.requestPermissions(activity,
                    arrayOf(permission),
                    Constants.PERMISSION_REQUEST_CODE)
        } else
            havePermission = true
        return havePermission
    }

}