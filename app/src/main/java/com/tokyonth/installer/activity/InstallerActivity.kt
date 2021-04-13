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
import android.text.TextUtils
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
import com.tokyonth.installer.utils.CommonUtil.bind
import com.tokyonth.installer.utils.CommonUtil.visibleOrGone
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

    private var installComplete = false
    private var showActivity = false
    private var showPerm = false

    private lateinit var vb: ActivityInstallerBinding
    private lateinit var vbInclude: ContentInstallerBinding
    private lateinit var vbDelInclude: LayoutDeleteContentBinding
    private lateinit var vbActInclude: LayoutActivityInfoBinding
    private lateinit var vbPermInclude: LayoutPermInfoBinding

    override fun initView(): ViewBinding? {
        vb = bind()
        vbInclude = vb.includeCt
        vbDelInclude = vbInclude.includeDel
        vbActInclude = vbInclude.includeAct
        vbPermInclude = vbInclude.includePerm
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

        apkSource = HelperTools.reflectGetReferrer(this).toString()
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

    private fun setViewStatus() {
        vb.includeCt.includePerm.cardPerm.visibleOrGone(get(Constants.SP_SHOW_PERMISSION, true))
        vb.includeCt.includeAct.cardAct.visibleOrGone(get(Constants.SP_SHOW_ACTIVITY, true))
        vb.includeCt.includeDel.sbAutoDel.isChecked = get(Constants.SP_AUTO_DELETE, false)
    }

    @SuppressLint("SetTextI18n")
    private fun initDetails(apkInfo: ApkInfoBean) {
        vb.ivAppIcon.setImageDrawable(apkInfo.icon)
        val bitmap = apkInfo.icon?.let { HelperTools.drawableToBitmap(it) }
        if (bitmap != null) {
            Palette.from(bitmap).generate { palette ->
                val vibrantSwatch = palette!!.lightVibrantSwatch
                val color: Int
                color = if (vibrantSwatch != null) {
                    HelperTools.colorBurn(vibrantSwatch.rgb)
                } else {
                    ContextCompat.getColor(this, R.color.colorAccent)
                }
                vb.tvVersionTips.setTextColor(color)

                val targetView = vb.appBarLayout
                val width = targetView.measuredWidth
                val height = targetView.measuredHeight
                val animator = ViewAnimationUtils.createCircularReveal(targetView, width / 2, height / 2, 0f, height.toFloat())
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        targetView.setBackgroundColor(color)
                    }
                })
                animator.duration = 500
                animator.start()
            }
        }

        vbInclude.fabInstall.visibility = View.VISIBLE
        vbPermInclude.cardPerm.visibility = View.VISIBLE
        vbActInclude.cardAct.visibility = View.VISIBLE
        apkFileName = apkInfo.appName!!
        apkFilePath = apkInfo.apkFile!!.path
        vb.tvAppName.text = apkInfo.appName
        vb.tvAppVersion.text = apkInfo.version

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
                text = HelperTools.checkVersion(this@InstallerActivity, apkInfo.versionCode, apkInfo.installedVersionCode)
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
        if (!TextUtils.isEmpty(apkInfo.packageName)) {
            initDetails(apkInfo)
            vbInclude.fabInstall.visibility = View.VISIBLE
            vbInclude.bottomAppBar.visibility = View.VISIBLE
        } else {
            val uri = intent.data
            var str: String? = null
            if (uri != null) {
                str = uri.toString()
            }
            vbInclude.tvInstallMsg.text = getString(R.string.parse_apk_failed, str)
        }
    }

    override fun onApkPreInstall(apkInfo: ApkInfoBean) {
        vbInclude.progressBar.visibility = View.VISIBLE
        vbPermInclude.cardPerm.visibility = View.GONE
        vbActInclude.cardAct.visibility = View.GONE
        vbInclude.fabInstall.isEnabled = false
        vbInclude.tvCancel.visibility = View.GONE
        vbInclude.tvSilently.visibility = View.GONE

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
            vbDelInclude.cardDel.visibility = View.VISIBLE
            showToast(getString(R.string.apk_installed, apkInfo.appName))
            if (get(Constants.SP_INSTALLED_VIBRATE, false)) {
                val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(800)
            }
            vb.tvAppName.text = getString(R.string.successful)
            vbInclude.fabInstall.apply {
                icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_send_24px)
                text = getString(R.string.open_app)
                isEnabled = true
            }
            vbInclude.tvCancel.text = getString(R.string.back)
        } else {
            vbInclude.fabInstall.apply {
                icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_clear_24px)
                text = getString(R.string.failed)
                isEnabled = true
            }
            vbInclude.tvCancel.text = getString(R.string.failed)

            if (get(Constants.SP_NEVER_SHOW_USE_SYSTEM_PKG, true)) {
                CustomizeDialog.getInstance(this)
                        .setTitle(R.string.dialog_text_title)
                        .setMessage(R.string.use_system_pkg)
                        .setPositiveButton(R.string.dialog_ok) { _: DialogInterface?, _: Int ->
                            HelperTools.startSystemPkgInstall(this, apkFilePath)
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
        vb.includeCt.progressBar.visibility = View.GONE
        installComplete = true
        progressDrawable.stop()
    }

    override fun onInstallLog(apkInfo: ApkInfoBean, logText: String) {
        vb.includeCt.tvInstallMsg.append(logText)
    }

    private fun isAutoDel() {
        if (get(Constants.SP_AUTO_DELETE, false) && vb.includeCt.tvCancel.text!! == getString(R.string.back)) {
            if (File(apkFilePath).delete()) {
                showToast(getString(R.string.apk_deleted, apkFileName))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_install -> {
                if (installComplete) {
                    startActivity(apkCommander.getApkInfo().packageName.let { it?.let { it1 -> packageManager.getLaunchIntentForPackage(it1) } })
                    isAutoDel()
                    finish()
                } else {
                    apkCommander.startInstall()
                }
            }
            R.id.tv_cancel -> {
                isAutoDel()
                finish()
            }
            R.id.tv_silently -> {
                val intent = Intent(this, SilentlyInstallActivity::class.java)
                intent.data = getIntent().data
                intent.putExtra(Constants.APK_SOURCE, apkSource)
                startActivity(intent)
                finish()
            }
            R.id.perm_ll -> {
                showPerm.also {
                    if (showPerm) {
                        vbPermInclude.ivPermArrow.setImageResource(R.drawable.ic_chevron_right_24px)
                    } else {
                        vbPermInclude.ivPermArrow.setImageResource(R.drawable.ic_expand_more_24px)
                    }
                    showPerm = !it
                    vbPermInclude.permRv.visibleOrGone(!it)
                }
            }
            R.id.act_ll -> {
                showActivity.also {
                    if (showActivity) {
                        vbActInclude.ivActArrow.setImageResource(R.drawable.ic_chevron_right_24px)
                    } else {
                        vbActInclude.ivActArrow.setImageResource(R.drawable.ic_expand_more_24px)
                    }
                    showActivity = !it
                    vbActInclude.actRv.visibleOrGone(!it)
                }
            }
            R.id.ib_night_mode -> {
                get(Constants.SP_NIGHT_MODE, false).also {
                    if (it) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    set(Constants.SP_NIGHT_MODE, !it)
                }
            }
            R.id.ib_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java), 200)
            R.id.ll_to_source -> HelperTools.toSelfSetting(this, apkSource)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (installComplete)
            setViewStatus()
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
