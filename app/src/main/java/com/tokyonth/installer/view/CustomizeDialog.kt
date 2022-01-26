package com.tokyonth.installer.view

import android.content.Context

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tokyonth.installer.R
import com.tokyonth.installer.data.LocalDataRepo

class CustomizeDialog private constructor(context: Context, overrideThemeResId: Int) :
    MaterialAlertDialogBuilder(context, overrideThemeResId) {
    companion object {

        fun get(context: Context): CustomizeDialog {
            val theme = if (LocalDataRepo.instance.isNightMode())
                R.style.DialogTheme
            else
                0
            return CustomizeDialog(context, theme)
        }

    }

}
