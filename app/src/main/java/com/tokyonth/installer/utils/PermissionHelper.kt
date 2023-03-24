package com.tokyonth.installer.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.tokyonth.installer.utils.path.DATA_TREE_URL
import com.tokyonth.installer.utils.path.isGrantDataDir

class PermissionHelper(
    private val activity: AppCompatActivity
) {

    private val permissionArray = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var isGrant: ((Boolean, Int) -> Unit)? = null

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var requestDataLauncher: ActivityResultLauncher<Intent>? = null

    private var requestResultLauncher: ActivityResultLauncher<Intent>? = null

    private var requestCode = -1

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestR() {
        val c = ActivityResultContracts.StartActivityForResult()
        requestResultLauncher = activity.registerForActivityResult(c) {
            isGrant?.invoke(true, requestCode)
        }
        if (Environment.isExternalStorageManager()) {
            isGrant?.invoke(true, requestCode)
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            requestResultLauncher?.launch(intent)
        }
    }

    private fun requestM(array: Array<String>) {
        val c = ActivityResultContracts.RequestMultiplePermissions()
        requestPermissionLauncher = activity.registerForActivityResult(c) { map ->
            val all = map.filter { !it.value }
            isGrant?.invoke(all.isEmpty(), requestCode)
        }
        requestPermissionLauncher?.launch(array)
    }

    private fun requestDataDir(pkg: String) {
        var uriString = DATA_TREE_URL
        if (pkg.isNotEmpty()) {
            uriString = uriString.plus("%2F$pkg")
        }
        val uri = Uri.parse(uriString)
        val documentFile = DocumentFile.fromTreeUri(activity, uri)
        val intent1 = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent1.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert(documentFile != null)
            intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile!!.uri)
        }
        requestDataLauncher?.launch(intent1)
    }

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestR()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestM(permissionArray)
        } else {
            isGrant?.invoke(true, requestCode)
        }
    }

    @SuppressLint("WrongConstant")
    fun startData(pkg: String) {
        val c = ActivityResultContracts.StartActivityForResult()
        requestDataLauncher = activity.registerForActivityResult(c) {
            it.data?.let { intent ->
                activity.contentResolver.takePersistableUriPermission(
                    intent.data!!,
                    intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                )
                isGrant?.invoke(true, requestCode)
            }
        }
        if (!activity.isGrantDataDir(pkg)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestDataDir(pkg)
                } else {
                    requestDataDir("")
                }
            }
        } else {
            isGrant?.invoke(true, requestCode)
        }
    }

    fun start(array: Array<String>, requestCode: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestM(array)
        } else {
            isGrant?.invoke(true, requestCode)
        }
    }

    fun registerCallback(isGrant: ((Boolean, Int) -> Unit)? = null) {
        this.isGrant = isGrant
    }

    fun dispose() {
        requestPermissionLauncher?.unregister()
        requestDataLauncher?.unregister()
        requestResultLauncher?.unregister()
    }

}
