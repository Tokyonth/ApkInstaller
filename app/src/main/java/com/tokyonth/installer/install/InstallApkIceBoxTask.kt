package com.tokyonth.installer.install

import androidx.core.content.FileProvider
import com.catchingnow.icebox.sdk_client.IceBox
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.utils.ktx.string
import java.io.File

class InstallApkIceBoxTask(
    apkInfoEntity: ApkInfoEntity,
    installCallback: InstallCallback
) : BaseInstaller(apkInfoEntity, installCallback) {

    override suspend fun install() {
        val iceboxState = when (IceBox.querySupportSilentInstall(App.context)) {
            IceBox.SilentInstallSupport.NOT_DEVICE_OWNER -> {
                string(R.string.not_icebox_owner)
            }
            IceBox.SilentInstallSupport.PERMISSION_REQUIRED -> {
                string(R.string.not_icebox_perm)
            }
            IceBox.SilentInstallSupport.SYSTEM_NOT_SUPPORTED -> {
                string(R.string.not_support_icebox_system)
            }
            IceBox.SilentInstallSupport.UPDATE_REQUIRED -> {
                string(R.string.not_support_icebox_version)
            }
            IceBox.SilentInstallSupport.NOT_INSTALLED -> {
                string(R.string.not_install_icebox)
            }
            IceBox.SilentInstallSupport.NOT_RUNNING -> {
                string(R.string.icebox_not_running)
            }
            else -> ""
        }
        installCallback.onInstallLog(iceboxState)
        if (iceboxState.isNotEmpty()) {
            installCallback.onApkInstalled(false)
            return
        }
        val authority = Constants.PROVIDER_NAME
        val uri = FileProvider.getUriForFile(App.context, authority, File(apkInfoEntity.filePath))
        val status = IceBox.installPackage(App.context, uri)
        installCallback.onApkInstalled(status)
    }

}
