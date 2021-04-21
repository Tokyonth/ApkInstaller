package com.tokyonth.installer.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager

import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.TranslateAnimation
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding

import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.ActivityAdapter
import com.tokyonth.installer.view.CustomizeDialog
import com.tokyonth.installer.adapter.PermissionAdapter
import com.tokyonth.installer.adapter.RvScrollListener
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.install.CommanderCallback
import com.tokyonth.installer.bean.permissions.PermFullBean
import com.tokyonth.installer.databinding.*
import com.tokyonth.installer.utils.*
import com.tokyonth.installer.utils.SPUtils.get
import com.tokyonth.installer.utils.SPUtils.set
import com.tokyonth.installer.view.ProgressDrawable

import java.io.File
import java.util.ArrayList

class InstallerActivity : BaseActivity(), CommanderCallback, View.OnClickListener {

    private lateinit var permFullBeanArrayList: ArrayList<PermFullBean>
    private lateinit var actStringArrayList: ArrayList<String>
    private lateinit var progressDrawable: ProgressDrawable
    private lateinit var permAdapter: PermissionAdapter
    private lateinit var actAdapter: ActivityAdapter
    private lateinit var apkCommander: APKCommander

    private lateinit var apkFilePath: String
    private lateinit var apkFileName: String
    private lateinit var apkSource: String

    private lateinit var vb: ActivityInstallerBinding
    private lateinit var vbInclude: ContentInstallerBinding
    private lateinit var vbDelInclude: LayoutDeleteContentBinding
    private lateinit var vbActInclude: LayoutActivityInfoBinding
    private lateinit var vbPermInclude: LayoutPermInfoBinding

    private val settingsRequestCode = 201

    override fun initView(): ViewBinding {
        vb = bind()
        vbInclude = vb.includeCt
        vbDelInclude = vbInclude.includeDel
        vbActInclude = vbInclude.includeAct
        vbPermInclude = vbInclude.includePerm

        vbInclude.fabInstall.tag = false
        vbActInclude.actLl.tag = false
        vbPermInclude.permLl.tag = false

        return vb
    }

    override fun initData() {
        if (intent.data == null) {
            finish()
        } else {
            initListener()
            initViewDetail()
        }
    }

    private fun initListener() {
        vbInclude.fabInstall.setOnClickListener(this)
        vbInclude.tvCancel.setOnClickListener(this)
        vbInclude.tvSilently.setOnClickListener(this)
        vbActInclude.actLl.setOnClickListener(this)
        vbPermInclude.permLl.setOnClickListener(this)
        vbInclude.llToSource.setOnClickListener(this)
        vb.ibNightMode.setOnClickListener(this)
        vb.ibSettings.setOnClickListener(this)
    }

    private fun initViewDetail() {
        permFullBeanArrayList = ArrayList()
        actStringArrayList = ArrayList()

        apkSource = CommonUtils.reflectGetReferrer(this).toString()
        apkCommander = APKCommander(this, this, intent.data!!, apkSource)

        permAdapter = PermissionAdapter(permFullBeanArrayList, this)
        actAdapter = ActivityAdapter(actStringArrayList)

        vbPermInclude.permRv.apply {
            addOnScrollListener(RvScrollListener(vbInclude.fabInstall))
            layoutManager = LinearLayoutManager(this@InstallerActivity)
            adapter = permAdapter
        }
        vbActInclude.actRv.apply {
            addOnScrollListener(RvScrollListener(vbInclude.fabInstall))
            layoutManager = LinearLayoutManager(this@InstallerActivity)
            adapter = actAdapter
        }

        vbDelInclude.sbAutoDel.setOnCheckedChangeListener { _, isChecked -> set(Constants.SP_AUTO_DELETE, isChecked) }
        vbInclude.tvApkSource.text = getString(R.string.text_apk_source, AppPackageUtils.getAppNameByPackageName(this, apkSource))
        vbInclude.ivApkSource.setImageDrawable(AppPackageUtils.getAppIconByPackageName(this, apkSource))
    }

