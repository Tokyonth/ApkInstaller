package com.tokyonth.installer.activity

import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding

import com.google.android.material.snackbar.Snackbar
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.SettingsAdapter
import com.tokyonth.installer.adapter.SettingsAdapter.*
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.view.CustomizeDialog
import com.tokyonth.installer.bean.SettingsBean
import com.tokyonth.installer.databinding.ActivitySettingsBinding
import com.tokyonth.installer.databinding.LayoutInputPkgBinding
import com.tokyonth.installer.utils.AppPackageUtils
import com.tokyonth.installer.utils.FileIOUtils
import com.tokyonth.installer.utils.SPUtils.get
import com.tokyonth.installer.utils.SPUtils.set
import com.tokyonth.installer.utils.bind

import java.util.ArrayList

class SettingsActivity : BaseActivity() {

    private val settingsList = ArrayList<SettingsBean>()

    private lateinit var vb: ActivitySettingsBinding

    override fun initView(): ViewBinding {
        vb = bind()
        return vb
    }

    override fun initData() {
        settingsList.add(SettingsBean(getString(R.string.title_show_perm), getString(R.string.summary_show_perm),
                R.drawable.ic_verified_user_24px, ContextCompat.getColor(this, R.color.color0),
                get(Constants.SP_SHOW_PERMISSION, true)))
        settingsList.add(SettingsBean(getString(R.string.title_show_act), getString(R.string.summary_show_act),
                R.drawable.ic_widgets_24px, ContextCompat.getColor(this, R.color.color1),
                get(Constants.SP_SHOW_ACTIVITY, true)))
        settingsList.add(SettingsBean(getString(R.string.vibrate), getString(R.string.install_vibrate),
                R.drawable.ic_waves_24px, ContextCompat.getColor(this, R.color.color2),
                get(Constants.SP_INSTALLED_VIBRATE, false)))
        settingsList.add(SettingsBean(getString(R.string.follow_system_night_mode), getString(R.string.follow_system_night_mode_sub),
                R.drawable.ic_brightness_6_24px, ContextCompat.getColor(this, R.color.color5),
                get(Constants.SP_NIGHT_FOLLOW_SYSTEM, false)))
        settingsList.add(SettingsBean(getString(R.string.install_mode), getString(R.string.install_mode_sub),
                R.drawable.ic_move_to_inbox_24px,
                ContextCompat.getColor(this, R.color.color4), false))
        // settingsBeanArrayList.add(SettingsBean(getString(R.string.freeze_app_list),
        //       getString(R.string.freeze_app_list_sub),
        //       R.drawable.ic_all_inbox_24px, ContextCompat.getColor(this, R.color.color3), false))
        AppPackageUtils.getVersionName(this).let {
            vb.includeCt.tvVersion.append(it)
            initViewStatusData()
            initSettingsStatus()
        }
    }

    private fun initViewStatusData() {
        vb.includeCt.rvSettingsItem.apply {
            layoutManager = GridLayoutManager(this@SettingsActivity, 1)
            adapter = SettingsAdapter(this@SettingsActivity, settingsList).apply {
                setOnItemClick { _, pos, bool ->
                    when (pos) {
                        0 -> set(Constants.SP_SHOW_PERMISSION, bool)
                        1 -> set(Constants.SP_SHOW_ACTIVITY, bool)
                        2 -> set(Constants.SP_INSTALLED_VIBRATE, bool)
                        3 -> {
                            if (bool)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            set(Constants.SP_NIGHT_FOLLOW_SYSTEM, bool)
                        }
                    }
                }
            }
        }

        vb.includeCt.cardPkg.setOnClickListener {
            LayoutInputPkgBinding.bind(View.inflate(this, R.layout.layout_input_pkg, null)).let {
                CustomizeDialog.getInstance(this)
                        .setTitle(R.string.text_title_input)
                        .setView(it.root)
                        .setPositiveButton(R.string.dialog_ok) { _, _ ->
                            it.etSysPkgName.text.toString().also { it1 ->
                                run {
                                    if (it1.isEmpty()) {
                                        Snackbar.make(vb.coordinatorLayout, getString(R.string.text_input_empty), Snackbar.LENGTH_SHORT).show()
                                    } else {
                                        set(Constants.SYS_PKG_NAME_KEY, it1.trim())
                                        vb.includeCt.tvPkgName.text = it1.trim()
                                    }
                                }
                            }
                        }
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
                        .setCancelable(false).create().show()
            }
        }

        val cacheSize = FileIOUtils.byteToString(FileIOUtils.getFileOrFolderSize(cacheDir))
        vb.includeCt.cbUseSysPkg.setOnCheckedChangeListener { _, isChecked -> set(Constants.SP_USE_SYS_PKG, isChecked) }
        vb.includeCt.tvApkCache.text = getString(R.string.text_apk_cache, cacheSize)
        vb.includeCt.cardApkCache.setOnClickListener {
            FileIOUtils.deleteFolderFile(cacheDir.path, true)
            Snackbar.make(vb.coordinatorLayout, getString(R.string.text_apk_cache_complete), Snackbar.LENGTH_SHORT).show()
            vb.includeCt.tvApkCache.text = getString(R.string.text_apk_cache, "0B")
        }
    }

    private fun initSettingsStatus() {
        get(Constants.SP_USE_SYS_PKG, false).let {
            vb.includeCt.cbUseSysPkg.isChecked = it
        }
        get(Constants.SYS_PKG_NAME_KEY, Constants.DEFAULT_SYS_PKG_NAME).let {
            vb.includeCt.tvPkgName.text = it
        }
    }

}
