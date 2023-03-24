package com.tokyonth.installer.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.ktx.toast
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import kotlin.math.floor

fun Drawable.drawable2Bitmap(): Bitmap {
    return when (this) {
        is BitmapDrawable -> {
            this.bitmap
        }
        else -> {
            val config =
                if (this.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            val bitmap = Bitmap.createBitmap(
                this.intrinsicWidth,
                this.intrinsicHeight,
                config
            )
            val canvas = Canvas(bitmap)
            this.setBounds(0, 0, canvas.width, canvas.height)
            this.draw(canvas)
            bitmap
        }
    }
}

object AppHelper {

    fun toSelfSetting(context: Context, str: String?) {
        Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            data = Uri.fromParts("package", str, null)
            context.startActivity(this)
        }
    }

    fun colorBurn(RGBValues: Int): Int {
        var red = RGBValues shr 16 and 0xFF
        var green = RGBValues shr 8 and 0xFF
        var blue = RGBValues and 0xFF
        red = floor(red * (1 - 0.1)).toInt()
        green = floor(green * (1 - 0.1)).toInt()
        blue = floor(blue * (1 - 0.1)).toInt()
        return Color.rgb(red, green, blue)
    }

    fun executeSystemPkgInstall(context: Context, filePath: String) {
        val sysPkgName = if (SPDataManager.instance.isUseSystemPkg()) {
            SPDataManager.instance.getSystemPkg()
        } else {
            Constants.DEFAULT_SYS_PKG_NAME
        }
        try {
            val actName =
                context.packageManager.getPackageInfo(sysPkgName, PackageManager.GET_ACTIVITIES)
                    .let {
                        it.activities[0].name
                    }
            Intent().apply {
                val apkUri =
                    FileProvider.getUriForFile(context, Constants.PROVIDER_NAME, File(filePath))
                component = ComponentName(sysPkgName, actName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(apkUri, Constants.URI_DATA_TYPE)
                context.startActivity(this)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            toast(context.getString(R.string.open_sys_pkg_failure))
            e.printStackTrace()
        }
    }

    /**
     * 获取准确的Intent Referrer
     */
    @SuppressLint("PrivateApi")
    fun reflectGetReferrer(context: Context?): String? {
        return try {
            val activityClass = Class.forName("android.app.Activity")
            val refererField = activityClass.getDeclaredField("mReferrer")
            refererField.isAccessible = true
            refererField[context] as String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isMiuiOS(): Boolean {
        val miuiVersionCode = "ro.miui.ui.version.code"
        val miuiVersionName = "ro.miui.ui.version.name"
        val miuiInternalStorage = "ro.miui.internal.storage"
        val prop = Properties()
        try {
            prop.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return prop.getProperty(miuiVersionCode, null) != null || prop.getProperty(
            miuiVersionName,
            null
        ) != null || prop.getProperty(miuiInternalStorage, null) != null
    }

}
