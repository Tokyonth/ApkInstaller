package com.tokyonth.installer.utils

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
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.view.CustomizeDialog
import java.io.File
import kotlin.math.floor

object CommonUtils {

    fun checkVersion(context: Context, version: Int, installedVersion: Int): String {
        return when {
            version == installedVersion -> {
                context.getString(R.string.apk_equal_version)
            }
            version > installedVersion -> {
                context.getString(R.string.apk_new_version)
            }
            else -> {
                if (!App.localData.isNeverShowTip()) {
                    CustomizeDialog.getInstance(context)
                            .setTitle(R.string.dialog_title_tip)
                            .setMessage(R.string.low_version_tip)
                            .setPositiveButton(R.string.dialog_btn_ok, null)
                            .setNegativeButton(R.string.dialog_no_longer_prompt) { _, _ ->
                                App.localData.setNeverShowTip()
                            }
                            .setCancelable(false)
                            .show()
                }
                context.getString(R.string.apk_low_version)
            }
        }
    }

    fun toSelfSetting(context: Context, str: String?) {
        val intent = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            data = Uri.fromParts("package", str, null)
        }
        context.startActivity(intent)
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

    @Suppress("DEPRECATION")
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val config = if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
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
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    fun startSystemPkgInstall(context: Context, filePath: String?) {
        val intent = Intent()
        var activityName: String? = null
        val sysPkgName: String = if (App.localData.isUseSystemPkg()) {
            App.localData.getSystemPkg()
        } else {
            Constants.DEFAULT_SYS_PKG_NAME
        }
        try {
            activityName = context.packageManager.getPackageInfo(sysPkgName, PackageManager.GET_ACTIVITIES).let {
                it.activities[0].name
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (activityName != null) {
            intent.apply {
                val apkUri = FileProvider.getUriForFile(context, Constants.PROVIDER_STR, File(filePath!!))
                component = ComponentName(sysPkgName, activityName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(apkUri, Constants.URI_DATA_TYPE)
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, context.getString(R.string.open_sys_pkg_failure), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 获取准确的Intent Referrer
     */
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

}
