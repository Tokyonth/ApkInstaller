package com.tokyonth.installer.view

import android.content.Context

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.SPUtils.get

class CustomizeDialog private constructor(context: Context, overrideThemeResId: Int) :
        MaterialAlertDialogBuilder(context, overrideThemeResId) {
    companion object {

        fun getInstance(context: Context): CustomizeDialog {
            val theme = if (context[Constants.SP_NIGHT_MODE, false])
                R.style.DialogTheme
            else
                0
            return CustomizeDialog(context, theme)
        }
    }

}
