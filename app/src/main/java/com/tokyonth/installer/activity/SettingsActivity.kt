package com.tokyonth.installer.activity

import android.os.Bundle
import android.text.Editable

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.view.View
import android.widget.TextView

import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.kyleduo.switchbutton.SwitchButton
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.SettingsAdapter
import com.tokyonth.installer.adapter.SettingsAdapter.*
import com.tokyonth.installer.utils.StatusBarColorUtils
import com.tokyonth.installer.widget.CustomizeDialog
import com.tokyonth.installer.bean.SettingsBean
import com.tokyonth.installer.utils.GetAppInfoUtils
import com.tokyonth.installer.utils.FileUtils
import com.tokyonth.installer.utils.SPUtils

import java.io.File
import java.util.ArrayList
import java.util.Objects

class SettingsActivity : AppCompatActivity() {

    private var switchButtonUseSysPkg: SwitchButton? = null
    private var textViewSysPkgName: TextView? = null
    private var textViewApkCache: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarColorUtils.setStatusBarDarkIcon(this,
                !(SPUtils.getData(Constants.SP_NIGHT_MODE, false) as Boolean))
        setContentView(R.layout.activity_settings)
        initViewData()
        initSettings()
    }

    private fun initViewData() {
        val settingsBeanArrayList = ArrayList<SettingsBean>()
        settingsBeanArrayList.add(SettingsBean(getString(R.string.title_show_perm),
                getString(R.string.summary_show_perm),
                R.drawable.ic_verified_user_24px, resources.getColor(R.color.color0)))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.title_show_act),
                getString(R.string.summary_show_act),
                R.drawable.ic_widgets_24px, resources.getColor(R.color.color1)))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.vibrate),
                getString(R.string.install_vibrate),
                R.drawable.ic_waves_24px, resources.getColor(R.color.color2)))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.use_shizuku),
                getString(R.string.use_shizuku_sub),
                R.drawable.ic_extension_24px, resources.getColor(R.color.color4)))
        settingsBeanArrayList.add(SettingsBean(getString(R.string.freeze_app_list),
                getString(R.string.freeze_app_list_sub),
                R.drawable.ic_all_inbox_24px, resources.getColor(R.color.color3)))

        val adapter = SettingsAdapter(this, settingsBeanArrayList)
        val rvSettings = findViewById<RecyclerView>(R.id.rv_settings_item)
        rvSettings.layoutManager = LinearLayoutManager(this)
        rvSettings.adapter = adapter
        adapter.setOnItemClick(onItemSwitchClick = object : OnItemSwitchClick {
            override fun onItemClick(view: View, pos: Int, bool: Boolean) {
                when (pos) {
                    0 -> SPUtils.putData(Constants.SP_SHOW_PERM, bool)
                    1 -> SPUtils.putData(Constants.SP_SHOW_ACT, bool)
                    2 -> SPUtils.putData(Constants.SP_VIBRATE, bool)
                    3 -> SPUtils.putData(Constants.SP_IS_SHIZUKU_MODE, bool)
                }
            }
        })

        findViewById<TextView>(R.id.tv_version).append(GetAppInfoUtils.getVersionName(this))

        textViewApkCache = findViewById(R.id.tv_apk_cache)
        textViewSysPkgName = findViewById(R.id.tv_pkg_name)
        switchButtonUseSysPkg = findViewById(R.id.cb_use_sys_pkg)
        switchButtonUseSysPkg!!.setOnCheckedChangeListener { _, isChecked -> SPUtils.putData(Constants.SP_USE_SYS_PKG, isChecked) }

        val cacheSize = FileUtils.byteToString(FileUtils.getFileOrFolderSize(File(Constants.CACHE_APK_DIR)))
        textViewApkCache!!.text = getString(R.string.text_apk_cache, cacheSize)

        findViewById<View>(R.id.card_apk_cache).setOnClickListener {
            FileUtils.deleteFolderFile(Constants.CACHE_APK_DIR, true)
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
