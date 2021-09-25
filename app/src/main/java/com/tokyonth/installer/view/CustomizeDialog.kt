package com.tokyonth.installer.view

import android.content.Context

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tokyonth.installer.App
import com.tokyonth.installer.R

class CustomizeDialog private constructor(context: Context, overrideThemeResId: Int) :
        MaterialAlertDialogBuilder(context, overrideThemeResId) {
    companion object {

        fun getInstance(context: Context): CustomizeDialog {
            val theme = if (App.localData.isNightMode())
                R.style.DialogTheme
            else
                0
            return CustomizeDialog(context, theme)
        }

    }

}
