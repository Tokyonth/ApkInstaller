package com.tokyonth.installer.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tokyonth.installer.R
import com.tokyonth.installer.data.PermissionInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.databinding.LayoutInputPkgBinding
import com.tokyonth.installer.utils.ktx.string

object DialogUtils {

    const val POSITIVE_BUTTON = 0
    const val NEGATIVE_BUTTON = 1
    const val NEUTRAL_BUTTON = 2

    fun permissionDialog(context: Context, btnClick: (Int) -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.use_app_warn)
            .setNegativeButton(R.string.exit_app) { _, _ ->
                btnClick.invoke(NEGATIVE_BUTTON)
            }
            .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                btnClick.invoke(POSITIVE_BUTTON)
            }
            .setCancelable(false)
            .show()
    }

    fun permInfoDialog(context: Context, entity: PermissionInfoEntity) {
        val lab = if (entity.permissionLabel.isEmpty()) {
            string(R.string.text_no_description)
        } else {
            entity.permissionLabel
        }
        val des = if (entity.permissionDesc.isEmpty()) {
            string(R.string.text_no_description)
        } else {
            entity.permissionDesc
        }

        MaterialAlertDialogBuilder(context)
            .setMessage(entity.permissionName + "\n\n" + lab + "\n\n" + des)
            .setPositiveButton(R.string.dialog_btn_ok, null)
            .create()
            .show()
    }

    fun unInstallDialog(context: Context, pkgName: String, btnClick: (Int) -> Unit) {
        val appName = PackageUtils.getAppNameByPackageName(context, pkgName)
        val uninstallMode = SPDataManager.instance.getInstallMode()
        val arr = context.resources.getStringArray(R.array.install_mode_arr)
        MaterialAlertDialogBuilder(context)
            .setTitle(string(R.string.text_uninstall, {
                when (uninstallMode) {
                    0 -> arr[0]
                    1 -> arr[1]
                    else -> ""
                }
            }))
            .setMessage(string(R.string.text_confirm_uninstall_app, appName))
            .setPositiveButton(R.string.apk_uninstall) { _, _ ->
                btnClick.invoke(POSITIVE_BUTTON)
            }
            .setNegativeButton(R.string.dialog_btn_cancel) { _, _ ->
                btnClick.invoke(NEGATIVE_BUTTON)
            }
            .setCancelable(false)
            .create()
            .show()
    }

    fun parseFailedDialog(context: Context, uri: Uri?, btnClick: (Int) -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setMessage(string(R.string.parse_apk_failed, uri))
            .setNegativeButton(R.string.exit_app) { _, _ ->
                btnClick.invoke(NEGATIVE_BUTTON)
            }
            .setCancelable(false)
            .show()
    }

    fun installModeDialog(context: Context, btnClick: (Int) -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setSingleChoiceItems(
                R.array.install_mode_arr,
                SPDataManager.instance.getInstallMode()
            ) { dialog, which ->
                btnClick.invoke(which)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_btn_cancel, null)
            .show()
    }

    fun systemPkgNameDialog(context: Context, resultPkg: (String) -> Unit) {
        val inputVB = LayoutInputPkgBinding.inflate(LayoutInflater.from(context))
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.text_title_input)
            .setView(inputVB.root)
            .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                inputVB.etSysPkgName.text.toString().also {
                    resultPkg.invoke(it)
                }
            }
            .setNegativeButton(R.string.dialog_btn_cancel, null)
            .show()
    }

    fun useSysPkgTipsDialog(context: Context, btnClick: (Int) -> Unit) {
        if (SPDataManager.instance.isNeverShowUsePkg())
            return
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_title_tip)
            .setMessage(R.string.use_system_pkg)
            .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                btnClick.invoke(POSITIVE_BUTTON)
            }
            .setNegativeButton(R.string.dialog_btn_cancel, null)
            .setNeutralButton(R.string.dialog_no_longer_prompt) { _, _ ->
                btnClick.invoke(NEUTRAL_BUTTON)
            }
            .setCancelable(false)
            .show()
    }

}
