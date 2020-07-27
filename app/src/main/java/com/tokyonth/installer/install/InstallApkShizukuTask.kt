package com.tokyonth.installer.install

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tokyonth.installer.Constants
import com.tokyonth.installer.Constants.SHELL_SCRIPT_CACHE_FILE
import com.tokyonth.installer.R
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.utils.PermissionHelper
import com.tokyonth.installer.utils.ToastUtil
import com.tokyonth.installer.utils.ToastUtil.showToast
import moe.shizuku.api.ShizukuApiConstants
import moe.shizuku.api.ShizukuService
import java.io.File

class InstallApkShizukuTask(private val activity: Activity,
                            private val handler: Handler,
                            private val commanderCallback: CommanderCallback,
                            private val mApkInfo: ApkInfoBean) : Thread() {

    private var retCode : Int = -1

    override fun run() {
        super.run()
        handler.post { commanderCallback.onApkPreInstall(mApkInfo) }
        if (PermissionHelper.requestPermissionByShizuku(activity)) {
            handler.post { commanderCallback.onInstallLog(mApkInfo, "Shizuku installation mode")}
            mApkInfo.apkFile?.let { rowInstall(it) }
        }
        if (retCode == 0 && mApkInfo.isFakePath) {
            if (!mApkInfo.apkFile!!.delete()) {
                Log.e("InstallApkTask", "failed to delete！")
            }
        }
        handler.post { commanderCallback.onApkInstalled(mApkInfo, retCode) }
    }

    private fun rowInstall(file: File) {
        if (!ShizukuService.pingBinder()) {
            return
        } else try {
            val command = "cat ${file.path} | pm install -S ${file.length()}"
            exec(command)
        } catch (e: Throwable) {
            Log.e("ApkShizukuInstaller", e.toString())
        }
    }

    private fun exec(command: String): Int? {
        initDir(SHELL_SCRIPT_CACHE_FILE.parentFile!!)
        SHELL_SCRIPT_CACHE_FILE.writeText(command)
        return execInternal("sh", SHELL_SCRIPT_CACHE_FILE.path)
    }

    private fun execInternal(vararg command: String): Int? {
        val process = ShizukuService.newProcess(command, null, null)
        process.waitFor()
        val errorString = process.errorStream.bufferedReader().use { it.readText() }
        val exitValue = process.exitValue()
        handler.post { commanderCallback.onInstallLog(mApkInfo, errorString)}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            process.destroyForcibly()
        } else {
            process.destroy()
        }
        return exitValue.also {
            if(exitValue == 0) {
                retCode = 0
            } else {
                Log.e("ApkShizukuInstaller", """Error: $errorString""".trimIndent())
            }
        }
    }

    private fun initDir(dir_file: File) {
        if (!dir_file.exists()) dir_file.mkdirs()
    }

}