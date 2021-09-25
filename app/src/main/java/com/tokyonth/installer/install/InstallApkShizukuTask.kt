package com.tokyonth.installer.install

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.IPackageInstallerSession
import android.content.pm.PackageInstaller
import android.os.Process
import com.tokyonth.installer.App
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.install.shizuku.IIntentSenderAdaptor
import com.tokyonth.installer.install.shizuku.IntentSenderUtils
import com.tokyonth.installer.install.shizuku.PackageInstallerUtils
import com.tokyonth.installer.install.shizuku.ShizukuSystemServerApi
import com.tokyonth.installer.utils.doAsync
import com.tokyonth.installer.utils.onUI
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import java.io.File
import java.util.concurrent.CountDownLatch

class InstallApkShizukuTask(private val apkInfoEntity: ApkInfoEntity,
                            private val installCallback: InstallCallback) {

    private val context: Context = App.context

    fun start() {
        installCallback.onApkPreInstall()
        val apkFile = File(apkInfoEntity.filePath!!)
        doAsync {
            var session: PackageInstaller.Session? = null
            val res: StringBuilder = StringBuilder()

            try {
                session = getPackageInstallerSession()
                res.append('\n').append("write: ")
                doWriteSession(session, apkFile)

                res.append('\n').append("commit: ")
                val result = doCommitSession(session)
                val status = result!!.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                val message = result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                res.append('\n').append("status: ").append(status).append(" (").append(message).append(")")
                onUI {
                    installCallback.onApkInstalled(InstallStatus.invoke(status))
                }
            } catch (tr: Throwable) {
                tr.printStackTrace()
                res.append(tr)
                onUI {
                    installCallback.onApkInstalled(InstallStatus.invoke(-1))
                }
            } finally {
                try {
                    session?.close()
                } catch (tr: Throwable) {
                    res.append(tr)
                }
            }
            onUI {
                installCallback.onInstallLog(res.toString().trim())
            }
        }
    }

    private fun getPackageInstaller(): PackageInstaller {
        val isRoot = Shizuku.getUid() == 0
        // the reason for use "com.android.shell" as installer package under adb is that getMySessions will check installer package's owner
        val installerPackageName = if (isRoot) context.packageName else "com.android.shell"
        val userId = if (isRoot) Process.myUserHandle().hashCode() else 0
        return PackageInstallerUtils.createPackageInstaller(ShizukuSystemServerApi.PackageManager_getPackageInstaller(), installerPackageName, userId)
    }

    private fun getPackageInstallerSession(totalSize: Long? = null): PackageInstaller.Session {
        val res: StringBuilder = StringBuilder()
        res.append("createSession: ")

        val params: PackageInstaller.SessionParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        totalSize?.let {
            params.setSize(totalSize)
        }
        var installFlags = PackageInstallerUtils.getInstallFlags(params)
        installFlags = installFlags or (0x00000004 /*PackageManager.INSTALL_ALLOW_TEST*/ or 0x00000002) /*PackageManager.INSTALL_REPLACE_EXISTING*/
        PackageInstallerUtils.setInstallFlags(params, installFlags)

        val packageInstaller = getPackageInstaller()
        val sessionId = packageInstaller.createSession(params)
        res.append(sessionId)
        @Suppress("LocalVariableName")
        val _session = IPackageInstallerSession.Stub.asInterface(ShizukuBinderWrapper(ShizukuSystemServerApi.PackageManager_getPackageInstaller().openSession(sessionId).asBinder()))
        return PackageInstallerUtils.createSession(_session)
    }

    private fun doWriteSession(session: PackageInstaller.Session, apkFile: File) {
        val inputStream = apkFile.inputStream()
        val outputStream = session.openWrite(apkFile.name, 0, -1)
        val buf = ByteArray(8192)
        var len: Int

        while (inputStream.read(buf).also { len = it } > 0) {
            outputStream.write(buf, 0, len)
            outputStream.flush()
            session.fsync(outputStream)
        }
        inputStream.close()
        outputStream.close()
    }

    private fun doCommitSession(session: PackageInstaller.Session): Intent? {
        val results = arrayOf<Intent?>(null)
        val countDownLatch = CountDownLatch(1)
        val intentSender: IntentSender = IntentSenderUtils.newInstance(object : IIntentSenderAdaptor() {
            override fun send(intent: Intent?) {
                results[0] = intent
                countDownLatch.countDown()
            }
        })
        session.commit(intentSender)
        countDownLatch.await()
        return results[0]
    }

}
