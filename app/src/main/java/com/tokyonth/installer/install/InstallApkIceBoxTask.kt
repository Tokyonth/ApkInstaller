package com.tokyonth.installer.install

import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.App
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.utils.doAsync
import com.tokyonth.installer.utils.onUI
import android.net.Uri
import androidx.annotation.StringRes

import androidx.core.content.FileProvider
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import java.io.File

class InstallApkIceBoxTask(private val apkInfoEntity: ApkInfoEntity,
                           private val installCallback: InstallCallback) {

    private val context = App.context

    fun start() {
        installCallback.onApkPreInstall()
        val iceboxState = when (IceBox.querySupportSilentInstall(context)) {
            IceBox.SilentInstallSupport.NOT_DEVICE_OWNER -> {
                getString(R.string.not_icebox_owner)
            }
            IceBox.SilentInstallSupport.PERMISSION_REQUIRED -> {
                getString(R.string.not_icebox_perm)
            }
            IceBox.SilentInstallSupport.SYSTEM_NOT_SUPPORTED -> {
                getString(R.string.not_support_icebox_system)
            }
            IceBox.SilentInstallSupport.UPDATE_REQUIRED -> {
                getString(R.string.not_support_icebox_version)
            }
            IceBox.SilentInstallSupport.NOT_INSTALLED -> {
                //PackageUtils.getVersionNameByPackageName(context, Constants.ICEBOX_PKG_NAME)
                getString(R.string.not_install_icebox)
            }
            IceBox.SilentInstallSupport.NOT_RUNNING -> {
                getString(R.string.icebox_not_running)
            }
            else -> ""
        }
        installCallback.onInstallLog(iceboxState)
        if (iceboxState.isNotEmpty()) {
            installCallback.onApkInstalled(InstallStatus.FAILURE)
            return
        }
        doAsync {
            val authority: String = Constants.PROVIDER_STR
            val uri: Uri = FileProvider.getUriForFile(context, authority, File(apkInfoEntity.filePath!!))
            val status = IceBox.installPackage(context, uri)
            onUI {
                if (status) {
                    installCallback.onApkInstalled(InstallStatus.SUCCESS)
                } else {
                    installCallback.onApkInstalled(InstallStatus.FAILURE)
                }
            }
        }
    }

    private fun getString(@StringRes id: Int): String {
        return context.getString(id)
    }

}
