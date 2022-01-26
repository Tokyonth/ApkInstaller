package com.tokyonth.installer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.ActivityAdapter
import com.tokyonth.installer.adapter.PermissionAdapter
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.data.PermFullEntity
import com.tokyonth.installer.databinding.*
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.install.InstallCallback
import com.tokyonth.installer.install.InstallStatus
import com.tokyonth.installer.utils.*
import com.tokyonth.installer.utils.ktx.lazyBind
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.toast
import com.tokyonth.installer.utils.ktx.visibleOrGone
import com.tokyonth.installer.utils.DialogUtils
import com.tokyonth.installer.view.ProgressDrawable
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class InstallerActivity : BaseActivity(), InstallCallback {

    private val binding: ActivityInstallerBinding by lazyBind()

    private lateinit var apkInfoEntity: ApkInfoEntity
    private lateinit var apkCommander: APKCommander
    private lateinit var progressDrawable: ProgressDrawable

    private lateinit var apkSource: String
    private var local = LocalDataRepo.instance
    private var installSuccess = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::apkInfoEntity.isInitialized) {
            outState.putParcelable(Constants.APK_INFO, apkInfoEntity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            apkInfoEntity = savedInstanceState.getParcelable(Constants.APK_INFO)!!
        } else {
            setViewStatus(false)
        }
        super.onCreate(savedInstanceState)
    }

    override fun initData() {
        intent.data.let {
            if (it == null) {
                finish()
            } else {
                apkSource = intent.getStringExtra(Constants.APK_SOURCE)!!
                apkCommander = if (this::apkInfoEntity.isInitialized) {
                    APKCommander(apkInfoEntity, this)
                } else {
                    APKCommander(it, apkSource, this)
                }
                apkCommander.startParse()
            }
        }
    }

    override fun setBinding() = binding

    override fun initView() {
        arrayOf(
            binding.fabInstall,
            binding.tvSilently,
            binding.tvCancel,
            binding.rlToSource,
        ).let { views ->
            View.OnClickListener {
                when (it) {
                    views[0] -> startInstallFun()
                    views[1] -> startSilentlyFun()
                    views[2] -> finish()
                    views[3] -> AppHelper.toSelfSetting(this, apkSource)
                }
            }.run {
                for (view in views) {
                    view.setOnClickListener(this)
                }
            }
        }
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (!apkInfo.packageName.isNullOrEmpty()) {
            apkInfoEntity = apkInfo
            setViewStatus(true)
            initApkDetails()
        } else {
            DialogUtils.parseFailedDialog(this, intent.data) {
                finish()
            }
        }
    }

    override fun onApkPreInstall() {
        setViewStatus(false)
        progressDrawable = ProgressDrawable().apply {
            putColor(Color.WHITE)
            animatorDuration(1500)
            start()
        }

        binding.run {
            fabInstall.icon = progressDrawable
            tvInstallMsg.text = ""
            installHeadView.setAppName(string(R.string.installing))
        }
    }

    override fun onApkInstalled(installStatus: InstallStatus) {
        when (installStatus) {
            InstallStatus.SUCCESS -> installSuccess()
            InstallStatus.FAILURE -> installFailure()
        }

        binding.run {
            tvCancel.visibility = View.VISIBLE
            llDel.visibility = View.VISIBLE
            installHeadView.isEnabled = true
        }

        progressDrawable.stop()
        installSuccess = true

        if (local.isAutoDel()) {
            File(apkInfoEntity.filePath!!).delete()
            toast(string(R.string.apk_deleted, apkInfoEntity.appName))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initApkDetails() {
        binding.installHeadView.apply {
            setAppIcon(apkInfoEntity.getIcon()!!)
            setAppName(apkInfoEntity.appName!!)
            setAppVersion(apkInfoEntity.version)
        }

        binding.sbAutoDel.setOnCheckedChangeListener { _, isChecked ->
            local.setAutoDel(
                isChecked
            )
        }
        binding.tvApkSource.text = string(
            R.string.text_apk_source,
            PackageUtils.getAppNameByPackageName(this, apkSource)
        )
        binding.ivApkSource.setImageDrawable(PackageUtils.getAppIconByPackageName(this, apkSource))

        val apkSize = FileUtils.byteToString(
            FileUtils.getFileSize(apkInfoEntity.filePath)
        )
        binding.tvInstallMsg.text =
            string(R.string.info_pkg_name) + " " + apkInfoEntity.packageName + "\n" +
                    string(R.string.info_apk_path) + " " + apkInfoEntity.filePath + "\n" +
                    string(R.string.text_apk_file_size, apkSize)
        if (apkInfoEntity.hasInstalledApp()) {
            binding.tvInstallMsg.append("\n${string(R.string.info_installed_version)} ${apkInfoEntity.installedVersion}")
            binding.installHeadView.showVersionTip(
                apkInfoEntity.versionCode,
                apkInfoEntity.installedVersionCode
            )
        }
        loadingPermActInfo()
    }

    private fun setViewStatus(isEnable: Boolean) {
        binding.run {
            clAct.visibleOrGone(isEnable)
            clPerm.visibleOrGone(isEnable)
            tvCancel.visibleOrGone(isEnable)
            tvSilently.visibleOrGone(isEnable)

            fabInstall.isEnabled = isEnable
            rlToSource.isEnabled = isEnable
            installHeadView.isEnabled = isEnable
        }
    }

    private fun loadingPermActInfo() {
        binding.clPerm.setTitle(string(R.string.app_permissions, "0"))
        apkInfoEntity.permissions?.let {
            val permList = ArrayList<PermFullEntity>()
            val (group, lab, des) = apkInfoEntity.permissionsDesc!!
            for (index in it.indices) {
                permList.add(PermFullEntity(it[index], group[index], des[index], lab[index]))
            }
            binding.clPerm.apply {
                setScrollView(binding.fabInstall)
                setAdapter {
                    PermissionAdapter(permList).apply {
                        setItemClickListener { perm ->
                            DialogUtils.permInfoDialog(this@InstallerActivity, perm)
                        }
                    }
                }
                setTitle(string(R.string.app_permissions, it.size.toString()))
            }
        }

        binding.clAct.apply {
            setTitle(string(R.string.apk_activity, "0"))
            apkInfoEntity.activities?.let {
                val actList = ArrayList<String>()
                for (act in it) {
                    actList.add(act.name)
                }
                setScrollView(binding.fabInstall)
                setAdapter {
                    ActivityAdapter(actList)
                }
                setTitle(string(R.string.apk_activity, it.size.toString()))
            }
        }
    }

    private fun installSuccess() {
        val launch = packageManager.getLaunchIntentForPackage(apkInfoEntity.packageName!!)
        binding.fabInstall.apply {
            icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_open)
            text = string(R.string.open_installed_app)
            if (launch != null) {
                isEnabled = true
            } else {
                isEnabled = false
                backgroundTintList = ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.color8,
                        null
                    )
                )
            }
        }
        binding.installHeadView.setAppName(string(R.string.install_successful))
        binding.tvCancel.text = string(R.string.back_track)
    }

    private fun installFailure() {
        binding.fabInstall.apply {
            backgroundTintList =
                ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.color7, null))
            icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_close)
            text = string(R.string.install_failed_msg)
            isEnabled = false
        }
        binding.installHeadView.setAppName(string(R.string.install_failed_msg))
        binding.tvCancel.text = string(R.string.exit_app)
        DialogUtils.useSysPkgTipsDialog(this) {
            when (it) {
                DialogUtils.NEUTRAL_BUTTON -> {
                    local.setNeverShowUsePkg()
                }
                DialogUtils.POSITIVE_BUTTON -> {
                    AppHelper.startSystemPkgInstall(this, apkInfoEntity.filePath)
                    finish()
                }
            }
        }
    }

    override fun onInstallLog(installLog: String) {
        binding.tvInstallMsg.append(installLog)
    }

    private fun startInstallFun() {
        if (installSuccess) {
            startActivity(packageManager.getLaunchIntentForPackage(apkInfoEntity.packageName!!))
            finish()
        } else {
            apkCommander.startInstall()
        }
    }

    private fun startSilentlyFun() {
        Intent(this, SilentlyInstallActivity::class.java).let {
            it.putExtra(Constants.IS_FORM_INSTALL_ACT, true)
            it.putExtra(Constants.APK_INFO, apkInfoEntity)
            startActivity(it)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (installSuccess)
            return
        binding.run {
            clAct.visibleOrGone(local.isShowActivity())
            clPerm.visibleOrGone(local.isShowPermission())
        }
    }

}
