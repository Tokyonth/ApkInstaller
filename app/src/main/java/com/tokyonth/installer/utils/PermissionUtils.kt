package com.tokyonth.installer.utils

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.catchingnow.icebox.sdk_client.IceBox
import rikka.shizuku.ShizukuProvider

object PermissionUtils {

    fun requestIceBoxPermission(activity: AppCompatActivity, code: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity,
                IceBox.SDK_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(IceBox.SDK_PERMISSION),
                code
            )
            false
        } else {
            true
        }
    }

    fun requestShizukuPermission(activity: AppCompatActivity, code: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity,
                ShizukuProvider.PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    ShizukuProvider.PERMISSION
                )
            ) {
                return false
            }
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(ShizukuProvider.PERMISSION),
                code
            )
            false
        } else {
            true
        }
    }

}
