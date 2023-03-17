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
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import kotlin.math.floor

object AppHelper {

    fun checkVersion(context: Context, version: Int, installedVersion: Int): String {
        return when {
            version == installedVersion -> string(R.string.apk_equal_version)

            version > installedVersion -> string(R.string.apk_new_version)

            else -> {
                if (!SPDataManager.instance.isNeverShowTip()) {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.dialog_title_tip)
                        .setMessage(R.string.low_version_tip)
                        .setPositiveButton(R.string.dialog_btn_ok, null)
                        .setNegativeButton(R.string.dialog_no_longer_prompt) { _, _ ->
                            SPDataManager.instance.setNeverShowTip()
                        }
                        .setCancelable(false)
                        .show()
                }
                string(R.string.apk_low_version)
            }
        }
    }

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

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val bitmap = Bitmap.createBitmap(w, h, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    fun getBitmapFromDrawable(context: Context?, @DrawableRes drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawable || drawable is VectorDrawableCompat) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    fun startSystemPkgInstall(context: Context, filePath: String?) {
        val sysPkgName: String = if (SPDataManager.instance.isUseSystemPkg()) {
            SPDataManager.instance.getSystemPkg()
        } else {
            Constants.DEFAULT_SYS_PKG_NAME
        }
        val activityName: String? = try {
            context.packageManager.getPackageInfo(sysPkgName, PackageManager.GET_ACTIVITIES)
                .let {
                    it.activities[0].name
                }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
        if (activityName != null) {
            Intent().apply {
                val apkUri =
                    FileProvider.getUriForFile(context, Constants.PROVIDER_NAME, File(filePath!!))
                component = ComponentName(sysPkgName, activityName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(apkUri, Constants.URI_DATA_TYPE)
                context.startActivity(this)
            }
        } else {
            toast(context.getString(R.string.open_sys_pkg_failure))
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
