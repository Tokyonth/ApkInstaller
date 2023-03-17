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
import com.tokyonth.installer.utils.path.DocumentFileUriUtils

class PermissionHelper(
    private val activity: AppCompatActivity,
    private val isGrant: (Boolean) -> Unit
) {

    private val permissionArray = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var requestResultLauncher: ActivityResultLauncher<Intent>? = null

    private fun requestData() {
        val uri =
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
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
        requestResultLauncher?.launch(intent1)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestR() {
        if (Environment.isExternalStorageManager()) {
            if (!DocumentFileUriUtils.isGrant(activity)) {
                requestData()
            } else {
                isGrant.invoke(true)
            }
        } else {
            val c = ActivityResultContracts.StartActivityForResult()
            requestResultLauncher = activity.registerForActivityResult(c) {
                it.data?.let { intent ->
                    saveFlag(intent)
                }
            }
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            requestResultLauncher?.launch(intent)
        }
    }

    private fun requestM() {
        val c = ActivityResultContracts.RequestMultiplePermissions()
        requestPermissionLauncher = activity.registerForActivityResult(c) { map ->
            val all = map.filter { !it.value }
            isGrant.invoke(all.isEmpty())
        }
        requestPermissionLauncher?.launch(permissionArray)
    }

    @SuppressLint("WrongConstant")
    private fun saveFlag(intent: Intent) {
        activity.contentResolver.takePersistableUriPermission(
            intent.data!!,
            intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        )
        isGrant.invoke(true)
    }

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestR()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestM()
        } else {
            isGrant.invoke(true)
        }
    }

    fun dispose() {
        requestPermissionLauncher?.unregister()
        requestResultLauncher?.unregister()
    }

}