    @SuppressLint("SetTextI18n")
    private fun initApkDetails(apkInfo: ApkInfoBean) {
        apkInfo.icon.let {
            vb.ivAppIcon.setImageDrawable(it)
            CommonUtils.drawableToBitmap(it!!).let { it1 ->
                {
                    if (it1 != null) {
                        Palette.from(it1).generate { palette ->
                            val vibrantSwatch = palette!!.lightVibrantSwatch
                            val color: Int = if (vibrantSwatch != null) {
                                CommonUtils.colorBurn(vibrantSwatch.rgb)
                            } else {
                                ContextCompat.getColor(this, R.color.colorAccent)
                            }
                            vb.tvVersionTips.setTextColor(color)

                            val targetView = vb.appBarLayout
                            val width = targetView.measuredWidth
                            val height = targetView.measuredHeight
                            ViewAnimationUtils.createCircularReveal(targetView, width / 2, height / 2, 0f, height.toFloat()).apply {
                                addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationStart(animation: Animator) {
                                        super.onAnimationStart(animation)
                                        targetView.setBackgroundColor(color)
                                    }
                                })
                                duration = 500
                                start()
                            }
                        }
                    }
                }
            }
        }

        vbInclude.fabInstall.visibility = View.VISIBLE
        vbPermInclude.cardPerm.visibility = View.VISIBLE
        vbActInclude.cardAct.visibility = View.VISIBLE
        apkFileName = apkInfo.appName!!
        apkFilePath = apkInfo.apkFile!!.path
        vb.tvAppName.text = apkInfo.appName
        vb.tvAppVersion.text = apkInfo.version
        vb.ivAppIcon.setImageDrawable(apkInfo.icon)

        vbInclude.tvInstallMsg.text = resources.getString(R.string.info_pkg_name) + apkInfo.packageName + "\n" +
                resources.getString(R.string.info_apk_path) + apkFilePath + "\n" +
                resources.getString(R.string.text_size, FileIOUtils.byteToString(FileIOUtils.getFileSize(apkFilePath)))
        if (apkInfo.hasInstalledApp()) {
            vbInclude.tvInstallMsg.append("\n${resources.getString(R.string.info_installed_version)} ${apkInfo.installedVersion}")
            vb.tvVersionTips.apply {
                val translateAnimation = TranslateAnimation(0f, 0f, -200f, 0f)
                translateAnimation.duration = 500
                alpha = 0.70f
                animation = translateAnimation
                startAnimation(translateAnimation)
                visibility = View.VISIBLE
                text = CommonUtils.checkVersion(this@InstallerActivity, apkInfo.versionCode, apkInfo.installedVersionCode)
            }
        }

        get(Constants.SP_SHOW_PERMISSION, true).also {
            if (it) {
                if (apkInfo.permissions != null && apkInfo.permissions!!.isNotEmpty()) {
                    for (i in apkInfo.permissions!!.indices) {
                        if (get(Constants.SP_SHOW_PERMISSION, true)) {
                            permFullBeanArrayList.add(PermFullBean(apkInfo.permissions!![i],
                                    apkCommander.getPermInfo().permissionGroup?.get(i),
                                    apkCommander.getPermInfo().permissionDescription?.get(i),
                                    apkCommander.getPermInfo().permissionLabel?.get(i)))
                        }
                    }
                }
            }
            vbPermInclude.cardPerm.visibleOrGone(it)
        }
        get(Constants.SP_SHOW_ACTIVITY, true).also {
            if (it) {
                if (apkInfo.activities != null && apkInfo.activities!!.isNotEmpty()) {
                    actStringArrayList.addAll(apkInfo.activities!!)
                }
            }
            vbActInclude.cardAct.visibleOrGone(it)
        }

        permAdapter.notifyDataSetChanged()
        actAdapter.notifyDataSetChanged()

        vbPermInclude.tvPermQuantity.text = getString(R.string.app_permissions, permAdapter.itemCount.toString())
        vbActInclude.tvActQuantity.text = getString(R.string.app_act, actAdapter.itemCount.toString())
    }

    override fun onStartParseApk(uri: Uri) {
        vbInclude.fabInstall.visibility = View.GONE
        vbInclude.bottomAppBar.visibility = View.GONE
    }

    override fun onApkParsed(apkInfo: ApkInfoBean) {
        if (apkInfo.packageName?.isNotEmpty()!!) {
            initApkDetails(apkInfo)
            vbInclude.fabInstall.visibility = View.VISIBLE
            vbInclude.bottomAppBar.visibility = View.VISIBLE
        } else {
            intent.data.let {
                vbInclude.tvInstallMsg.text = getString(R.string.parse_apk_failed, it.toString())
            }
        }
    }

    override fun onApkPreInstall(apkInfo: ApkInfoBean) {
        vbInclude.pbInstalling.visibility = View.VISIBLE
        vbPermInclude.cardPerm.visibility = View.GONE
        vbActInclude.cardAct.visibility = View.GONE
        vbInclude.tvCancel.visibility = View.GONE
        vbInclude.tvSilently.visibility = View.GONE
        vbInclude.fabInstall.isEnabled = false

        progressDrawable = ProgressDrawable()
        progressDrawable.putColor(Color.WHITE)
        progressDrawable.animatorDuration(1500)
        progressDrawable.start()
        vbInclude.fabInstall.icon = progressDrawable
        vbInclude.tvInstallMsg.text = ""
        vb.tvAppName.text = getString(R.string.installing)
    }

    @Suppress("DEPRECATION")
    override fun onApkInstalled(apkInfo: ApkInfoBean, resultCode: Int) {
        if (resultCode == 0) {
            showToast(getString(R.string.apk_installed, apkInfo.appName))
            vbDelInclude.cardDel.visibility = View.VISIBLE
            if (get(Constants.SP_INSTALLED_VIBRATE, false)) {
                (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).apply {
                    vibrate(800)
                }
            }

            vbInclude.fabInstall.apply {
                icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_send_24px)
                text = getString(R.string.open_app)
                isEnabled = true
            }
            vb.tvAppName.text = getString(R.string.successful)
            vbInclude.tvCancel.text = getString(R.string.back)
        } else {
            vbInclude.fabInstall.apply {
                icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_clear_24px)
                text = getString(R.string.failed)
                isEnabled = false
            }
            vbInclude.tvCancel.text = getString(R.string.exit_app)

            if (get(Constants.SP_NEVER_SHOW_USE_SYSTEM_PKG, true)) {
                CustomizeDialog.getInstance(this)
                        .setTitle(R.string.dialog_text_title)
                        .setMessage(R.string.use_system_pkg)
                        .setPositiveButton(R.string.dialog_ok) { _: DialogInterface?, _: Int ->
                            CommonUtils.startSystemPkgInstall(this, apkFilePath)
                            finish()
                        }
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
                        .setNeutralButton(R.string.never_show) { _: DialogInterface?, _: Int ->
                            set(Constants.SP_NEVER_SHOW_USE_SYSTEM_PKG, false)
                        }
                        .setCancelable(false).create().show()
            }

        }
        vbInclude.tvCancel.visibility = View.VISIBLE
        vbInclude.pbInstalling.visibility = View.GONE
        vbInclude.fabInstall.tag = true
        progressDrawable.stop()
    }

    override fun onInstallLog(apkInfo: ApkInfoBean, logText: String) {
        vb.includeCt.tvInstallMsg.append(logText)
    }

    private fun isAutoDel() {
        if (get(Constants.SP_AUTO_DELETE, false) && vb.includeCt.tvCancel.text!! == getString(R.string.back)) {
            if (File(apkFilePath).delete()) {
                showToast(getString(R.string.apk_deleted, apkFileName))
                finish()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_install -> {
                vbInclude.fabInstall.tag.let {
                    if (it as Boolean) {
                        apkCommander.getApkInfo().packageName.let { it1 ->
                            {
                                if (it1 != null) {
                                    startActivity(packageManager.getLaunchIntentForPackage(it1))
                                    isAutoDel()
                                }
                            }
                        }
                        finish()
                    } else {
                        apkCommander.startInstall()
                    }
                }
            }
            R.id.tv_cancel -> isAutoDel()
            R.id.tv_silently -> {
                val intent = Intent(this, SilentlyInstallActivity::class.java).apply {
                    data = intent.data
                    putExtra(Constants.APK_SOURCE, apkSource)
                }
                startActivity(intent)
                finish()
            }
            R.id.perm_ll -> {
                vbPermInclude.permLl.tag.let {
                    if (it as Boolean) {
                        vbPermInclude.ivPermArrow.setImageResource(R.drawable.ic_chevron_right_24px)
                    } else {
                        vbPermInclude.ivPermArrow.setImageResource(R.drawable.ic_expand_more_24px)
                    }
                    vbPermInclude.permRv.visibleOrGone(!it)
                }
            }
            R.id.act_ll -> {
                vbActInclude.actLl.tag.let {
                    if (it as Boolean) {
                        vbActInclude.ivActArrow.setImageResource(R.drawable.ic_chevron_right_24px)
                    } else {
                        vbActInclude.ivActArrow.setImageResource(R.drawable.ic_expand_more_24px)
                    }
                    vbActInclude.actRv.visibleOrGone(!it)
                }
            }
            R.id.ib_night_mode -> {
                get(Constants.SP_NIGHT_MODE, false).let {
                    if (it) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    set(Constants.SP_NIGHT_MODE, !it)
                }
            }
            R.id.ib_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java), settingsRequestCode)
            R.id.ll_to_source -> CommonUtils.toSelfSetting(this, apkSource)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(vbInclude.fabInstall.tag as Boolean) && requestCode == settingsRequestCode) {
            vbPermInclude.cardPerm.visibleOrGone(get(Constants.SP_SHOW_PERMISSION, true))
            vbActInclude.cardAct.visibleOrGone(get(Constants.SP_SHOW_ACTIVITY, true))
        }
        vbDelInclude.sbAutoDel.isChecked = get(Constants.SP_AUTO_DELETE, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::apkCommander.isInitialized && apkCommander.getApkInfo().isFakePath) {
            if (!apkCommander.getApkInfo().apkFile?.delete()!!) {
                Log.e("InstallerActivity", "failed to delete！")
            }
        }
    }

}
