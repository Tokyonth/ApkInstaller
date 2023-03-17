package com.tokyonth.installer.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.model.InstallerViewModel
import com.tokyonth.installer.adapter.ActivityAdapter
import com.tokyonth.installer.adapter.PermissionAdapter
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.databinding.*
import com.tokyonth.installer.install.InstallStatus
import com.tokyonth.installer.install.InstallerServer
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

class InstallerActivity : BaseActivity() {

    private val binding: ActivityInstallerBinding by lazyBind()

    private val model: InstallerViewModel by viewModels()

    private val sp = SPDataManager.instance

    private var apkInfoEntity: ApkInfoEntity? = null

    private var progressDrawable: ProgressDrawable? = null

    private var apkSource: String = ""

    private var installed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (sp.isDefaultSilent()) {
            startSilentlyFun()
            return
        }
        super.onCreate(savedInstanceState)
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
                    views[0] -> startInstallFun(apkInfoEntity!!)
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

    override fun initData() {
        intent.data.let {
            if (it == null) {
                finish()
            } else {
                initLiveDataObs()
                apkSource = AppHelper.reflectGetReferrer(this).toString()
                model.startParse(it, apkSource)
            }
        }
    }

    private fun initLiveDataObs() {
        model.apkParsedLiveData.observe(this) {
            this.apkInfoEntity = it
            onApkParsed(it)
        }
        model.apkPreInstallLiveData.observe(this) {
            onApkPreInstall()
        }
        model.apkInstallLogLiveData.observe(this) {
            onInstallLog(it)
        }
        model.apkInstalledLiveData.observe(this) {
            onApkInstalled(apkInfoEntity!!, it)
        }
    }

    private fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (!apkInfo.packageName.isNullOrEmpty()) {
            setViewStatus(true)
            try {
                initApkDetails(apkInfo)
            } catch (e:Exception){
                Log.e("打印-->", e.stackTraceToString())
            }
        } else {
            DialogUtils.parseFailedDialog(this, intent.data) {
                finish()
            }
        }
    }

    private fun onApkPreInstall() {
        setViewStatus(false)
        progressDrawable = ProgressDrawable().apply {
            putColor(Color.WHITE)
            animatorDuration(1500)
            start()
        }

        binding.run {
            //   fabInstall.icon = progressDrawable
            fabInstall.setImageDrawable(progressDrawable)
            tvInstallMsg.text = ""
            installHeadView.setAppName(string(R.string.installing))
        }
    }

    private fun onApkInstalled(apkInfo: ApkInfoEntity, installStatus: InstallStatus) {
        when (installStatus) {
            InstallStatus.SUCCESS -> installSuccess(apkInfo)
            InstallStatus.FAILURE -> installFailure(apkInfo)
        }

        binding.run {
            tvCancel.visibility = View.VISIBLE
            llDel.visibility = View.VISIBLE
            installHeadView.isEnabled = true
        }

        progressDrawable?.stop()
        installed = true

        if (sp.isAutoDel()) {
            File(apkInfo.filePath!!).delete()
            toast(string(R.string.apk_deleted, apkInfo.appName))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initApkDetails(apkInfo: ApkInfoEntity) {
        binding.installHeadView.apply {
            setAppIcon(apkInfo.icon!!)
            setAppName(apkInfo.appName!!)
            setAppVersion(apkInfo.version)
        }

        binding.sbAutoDel.setOnCheckedChangeListener { _, isChecked ->
            sp.setAutoDel(
                isChecked
            )
        }
        binding.tvApkSource.text = string(
            R.string.text_apk_source,
            PackageUtils.getAppNameByPackageName(this, apkSource)
        )
        binding.ivApkSource.setImageDrawable(PackageUtils.getAppIconByPackageName(this, apkSource))

        val apkSize = FileUtils.byteToString(
            FileUtils.getFileSize(apkInfo.filePath)
        )
        binding.tvInstallMsg.text =
            string(R.string.info_pkg_name) + " " + apkInfo.packageName + "\n" +
                    string(R.string.info_apk_path) + " " + apkInfo.filePath + "\n" +
                    string(R.string.text_apk_file_size, apkSize)
        if (apkInfo.isHasInstalledApp) {
            binding.tvInstallMsg.append("\n${string(R.string.info_installed_version)} ${apkInfo.installedVersion}")
            binding.installHeadView.showVersionTip(
                apkInfo.versionCode,
                apkInfo.installedVersionCode
            )
        }
        loadingPermActInfo(apkInfo)
    }

    private fun setViewStatus(isEnable: Boolean) {
        binding.run {
            clActivity.visibleOrGone(isEnable)
            clPerm.visibleOrGone(isEnable)
            tvCancel.visibleOrGone(isEnable)
            tvSilently.visibleOrGone(isEnable)

            fabInstall.isEnabled = isEnable
            rlToSource.isEnabled = isEnable
            installHeadView.isEnabled = isEnable
        }
    }

    private fun loadingPermActInfo(apkInfo: ApkInfoEntity) {
        binding.clPerm.setTitle(string(R.string.app_permissions, "0"))
        apkInfo.permissions?.let {
            binding.clPerm.apply {
                setScrollView(binding.fabInstall)
                setAdapter {
                    PermissionAdapter(it).apply {
                        setItemClickListener { perm ->
                            DialogUtils.permInfoDialog(this@InstallerActivity, perm)
                        }
                    }
                }
                setTitle(string(R.string.app_permissions, it.size.toString()))
            }
        }

        binding.clActivity.apply {
            setTitle(string(R.string.apk_activity, "0"))
            apkInfo.activities?.let {
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

    private fun installSuccess(apkInfo: ApkInfoEntity) {
        val launch = packageManager.getLaunchIntentForPackage(apkInfo.packageName!!)
        binding.fabInstall.apply {
            //icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_open)
            //text = string(R.string.open_installed_app)
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

    private fun installFailure(apkInfo: ApkInfoEntity) {
        binding.fabInstall.apply {
            backgroundTintList =
                ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.color7, null))
            //icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_close)
            //text = string(R.string.install_failed_msg)
            isEnabled = false
        }
        binding.installHeadView.setAppName(string(R.string.install_failed_msg))
        binding.tvCancel.text = string(R.string.exit_app)
        DialogUtils.useSysPkgTipsDialog(this) {
            when (it) {
                DialogUtils.NEUTRAL_BUTTON -> {
                    sp.setNeverShowUsePkg()
                }
                DialogUtils.POSITIVE_BUTTON -> {
                    AppHelper.startSystemPkgInstall(this, apkInfo.filePath)
                    finish()
                }
            }
        }
    }

    private fun onInstallLog(installLog: String) {
        binding.tvInstallMsg.append(installLog)
    }

    private fun startInstallFun(apkInfo: ApkInfoEntity) {
        if (installed) {
            startActivity(packageManager.getLaunchIntentForPackage(apkInfo.packageName!!))
            finish()
        } else {
            model.startInstall()
        }
    }

    private fun startSilentlyFun() {
        InstallerServer.enqueueWork(this, intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!installed) {
            binding.run {
                clActivity.visibleOrGone(sp.isShowActivity())
                clPerm.visibleOrGone(sp.isShowPermission())
            }
        }
    }

}
