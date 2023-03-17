package com.tokyonth.installer.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.tokyonth.installer.R

import androidx.core.app.NotificationCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object NotificationUtils {

    fun checkNotification(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            MaterialAlertDialogBuilder(context)
                .setMessage(context.getString(R.string.notification_perm))
                .setNegativeButton(context.getString(R.string.dialog_btn_cancel), null)
                .setPositiveButton(
                    context.getString(R.string.dialog_btn_ok)
                ) { _, _ -> startNotificationPerm(context) }
                .setCancelable(false)
                .show()
        }
    }

    private fun startNotificationPerm(context: Context) {
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
        val channel: NotificationChannel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                "status",
                "apkInstallStatus",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableLights(true)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "status")
            .setContentTitle(status)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentText(appName)
            .setLargeIcon(appIcon)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

}
