package com.tokyonth.installer.utils

import android.content.Context
import android.content.DialogInterface
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.widget.CustomizeDialog

object VersionHelper {

    fun checkVersion(context: Context, version: Int, installedVersion: Int): String {
        return when {
            version == installedVersion -> {
                context.getString(R.string.text_equal_ver)
            }
            version > installedVersion -> {
                context.getString(R.string.text_new_ver)
            }
            else -> {
                CustomizeDialog.getInstance(context)
                        .setTitle(R.string.dialog_title_tips)
                        .setMessage(R.string.low_ver_msg)
                        .setPositiveButton(R.string.text_i_know) { _: DialogInterface?, _: Int -> SPUtils.putData(Constants.SP_NEVER_TIP_VERSION, false) }
                        .setNegativeButton(R.string.dialog_no_longer_prompt, null)
                        .setCancelable(false).create().show()
                context.getString(R.string.text_low_ver)
            }
        }
    }

}