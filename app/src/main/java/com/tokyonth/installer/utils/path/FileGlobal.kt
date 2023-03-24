package com.tokyonth.installer.utils.path

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.tokyonth.installer.App
import java.io.File
import java.io.FileOutputStream

internal const val AUTHORITY = ".ApkFileProvider"

const val DATA_TREE_URL =
    "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"

fun Context.isGrantDataDir(pkg: String): Boolean {
    var uriString = DATA_TREE_URL
    if (pkg.isNotEmpty()) {
        uriString = uriString.plus("%2F$pkg")
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        for (persistedUriPermission in this.contentResolver.persistedUriPermissions) {
            if (persistedUriPermission.isReadPermission
                && persistedUriPermission.uri.toString()
                == uriString
            ) {
                return true
            }
        }
        return false
    } else {
        return true
    }
}

fun Uri.copyFromStreamUri(fileName: String): File {
    val targetFile = File(App.context.externalCacheDir, fileName)
    if (targetFile.exists()) {
        targetFile.delete()
    }
    var read: Int
    val buffer = ByteArray(8 * 1024)
    val inputStream = App.context.contentResolver.openInputStream(this)
    inputStream?.use { input ->
        FileOutputStream(targetFile).use { out ->
            while (input.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            out.flush()
        }
    }
    return targetFile
}

//Permission
//----------------------------------------------------------------

/**
 * @return 传入的Uri是否已具备访问权限 (Whether the incoming Uri has access permission)
 */
fun giveUriPermission(uri: Uri?): Boolean {
    return uri?.run {
        when (App.context.checkUriPermission(
            this,
            android.os.Process.myPid(),
            android.os.Process.myUid(),
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )) {
            PackageManager.PERMISSION_GRANTED -> true
            PackageManager.PERMISSION_DENIED -> {
                App.context.grantUriPermission(
                    App.context.applicationContext.packageName,
                    this,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                false
            }
            else -> false
        }
    } ?: false
}

fun revokeUriPermission(uri: Uri?) {
    App.context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

inline fun <R> Uri.use(block: Uri.() -> R): R {
    var isAlreadyHavePermission = false
    try {
        isAlreadyHavePermission = giveUriPermission(this)
        return block()
    } catch (t: Throwable) {
        Log.e("giveUri:", "${t.message}")
    } finally {
        if (!isAlreadyHavePermission) {
            try {
                revokeUriPermission(this)
            } catch (t: Throwable) {
                Log.e("revokeUri", "${t.message}")
            }
        }
    }
    return block()
}
