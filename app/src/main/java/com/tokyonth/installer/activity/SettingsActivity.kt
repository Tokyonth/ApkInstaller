package com.tokyonth.installer.activity

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding

import com.google.android.material.snackbar.Snackbar
import com.tokyonth.installer.App
import com.tokyonth.installer.BuildConfig
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.SettingsAdapter
import com.tokyonth.installer.adapter.SettingsAdapter.*
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.view.CustomizeDialog
import com.tokyonth.installer.databinding.ActivitySettingsBinding
import com.tokyonth.installer.databinding.LayoutInputPkgBinding
import com.tokyonth.installer.utils.PackageUtils
import com.tokyonth.installer.utils.FileIOUtils
import com.tokyonth.installer.utils.lazyBind
import rikka.shizuku.ShizukuProvider.PERMISSION
import com.catchingnow.icebox.sdk_client.IceBox

class SettingsActivity : BaseActivity() {

    private val viewBind: ActivitySettingsBinding by lazyBind()

    private val localDataRepo: LocalDataRepo = App.localData

    private var settingsAdapter: SettingsAdapter? = null

    override fun initView(): ViewBinding {
        initViewStatus()
        return viewBind
    }

    override fun initData() {
        App.localData.setNotFirstBoot()
    }

    private fun initViewStatus() {
        viewBind.cbUseSysPkg.isChecked = localDataRepo.isUseSystemPkg()
        viewBind.tvPkgName.text = localDataRepo.getSystemPkg()

        settingsAdapter = SettingsAdapter(this@SettingsActivity).apply {
            setOnItemClickListener(object : OnItemClickListener {
                override fun onSwitch(pos: Int, bool: Boolean) {
                    when (pos) {
                        0 -> localDataRepo.setShowPermission(bool)
                        1 -> localDataRepo.setShowActivity(bool)
                        2 -> localDataRepo.setDefaultSilent(bool)
                        3 -> localDataRepo.setAutoDel(bool)
                        4 -> {
                            if (bool)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            localDataRepo.setFollowSystem(bool)
                        }
                    }
                }

                override fun onClick(pos: Int) {
                    if (pos == 5) installModeDialog()
                }
            })
        }
        viewBind.rvSettings.apply {
            layoutManager = GridLayoutManager(this@SettingsActivity, 1)
            adapter = settingsAdapter
        }

        val cacheSize = FileIOUtils.byteToString(FileIOUtils.getFileOrFolderSize(cacheDir))
        viewBind.tvApkCache.text = getString(R.string.text_apk_cache, cacheSize)
        viewBind.tvVersion.text = getString(R.string.text_settings_version, BuildConfig.VERSION_NAME)
        viewBind.cbUseSysPkg.setOnCheckedChangeListener { _, isChecked -> localDataRepo.setUseSystemPkg(isChecked) }
        viewBind.cardPkg.setOnClickListener { systemPkgNameDialog() }
        viewBind.cardApkCache.setOnClickListener {
            FileIOUtils.deleteFolderFile(cacheDir.path, true)
            Snackbar.make(viewBind.rootLayout, getString(R.string.clean_complete), Snackbar.LENGTH_SHORT).show()
            viewBind.tvApkCache.text = getString(R.string.text_apk_cache, "0B")
        }
    }

    private fun requestShizukuPermission(code: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(this, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION)) {
                return false
            }
            ActivityCompat.requestPermissions(this,
                    arrayOf(PERMISSION),
                    code)
            false
        } else {
            true
        }
    }

    private fun requestIceBoxPermission(code: Int): Boolean {
        return if (ContextCompat.checkSelfPermission(this, IceBox.SDK_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(IceBox.SDK_PERMISSION),
                    code)
            false
        } else {
            true
        }
    }

    private fun installModeDialog() {
        CustomizeDialog.getInstance(this)
                .setSingleChoiceItems(R.array.install_mode_arr, localDataRepo.getInstallMode()) { dialog, which ->
                    when (which) {
                        0 -> localDataRepo.setInstallMode(which)
                        1 -> shizukuCheck(which)
                        2 -> iceBoxCheck(which)
                    }
                    settingsAdapter?.updateInstallMode()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.dialog_btn_cancel, null)
                .show()
    }

    private fun iceBoxCheck(code: Int) {
        if (!PackageUtils.isIceBoxClientAvailable(this)) {
            Snackbar.make(viewBind.rootLayout, getString(R.string.not_install_icebox), Snackbar.LENGTH_SHORT).show()
        } else {
            if (requestIceBoxPermission(code)) {
                localDataRepo.setInstallMode(code)
                settingsAdapter?.updateInstallMode()
            }
        }
    }

    private fun shizukuCheck(code: Int) {
        if (Build.VERSION.SDK_INT <= 23) {
            CustomizeDialog.getInstance(this)
                    .setMessage(R.string.shizuku_lowest_api)
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .show()
            return
        }
        if (!PackageUtils.isShizukuClientAvailable(this)) {
            Snackbar.make(viewBind.rootLayout, getString(R.string.shizuku_not_installed), Snackbar.LENGTH_SHORT).show()
        } else {
            if (requestShizukuPermission(code)) {
                localDataRepo.setInstallMode(code)
                settingsAdapter?.updateInstallMode()
            }
        }
    }

    private fun systemPkgNameDialog() {
        val inputVB: LayoutInputPkgBinding by lazyBind()
        CustomizeDialog.getInstance(this)
                .setTitle(R.string.text_title_input)
                .setView(inputVB.root)
                .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                    inputVB.etSysPkgName.text.toString().also { pkgName ->
                        if (pkgName.isEmpty()) {
                            Snackbar.make(viewBind.rootLayout, getString(R.string.text_input_empty), Snackbar.LENGTH_SHORT).show()
                        } else {
                            localDataRepo.setSystemPkg(pkgName.trim())
                            viewBind.tvPkgName.text = pkgName.trim()
                        }
                    }
                }
                .setNegativeButton(R.string.dialog_btn_cancel, null)
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 || requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                localDataRepo.setInstallMode(requestCode)
                settingsAdapter?.updateInstallMode()
            } else {
                val str = when (requestCode) {
                    1 -> getString(R.string.shizuku_permission_request)
                    2 -> getString(R.string.icebox_permission_request)
                    else -> ""
                }
                Snackbar.make(viewBind.rootLayout, str, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

}
