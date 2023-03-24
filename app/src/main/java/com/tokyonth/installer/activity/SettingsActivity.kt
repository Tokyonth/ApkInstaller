package com.tokyonth.installer.activity

import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchingnow.icebox.sdk_client.IceBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.tokyonth.installer.BuildConfig
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.SettingsAdapter
import com.tokyonth.installer.adapter.SettingsAdapter.*
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.databinding.ActivitySettingsBinding
import com.tokyonth.installer.databinding.LayoutInputPkgBinding
import com.tokyonth.installer.utils.PackageUtils
import com.tokyonth.installer.utils.PermissionHelper
import com.tokyonth.installer.utils.ktx.*
import rikka.shizuku.ShizukuProvider

class SettingsActivity : BaseActivity() {

    private val binding: ActivitySettingsBinding by lazyBind()

    private var settingsAdapter = SettingsAdapter()

    override fun setBinding() = binding

    override fun initView() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        initViewStatus()
    }

    override fun initData() {
        super.initData()
        SPDataManager.instance.setNotFirstBoot()
        permissionHelper = PermissionHelper(this)
        permissionHelper?.registerCallback { all, code ->
            if (all) {
                SPDataManager.instance.setInstallMode(code)
                settingsAdapter.updateInstallMode()
            } else {
                val str = when (code) {
                    1 -> string(R.string.shizuku_permission_request)
                    2 -> string(R.string.icebox_permission_request)
                    else -> ""
                }
                Snackbar.make(binding.root, str, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViewStatus() {
        val local = SPDataManager.instance
        binding.cbUseSysPkg.isChecked = local.isUseSystemPkg()
        binding.tvPkgName.text = local.getSystemPkg()

        settingsAdapter.apply {
            setOnItemActionListener(object : OnItemActionListener {
                override fun onSwitch(pos: Int, bool: Boolean) {
                    when (pos) {
                        0 -> local.setShowPermission(bool)
                        1 -> local.setShowActivity(bool)
                        2 -> local.setDefaultSilent(bool)
                        3 -> local.setAutoDel(bool)
                        4 -> {
                            if (bool) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            }
                            local.setFollowSystem(bool)
                        }
                    }
                }

                override fun onClick(pos: Int) {
                    if (pos == 5) {
                        MaterialAlertDialogBuilder(this@SettingsActivity)
                            .setSingleChoiceItems(
                                R.array.install_mode_arr,
                                SPDataManager.instance.getInstallMode()
                            ) { dialog, which ->
                                when (which) {
                                    0 -> local.setInstallMode(which)
                                    1 -> shizukuCheck(which)
                                    2 -> iceBoxCheck(which)
                                }
                                settingsAdapter.updateInstallMode()
                                dialog.dismiss()
                            }
                            .setNegativeButton(R.string.dialog_btn_cancel, null)
                            .show()
                    }
                }
            })
        }
        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = settingsAdapter
        }

        val cacheSize = externalCacheDir?.fileOrFolderSize()?.toMemorySize() ?: 0
        binding.tvApkCache.text = string(R.string.text_apk_cache, cacheSize)
        binding.tvAppVersion.text = string(R.string.text_settings_version, BuildConfig.VERSION_NAME)
        binding.cbUseSysPkg.setOnCheckedChangeListener { _, isChecked ->
            local.setUseSystemPkg(isChecked)
        }
        binding.csSysPkg.setOnClickListener {
            val pkgBinding = LayoutInputPkgBinding.inflate(LayoutInflater.from(this))
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.text_title_input)
                .setView(pkgBinding.root)
                .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                    val name = pkgBinding.etSystemPkg.text.toString()
                    if (name.isEmpty()) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.text_input_empty),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        local.setSystemPkg(name.trim())
                        binding.tvPkgName.text = name.trim()
                    }
                }
                .setNegativeButton(R.string.dialog_btn_cancel, null)
                .show()
        }
        binding.tvApkCache.setOnClickListener {
            externalCacheDir?.deleteFolderFile(true)
            Snackbar.make(
                binding.root,
                string(R.string.clean_complete),
                Snackbar.LENGTH_SHORT
            ).show()
            binding.tvApkCache.text = string(R.string.text_apk_cache, "0B")
        }
    }

    private fun iceBoxCheck(requestCode: Int) {
        if (!PackageUtils.isIceBoxClientAvailable(this)) {
            Snackbar.make(
                binding.root,
                getString(R.string.not_install_icebox),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            permissionHelper?.start(arrayOf(IceBox.SDK_PERMISSION), requestCode)
        }
    }

    private fun shizukuCheck(requestCode: Int) {
        if (!PackageUtils.isShizukuClientAvailable(this)) {
            Snackbar.make(
                binding.root,
                getString(R.string.shizuku_not_installed),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            permissionHelper?.start(arrayOf(ShizukuProvider.PERMISSION), requestCode)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionHelper?.dispose()
    }

}
