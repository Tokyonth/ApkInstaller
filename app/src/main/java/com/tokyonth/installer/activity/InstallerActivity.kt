package com.tokyonth.installer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.resources.MaterialAttributes
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.model.InstallerViewModel
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.databinding.*
import com.tokyonth.installer.install.InstallerServer
import com.tokyonth.installer.utils.*
import com.tokyonth.installer.utils.ktx.*
import com.tokyonth.installer.view.ProgressDrawable
import java.io.File
import java.util.*

class InstallerActivity : BaseActivity() {

    private val binding: ActivityInstallerBinding by lazyBind()

    private val model: InstallerViewModel by viewModels()

    private var progressDrawable: ProgressDrawable? = null

    private var apkInfo: ApkInfoEntity? = null

    private var isInstalling = false

    private var isInstalled = false

    private var apkSource = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        if (SPDataManager.instance.isDefaultSilent()) {
            startSilentlyInstall()
            return
        }
        super.onCreate(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (!isInstalling) {
            intent?.let { initApkUri(it) }
        }
    }

    override fun setBinding() = binding

    override fun initView() {
        binding.loadingView.showLoading()
        binding.fabInstall.click {
            startInstallFun()
        }
        binding.tvSilently.click {
            startSilentlyInstall()
        }
        binding.tvCancel.click {
            if (SPDataManager.instance.isAutoDel()) {
                File(apkInfo!!.filePath).delete()
                toast(string(R.string.apk_deleted, apkInfo!!.appName))
            }
            finish()
        }
        binding.sbAutoDel.isChecked = SPDataManager.instance.isAutoDel()
    }

    override fun initData() {
        apkSource = AppHelper.reflectGetReferrer(this).orEmpty()
        initLiveDataObs()
        initApkUri(intent)
    }

    private fun initApkUri(mIntent: Intent) {
        mIntent.data.let {
            if (it == null) {
                finish()
            } else {
                permissionHelper?.registerCallback { all, _ ->
                    if (all) {
                        model.startParse(it, apkSource)
                    } else {
                        toast(string(R.string.no_permissions))
                        finish()
                    }
                }
                permissionHelper?.startData(apkSource)
            }
        }
    }

    private fun initLiveDataObs() {
        model.apkParsedLiveData.observe(this) {
            apkInfo = it
            onApkParsed()
        }
        model.apkParsedFailedLiveData.observe(this) {
            onApkParsedFailed(it)
        }
        model.apkPreInstallLiveData.observe(this) {
            onApkPreInstall()
        }
        model.apkInstallLogLiveData.observe(this) {
            onInstallLog(it)
        }
        model.apkInstalledLiveData.observe(this) {
            onApkInstalled(it)
        }
    }

    private fun onApkParsed() {
        binding.loadingView.showContentView()
        binding.cardLog.visibleOrGone(false)
        if (!apkInfo?.packageName.isNullOrEmpty()) {
            changeViewStatus(true)
            initApkDetails(apkInfo!!)
        }
    }

