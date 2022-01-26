package com.tokyonth.installer.activity

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tokyonth.installer.BuildConfig
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.SettingsAdapter
import com.tokyonth.installer.adapter.SettingsAdapter.*
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.databinding.ActivitySettingsBinding
import com.tokyonth.installer.utils.PackageUtils
import com.tokyonth.installer.utils.FileUtils
import com.tokyonth.installer.utils.ktx.lazyBind
import com.tokyonth.installer.utils.PermissionUtils.requestIceBoxPermission
import com.tokyonth.installer.utils.PermissionUtils.requestShizukuPermission
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.DialogUtils

class SettingsActivity : BaseActivity() {

    private val binding: ActivitySettingsBinding by lazyBind()

    private val local = LocalDataRepo.instance

    private var settingsAdapter: SettingsAdapter? = null

    override fun setBinding() = binding

    override fun initView() {
        binding.tvAppName.append("\uD83C\uDFEE")
        initViewStatus()
    }

    override fun initData() {
        local.setNotFirstBoot()
    }

    private fun initViewStatus() {
        binding.cbUseSysPkg.isChecked = local.isUseSystemPkg()
        binding.tvPkgName.text = local.getSystemPkg()

        settingsAdapter = SettingsAdapter(this@SettingsActivity).apply {
            setOnItemClickListener(object : OnItemClickListener {
                override fun onSwitch(pos: Int, bool: Boolean) {
                    when (pos) {
                        0 -> local.setShowPermission(bool)
                        1 -> local.setShowActivity(bool)
                        2 -> local.setDefaultSilent(bool)
                        3 -> local.setAutoDel(bool)
                        4 -> {
                            if (bool)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            local.setFollowSystem(bool)
                        }
                    }
                }

                override fun onClick(pos: Int) {
                    if (pos == 5) {
                        DialogUtils.installModeDialog(this@SettingsActivity) {
                            when (it) {
                                0 -> local.setInstallMode(it)
                                1 -> shizukuCheck(it)
                                2 -> iceBoxCheck(it)
                            }
                            settingsAdapter?.updateInstallMode()
                        }
                    }
                }
            })
        }
        binding.rvSettings.apply {
            layoutManager = GridLayoutManager(this@SettingsActivity, 1)
            adapter = settingsAdapter
        }

        val cacheSize = FileUtils.byteToString(
            FileUtils.getFileOrFolderSize(externalCacheDir)
        )
        binding.tvApkCache.text = string(R.string.text_apk_cache, cacheSize)
        binding.tvVersion.text =
            string(R.string.text_settings_version, BuildConfig.VERSION_NAME)
        binding.cbUseSysPkg.setOnCheckedChangeListener { _, isChecked ->
            local.setUseSystemPkg(
                isChecked
            )
        }
        binding.csSysPkg.setOnClickListener {
            DialogUtils.systemPkgNameDialog(this) {
                if (it.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.text_input_empty),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    local.setSystemPkg(it.trim())
                    binding.tvPkgName.text = it.trim()
                }
            }
        }
        binding.tvApkCache.setOnClickListener {
            FileUtils.deleteFolderFile(externalCacheDir?.path, true)
            Snackbar.make(
                binding.root,
                string(R.string.clean_complete),
                Snackbar.LENGTH_SHORT
            ).show()
            binding.tvApkCache.text = string(R.string.text_apk_cache, "0B")
        }
    }

    private fun iceBoxCheck(code: Int) {
        if (!PackageUtils.isIceBoxClientAvailable(this)) {
            Snackbar.make(
                binding.root,
                getString(R.string.not_install_icebox),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            if (requestIceBoxPermission(this, code)) {
                local.setInstallMode(code)
                settingsAdapter?.updateInstallMode()
            }
        }
    }

    private fun shizukuCheck(code: Int) {
        if (!PackageUtils.isShizukuClientAvailable(this)) {
            Snackbar.make(
                binding.root,
                getString(R.string.shizuku_not_installed),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            if (requestShizukuPermission(this, code)) {
                local.setInstallMode(code)
                settingsAdapter?.updateInstallMode()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 || requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                local.setInstallMode(requestCode)
                settingsAdapter?.updateInstallMode()
            } else {
                val str = when (requestCode) {
                    1 -> string(R.string.shizuku_permission_request)
                    2 -> string(R.string.icebox_permission_request)
                    else -> ""
                }
                Snackbar.make(binding.root, str, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

}
