package com.tokyonth.installer.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.tokyonth.installer.R

import androidx.core.app.NotificationCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tokyonth.installer.utils.ktx.string

object NotificationUtils {

    private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

    fun checkNotification(activity: Activity) {
        if (!checkNotification33(activity)) {
            MaterialAlertDialogBuilder(activity)
                .setMessage(string(R.string.notification_perm))
                .setNegativeButton(string(R.string.dialog_btn_cancel), null)
                .setPositiveButton(
                    string(R.string.dialog_btn_ok)
                ) { _, _ -> startNotificationPermission(activity) }
                .setCancelable(false)
                .show()
        }
    }

    private fun checkNotification33(activity: Activity):Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        POST_NOTIFICATIONS
                    )
                ) {
                    false
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(POST_NOTIFICATIONS), 100)
                    false
                }
            } else {
                return true
            }
        } else {
            return NotificationManagerCompat.from(activity).areNotificationsEnabled()
        }
    }

    private fun startNotificationPermission(context: Context) {
        val intent = Intent()
        val sdk = Build.VERSION.SDK_INT
        when {
            sdk >= 26 -> {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
            }
            (sdk in 21..25) -> {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            }
        }
        context.startActivity(intent)
    }

    fun sendNotification(context: Context, status: String, appName: String, appIcon: Bitmap) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "status",
                "apkInstallStatus",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableLights(true)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(context, "status")
            .setContentTitle(status)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentText(appName)
            .setLargeIcon(appIcon)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

}