    private fun onApkParsedFailed(msg: String) {
        val error = buildString {
            append(string(R.string.parse_apk_failed, intent.data))
            append("\n")
            append(msg)
        }
        binding.loadingView.showErrorView(error)
        //binding.tvInstallLog.append(msg)
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    private fun onApkPreInstall() {
        changeViewStatus(false)
        val mColor =
            MaterialAttributes.resolve(this, com.google.android.material.R.attr.colorControlNormal)
        val accentColor = color(mColor!!.resourceId)
        progressDrawable = ProgressDrawable().apply {
            putColor(accentColor)
            animatorDuration(1500)
            start()
        }

        binding.run {
            cardLog.visibleOrGone(true)
            tvInstallLog.text = "Start install..."
            fabInstall.setImageDrawable(progressDrawable)
            layoutHeader.setTitle(string(R.string.installing))
        }
    }

    private fun onApkInstalled(installed: Boolean) {
        if (installed) {
            installSuccess(apkInfo!!)
        } else {
            installFailure(apkInfo!!)
        }

        binding.run {
            tvCancel.visibility = View.VISIBLE
            llDel.visibility = View.VISIBLE
            layoutHeader.isEnabled = true
        }

        progressDrawable?.stop()
        isInstalling = false
        isInstalled = true

        if (apkInfo!!.isFakePath) {
            File(apkInfo!!.filePath).delete()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initApkDetails(apkInfo: ApkInfoEntity) {
        binding.layoutHeader.setAppInfo(apkInfo)
        binding.sbAutoDel.setOnCheckedChangeListener { _, isChecked ->
            SPDataManager.instance.setAutoDel(isChecked)
        }

        val apkSize = File(apkInfo.filePath).fileOrFolderSize().toMemorySize()
        val infoBuilder = buildString {
            append(string(R.string.info_pkg_name, apkInfo.packageName))
            append("\n")
            append(string(R.string.info_apk_path, apkInfo.filePath))
            append("\n")
            append(string(R.string.text_apk_file_size, apkSize))
            if (apkInfo.isHasInstalledApp) {
                append("\n")
                append(string(R.string.info_installed_version, apkInfo.installedVersion))
            }
        }
        binding.tvApkOverview.text = infoBuilder

        loadChipApk(apkInfo)
        loadingComposeInfo(apkInfo)
    }

    private fun loadChipApk(apkInfo: ApkInfoEntity) {
        val sIcon = PackageUtils.getAppIconByPackageName(this, apkSource)
        val sName = PackageUtils.getAppNameByPackageName(this, apkSource)
        val chipSource = Chip(this).apply {
            this.chipIcon = sIcon
            this.text = string(R.string.text_apk_source, sName)
            this.setEnsureMinTouchTargetSize(false)
            this.setOnClickListener {
                AppHelper.toSelfSetting(this@InstallerActivity, apkSource)
            }
        }
        val chipAbi = Chip(this).apply {
            this.setChipIconResource(R.drawable.round_memory_24)
            this.setEnsureMinTouchTargetSize(false)
            this.text =
                if (apkInfo.isArm64) string(R.string.apk_so_64) else string(R.string.apk_so_32)
        }

        binding.cipGroup.run {
            removeAllViews()
            addView(chipSource)
            addView(chipAbi)
            if (apkInfo.isXposed) {
                val chipXp = Chip(this@InstallerActivity).apply {
                    this.setChipIconResource(R.drawable.round_construction_24)
                    this.setEnsureMinTouchTargetSize(false)
                    this.text = string(R.string.apk_xposed)
                }
                addView(chipXp)
            }
            if (apkInfo.isFakePath) {
                val chipFake = Chip(this@InstallerActivity).apply {
                    this.setChipIconResource(R.drawable.round_attachment_24)
                    this.setEnsureMinTouchTargetSize(false)
                    this.text = string(R.string.apk_fake_path)
                }
                addView(chipFake)
            }
            if (apkInfo.isHasInstalledApp) {
                val chipVersion = Chip(this@InstallerActivity).apply {
                    this.setChipIconResource(R.drawable.round_beenhere_24)
                    this.setEnsureMinTouchTargetSize(false)
                    this.text = getVersionTip(apkInfo)
                }
                addView(chipVersion)
            }
        }
    }

    private fun getVersionTip(apkInfo: ApkInfoEntity): String {
        return when {
            apkInfo.version == apkInfo.installedVersion -> string(R.string.apk_equal_version)
            apkInfo.version > apkInfo.installedVersion -> string(R.string.apk_new_version)
            else -> {
                if (!SPDataManager.instance.isNeverShowTip()) {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.low_version_tip)
                        .setPositiveButton(R.string.dialog_btn_ok, null)
                        .setNegativeButton(R.string.dialog_no_longer_prompt) { _, _ ->
                            SPDataManager.instance.setNeverShowTip()
                        }
                        .setCancelable(false)
                        .show()
                }
                string(R.string.apk_low_version)
            }
        }
    }

    private fun changeViewStatus(isEnable: Boolean) {
        binding.run {
            clActivity.visibleOrGone(isEnable)
            clPermission.visibleOrGone(isEnable)
            tvCancel.visibleOrGone(isEnable)
            tvSilently.visibleOrGone(isEnable)

            fabInstall.isEnabled = isEnable
            layoutHeader.isEnabled = isEnable
        }
    }

    private fun loadingComposeInfo(apkInfo: ApkInfoEntity) {
        binding.clPermission.apply {
            val size = apkInfo.permissions?.size ?: 0
            setTitle(string(R.string.app_permissions, size))
            apkInfo.permissions?.let {
                setScrollView(binding.fabInstall)
                setData(it)
            }
        }

        binding.clActivity.apply {
            val size = apkInfo.activities?.size ?: 0
            setTitle(string(R.string.apk_activity, size))
            apkInfo.activities?.let {
                setScrollView(binding.fabInstall)
                setData(it)
            }
        }
    }

    private fun installSuccess(apkInfo: ApkInfoEntity) {
        val launch = packageManager.getLaunchIntentForPackage(apkInfo.packageName)
        binding.fabInstall.apply {
            setImageResource(R.drawable.round_eject_24)
            isEnabled = launch != null
        }
        binding.layoutHeader.setTitle(string(R.string.install_successful))
        binding.tvCancel.text = string(R.string.exit_app)
    }

    private fun installFailure(apkInfo: ApkInfoEntity) {
        binding.fabInstall.apply {
            setImageResource(R.drawable.round_priority_high_24)
            isEnabled = false
        }
        binding.layoutHeader.setTitle(string(R.string.install_failed_msg))
        binding.tvCancel.text = string(R.string.exit_app)
        if (!SPDataManager.instance.isNeverShowUsePkg()) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.use_system_pkg)
                .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                    AppHelper.executeSystemPkgInstall(this, apkInfo.filePath)
                    finish()
                }
                .setNegativeButton(R.string.dialog_btn_cancel, null)
                .setNeutralButton(R.string.dialog_no_longer_prompt) { _, _ ->
                    SPDataManager.instance.setNeverShowUsePkg()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun onInstallLog(installLog: String) {
        binding.tvInstallLog.append("\n")
        binding.tvInstallLog.append(installLog)
    }

    private fun startInstallFun() {
        if (isInstalled) {
            val launch = packageManager.getLaunchIntentForPackage(apkInfo!!.packageName)
            startActivity(launch)
            finish()
        } else {
            isInstalling = true
            model.startInstall(apkInfo!!)
        }
    }

    private fun startSilentlyInstall() {
        val hasPermission = NotificationUtils.checkNotification(this)
        if (hasPermission) {
            InstallerServer.enqueueWork(this, intent.apply {
                putExtra(InstallerServer.APK_REFERER, apkSource)
            })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isInstalled) {
            binding.run {
                val showActivity = SPDataManager.instance.isShowActivity()
                val showPermission = SPDataManager.instance.isShowPermission()
                clActivity.visibleOrGone(showActivity)
                clPermission.visibleOrGone(showPermission)
            }
        }
    }

}
