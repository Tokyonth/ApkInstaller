package com.tokyonth.installer.install

import android.net.Uri
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.zip.ZipException

class APKCommander(
    private val uri: Uri,
    private val referrer: String,
    private val callback: InstallCallback
) {

    fun startParse() {
        try {
            ParseApkTask(uri, referrer) {
                if (it != null) {
                    callback.onApkParsed(it)
                } else {
                    callback.onApkParsedFailed("Path is empty!")
                }
            }.startParseApkTask()
        } catch (e: Exception) {
            if (e is ZipException) {
                callback.onApkParsedFailed("The file is not an apk!")
            } else {
                callback.onApkParsedFailed(e.message.toString())
            }
        }
    }

    fun startInstall(apkInfoEntity: ApkInfoEntity) {
        val mode = SPDataManager.instance.getInstallMode()
        val installer = InstallerFactory.create(mode, apkInfoEntity, callback)
        CoroutineScope(Dispatchers.IO).launch {
            installer.install()
        }
    }

}
