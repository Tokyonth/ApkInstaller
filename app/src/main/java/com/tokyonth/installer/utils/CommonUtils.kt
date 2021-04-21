package com.tokyonth.installer.utils

import android.Manifest
import android.app.Activity
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
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.databinding.LayoutCommonToastBinding
import com.tokyonth.installer.utils.SPUtils.get
import com.tokyonth.installer.utils.SPUtils.set
import com.tokyonth.installer.view.CustomizeDialog
import moe.shizuku.api.ShizukuApiConstants
import java.io.File
import kotlin.math.floor

object CommonUtils {

    @JvmStatic
    fun showToast(context: Context, msg: String) {
        val vb = LayoutCommonToastBinding.bind(View.inflate(context, R.layout.layout_common_toast, null))
        Toast(context).apply {
            view = vb.root
            vb.tvCommonToast.text = msg
            duration = Toast.LENGTH_SHORT
            show()
        }
    }

    fun checkVersion(context: Context, version: Int, installedVersion: Int): String {
        return when {
            version == installedVersion -> {
                context.getString(R.string.text_equal_ver)
            }
            version > installedVersion -> {
                context.getString(R.string.text_new_ver)
            }
            else -> {
                context[Constants.SP_NEVER_TIP_VERSION, true].let {
                    if (it) {
                        CustomizeDialog.getInstance(context)
                                .setTitle(R.string.dialog_title_tips)
                                .setMessage(R.string.low_ver_msg)
                                .setPositiveButton(R.string.text_i_know, null)
                                .setNegativeButton(R.string.dialog_no_longer_prompt) { _, _ ->
                                    context[Constants.SP_NEVER_TIP_VERSION] = false
                                }
                                .setCancelable(false).create().show()
                    }
                }
                context.getString(R.string.text_low_ver)
            }
        }
    }

    fun requestPermissionByIcebox(activity: Activity): Boolean {
        var havePermission = false
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(IceBox.SDK_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0x233)
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
                showToast(activity, activity.getString(R.string.shizuku_permission_request))
            }
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 100)
        } else {
            havePermission = true
        }
        return havePermission
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
    fun drawableToBitmap(drawable: Drawable): Bitmap? {
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
        val sysPkgName: String = if (context[Constants.SP_USE_SYS_PKG, false]) {
            context[Constants.SYS_PKG_NAME_KEY, Constants.DEFAULT_SYS_PKG_NAME]
        } else {
            Constants.DEFAULT_SYS_PKG_NAME
        }
        try {
            val packageInfo = context.packageManager.getPackageInfo(sysPkgName, PackageManager.GET_ACTIVITIES)
            activityName = packageInfo.activities[0].name
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
            showToast(context, context.getString(R.string.open_sys_pkg_failure))
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
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            null
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            null
        }
    }

}