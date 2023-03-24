package com.tokyonth.installer.activity

import android.annotation.SuppressLint
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

    private var installed = false

    private var apkSource = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        if (SPDataManager.instance.isDefaultSilent()) {
            startSilentlyInstall()
            return
        }
        super.onCreate(savedInstanceState)
    }

    override fun setBinding() = binding

    override fun initView() {
        arrayOf(
            binding.fabInstall,
            binding.tvSilently,
            binding.tvCancel
        ).let { views ->
            View.OnClickListener {
                when (it) {
                    views[0] -> startInstallFun()
                    views[1] -> startSilentlyInstall()
                    views[2] -> finish()
                }
            }.run {
                for (view in views) {
                    view.setOnClickListener(this)
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        intent.data.let {
            if (it == null) {
                finish()
            } else {
                apkSource = AppHelper.reflectGetReferrer(this).toString()
                permissionHelper?.registerCallback { all, _ ->
                    if (all) {
                        initLiveDataObs()
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
            onApkParsed(it)
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

    private fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (apkInfo.packageName.isNotEmpty()) {
            changeViewStatus(true)
            initApkDetails(apkInfo)
        } else {
            /*DialogUtils.parseFailedDialog(this, intent.data) {
                finish()
            }*/
        }
    }

    private fun onApkParsedFailed(msg: String) {

    }

    @SuppressLint("RestrictedApi")
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
            tvInstallMsg.text = ""
            fabInstall.setImageDrawable(progressDrawable)
            layoutHeader.setTitle(string(R.string.installing))
        }
    }

    private fun onApkInstalled(isInstalled: Boolean) {
        if (isInstalled) {
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
        installed = true

        if (SPDataManager.instance.isAutoDel()) {
            File(apkInfo!!.filePath).delete()
            toast(string(R.string.apk_deleted, apkInfo!!.appName))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initApkDetails(apkInfo: ApkInfoEntity) {
        binding.layoutHeader.setAppInfo(apkInfo)
        binding.sbAutoDel.setOnCheckedChangeListener { _, isChecked ->
            SPDataManager.instance.setAutoDel(isChecked)
        }

        val apkSize = File(apkInfo.filePath).fileOrFolderSize().toMemorySize()
        val msgBuilder = buildString {
            append(string(R.string.info_pkg_name))
            append(apkInfo.packageName)
            append("\n")
            append(string(R.string.info_apk_path))
            append(apkInfo.filePath)
            append("\n")
            append(string(R.string.text_apk_file_size, apkSize))
            if (apkInfo.isHasInstalledApp) {
                append("\n")
                append(string(R.string.info_installed_version))
                append(apkInfo.installedVersion)
            }
        }
        binding.tvInstallMsg.text = msgBuilder

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
                        .setTitle(R.string.dialog_title_tip)
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
            val size = apkInfo.activities?.size ?: 0
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
        binding.tvCancel.text = string(R.string.back_track)
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
                .setTitle(R.string.dialog_title_tip)
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
        binding.tvInstallMsg.append(installLog)
    }

    private fun startInstallFun() {
        if (installed) {
            val launch = packageManager.getLaunchIntentForPackage(apkInfo!!.packageName)
            startActivity(launch)
            finish()
        } else {
            model.startInstall(apkInfo!!)
        }
    }

    private fun startSilentlyInstall() {
        InstallerServer.enqueueWork(this, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!installed) {
            binding.run {
                val showActivity = SPDataManager.instance.isShowActivity()
                val showPermission = SPDataManager.instance.isShowPermission()
                clActivity.visibleOrGone(showActivity)
                clPermission.visibleOrGone(showPermission)
            }
        }
    }

}
