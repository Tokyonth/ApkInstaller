package com.tokyonth.installer.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import com.tokyonth.installer.R
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.data.PermFullEntity
import com.tokyonth.installer.databinding.LayoutInputPkgBinding
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.view.CustomizeDialog

object DialogUtils {

    const val POSITIVE_BUTTON = 0
    const val NEGATIVE_BUTTON = 1
    const val NEUTRAL_BUTTON = 2

    fun permissionDialog(context: Context, btnClick: (Int) -> Unit) {
        CustomizeDialog.get(context)
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

    fun permInfoDialog(context: Context, entity: PermFullEntity) {
        val lab = if (entity.lab.isEmpty()) {
            string(R.string.text_no_description)
        } else {
            entity.lab
        }
        val des = if (entity.des.isEmpty()) {
            string(R.string.text_no_description)
        } else {
            entity.des
        }

        CustomizeDialog.get(context)
            .setMessage(entity.perm + "\n\n" + lab + "\n\n" + des)
            .setPositiveButton(R.string.dialog_btn_ok, null)
            .create()
            .show()
    }

    fun unInstallDialog(context: Context, pkgName: String, btnClick: (Int) -> Unit) {
        val appName = PackageUtils.getAppNameByPackageName(context, pkgName)
        val uninstallMode = LocalDataRepo.instance.getInstallMode()
        val arr = context.resources.getStringArray(R.array.install_mode_arr)
        CustomizeDialog.get(context)
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
        CustomizeDialog.get(context)
            .setMessage(string(R.string.parse_apk_failed, uri))
            .setNegativeButton(R.string.exit_app) { _, _ ->
                btnClick.invoke(NEGATIVE_BUTTON)
            }
            .setCancelable(false)
            .show()
    }

    fun installModeDialog(context: Context, btnClick: (Int) -> Unit) {
        CustomizeDialog.get(context)
            .setSingleChoiceItems(
                R.array.install_mode_arr,
                LocalDataRepo.instance.getInstallMode()
            ) { dialog, which ->
                btnClick.invoke(which)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_btn_cancel, null)
            .show()
    }

    fun systemPkgNameDialog(context: Context, resultPkg: (String) -> Unit) {
        val inputVB = LayoutInputPkgBinding.inflate(LayoutInflater.from(context))
        CustomizeDialog.get(context)
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
        if (LocalDataRepo.instance.isNeverShowUsePkg())
            return
        CustomizeDialog.get(context)
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
