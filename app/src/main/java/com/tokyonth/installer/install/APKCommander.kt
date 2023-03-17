package com.tokyonth.installer.install

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.tokyonth.installer.App

import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.utils.ktx.doAsync
import com.tokyonth.installer.utils.ktx.onUI
import java.io.File

class APKCommander {

    private lateinit var apkInfoEntity: ApkInfoEntity

    private var uri: Uri? = null
    private var referrer: String? = null
    private var installCallback: InstallCallback

    constructor(apkInfoEntity: ApkInfoEntity, installCallback: InstallCallback) {
        this.apkInfoEntity = apkInfoEntity
        this.installCallback = installCallback
    }

    constructor(uri: Uri, referrer: String, installCallback: InstallCallback) {
        this.uri = uri
        this.referrer = referrer
        this.installCallback = installCallback
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getUriPath(): String? {
        var path: String? = null
        runCatching {
            App.context.contentResolver.openFileDescriptor(uri!!, "rw")?.let {
                val file = File("/proc/self/fd/${it.fd}")
               // path = Files.readSymbolicLink(file.toPath()).pathString
                //path = file.canonicalPath
                //path = Os.readlink(file.path)
                Log.e("path0---->", file.path + " fix")
                it.close()
            }
            path
        }.onSuccess {
            Log.e("path 1---->", it!! + " fix")
        }.onFailure {
            Log.e("path err---->", it.message!!)
        }
        return path
    }

    fun startParse() {
      //  getUriPath()

        if (this::apkInfoEntity.isInitialized) {
            installCallback.onApkParsed(apkInfoEntity)
            return
        }
        ParseApkTask(uri!!, referrer!!).let {
            doAsync({
                Log.e("ParseApkError->", it.message!!)
            }, {
                apkInfoEntity = it.startParseApkTask()
                onUI {
                    installCallback.onApkParsed(apkInfoEntity)
                }
            })
        }
    }

    fun startInstall() {
        InstallerFactory.create(SPDataManager.instance.getInstallMode()).apply {
            make(installCallback, apkInfoEntity)
            install()
        }
    }

}
