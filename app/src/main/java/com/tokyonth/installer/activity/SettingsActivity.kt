package com.tokyonth.installer.activity

import android.os.Bundle
import android.text.Editable

import androidx.recyclerview.widget.RecyclerView

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager

import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.kyleduo.switchbutton.SwitchButton
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.SettingsAdapter
import com.tokyonth.installer.adapter.SettingsAdapter.*
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.widget.CustomizeDialog
import com.tokyonth.installer.bean.SettingsBean
import com.tokyonth.installer.utils.GetAppInfoUtils
import com.tokyonth.installer.utils.FileUtils
import com.tokyonth.installer.utils.SPUtils

import java.util.ArrayList
import java.util.Objects

class SettingsActivity : BaseActivity() {

    private var switchButtonUseSysPkg: SwitchButton? = null
    private var textViewSysPkgName: TextView? = null
    private var textViewApkCache: TextView? = null

    override fun setActivityView(): Int {
        return R.layout.activity_settings
    }

    override fun initActivity(savedInstanceState: Bundle?) {
        initViewData()
        initSettings()
    }

    private fun initViewData() {
        val settingsBeanArrayList = ArrayList<SettingsBean>()
        settingsBeanArrayList.add(SettingsBean(getString(R.string.title_show_perm),
                getString(R.string.summary_show_perm),
                R.drawable.ic_verified_user_24px, ContextCompat.getColor(this, R.color.color0),
                SPUtils.getData(Constants.SP_SHOW_PERMISSION, true) as Boolean))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.title_show_act),
                getString(R.string.summary_show_act),
                R.drawable.ic_widgets_24px, ContextCompat.getColor(this, R.color.color1),
                SPUtils.getData(Constants.SP_SHOW_ACTIVITY, true) as Boolean))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.vibrate),
                getString(R.string.install_vibrate),
                R.drawable.ic_waves_24px, ContextCompat.getColor(this, R.color.color2),
                SPUtils.getData(Constants.SP_INSTALLED_VIBRATE, false) as Boolean))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.follow_system_night_mode),
                getString(R.string.follow_system_night_mode_sub),
                R.drawable.ic_brightness_6_24px, ContextCompat.getColor(this, R.color.color5),
                SPUtils.getData(Constants.SP_NIGHT_FOLLOW_SYSTEM, false) as Boolean))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.install_mode),
                getString(R.string.install_mode_sub),
                R.drawable.ic_move_to_inbox_24px,
                ContextCompat.getColor(this, R.color.color4), false))
        // settingsBeanArrayList.add(SettingsBean(getString(R.string.freeze_app_list),
        //       getString(R.string.freeze_app_list_sub),
        //       R.drawable.ic_all_inbox_24px, ContextCompat.getColor(this, R.color.color3), false))

        val adapter = SettingsAdapter(this, settingsBeanArrayList)
        val rvSettings = findViewById<RecyclerView>(R.id.rv_settings_item)
        rvSettings.layoutManager = GridLayoutManager(this, 1)
        rvSettings.adapter = adapter.apply {
            setOnItemClick(onItemSwitchClick = object : OnItemSwitchClick {
                override fun onItemClick(view: View, pos: Int, bool: Boolean) {
                    when (pos) {
                        0 -> SPUtils.putData(Constants.SP_SHOW_PERMISSION, bool)
                        1 -> SPUtils.putData(Constants.SP_SHOW_ACTIVITY, bool)
                        2 -> SPUtils.putData(Constants.SP_INSTALLED_VIBRATE, bool)
                        3 -> {
                            if (bool)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            SPUtils.putData(Constants.SP_NIGHT_FOLLOW_SYSTEM, bool)
                        }
                    }
                }
            })
        }

        findViewById<TextView>(R.id.tv_version).append(GetAppInfoUtils.getVersionName(this))

        textViewApkCache = findViewById(R.id.tv_apk_cache)
        textViewSysPkgName = findViewById(R.id.tv_pkg_name)
        switchButtonUseSysPkg = findViewById(R.id.cb_use_sys_pkg)
        switchButtonUseSysPkg!!.setOnCheckedChangeListener { _, isChecked -> SPUtils.putData(Constants.SP_USE_SYS_PKG, isChecked) }

        val cacheSize = FileUtils.byteToString(FileUtils.getFileOrFolderSize(cacheDir))
        textViewApkCache!!.text = getString(R.string.text_apk_cache, cacheSize)

        findViewById<View>(R.id.card_apk_cache).setOnClickListener {
            FileUtils.deleteFolderFile(cacheDir.path, true)
            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.text_apk_cache_complete), Snackbar.LENGTH_SHORT).show()
            textViewApkCache!!.text = getString(R.string.text_apk_cache, cacheSize)
        }

        findViewById<View>(R.id.card_pkg).setOnClickListener {
            val inView = View.inflate(this@SettingsActivity, R.layout.layout_input_pkg, null)
            val edit = inView.findViewById<TextInputEditText>(R.id.et_sys_pkg_name)
            CustomizeDialog.getInstance(this)
                    .setTitle(R.string.text_title_input)
                    .setView(inView)
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        val str = Objects.requireNonNull<Editable>(edit.text).toString().trim { it <= ' ' }
                        if (str.isEmpty()) {
                            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.text_input_empty), Snackbar.LENGTH_SHORT).show()
                        } else {
                            SPUtils.putData(Constants.SYS_PKG_NAME, str)
                            textViewSysPkgName!!.text = str
                        }
                    }
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .setCancelable(false).create().show()
        }
    }

    private fun initSettings() {
        val useSysPkg = SPUtils.getData(Constants.SP_USE_SYS_PKG, false) as Boolean
        switchButtonUseSysPkg!!.isChecked = useSysPkg
        if (SPUtils.getData(Constants.SYS_PKG_NAME, Constants.SYS_PKG_NAME) != Constants.SYS_PKG_NAME) {
            textViewSysPkgName!!.text = SPUtils.getData(Constants.SYS_PKG_NAME, Constants.SYS_PKG_NAME)!!.toString()
        }
    }

}
