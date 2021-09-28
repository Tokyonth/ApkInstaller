package com.tokyonth.installer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.App
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.data.PermFullEntity
import com.tokyonth.installer.databinding.*
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.install.InstallCallback
import com.tokyonth.installer.install.InstallStatus
import com.tokyonth.installer.utils.*
import com.tokyonth.installer.view.CustomizeDialog
import com.tokyonth.installer.view.ProgressDrawable
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class InstallerActivity : BaseActivity(), InstallCallback {

    private val viewBind: ActivityInstallerBinding by lazyBind()

    private lateinit var apkInfoEntity: ApkInfoEntity
    private lateinit var apkCommander: APKCommander
    private lateinit var localDataRepo: LocalDataRepo
    private lateinit var progressDrawable: ProgressDrawable

    private lateinit var apkSource: String
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
        localDataRepo = App.localData
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
                apkCommander.start()
            }
        }
    }

    override fun initView(): ViewBinding {
        arrayOf(
                viewBind.fabInstall,
                viewBind.tvSilently,
                viewBind.tvCancel,
                viewBind.rlToSource,
        ).let { views ->
            View.OnClickListener {
                when (it) {
                    views[0] -> startInstallFun()
                    views[1] -> startSilentlyFun()
                    views[2] -> finish()
                    views[3] -> CommonUtils.toSelfSetting(this, apkSource)
                }
            }.run {
                for (view in views) {
                    view.setOnClickListener(this)
                }
            }
        }
        return viewBind
    }

    override fun onApkParsed(apkInfo: ApkInfoEntity) {
        if (!apkInfo.packageName.isNullOrEmpty()) {
            apkInfoEntity = apkInfo
            setViewStatus(true)
            initApkDetails()
        } else {
            CustomizeDialog.getInstance(this)
                    .setMessage(getString(R.string.parse_apk_failed, intent.data))
                    .setNegativeButton(getString(R.string.exit_app)) { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
        }
    }

    override fun onApkPreInstall() {
        setViewStatus(false)
        progressDrawable = ProgressDrawable().apply {
            putColor(Color.WHITE)
            animatorDuration(1500)
            start()
        }

        viewBind.fabInstall.icon = progressDrawable
        viewBind.tvInstallMsg.text = ""
        viewBind.installHeadView.setAppName(getString(R.string.installing))
    }

    override fun onApkInstalled(installStatus: InstallStatus) {
        when (installStatus) {
            InstallStatus.SUCCESS -> installSuccess()
            InstallStatus.FAILURE -> installFailure()
        }
        viewBind.tvCancel.visibility = View.VISIBLE
        viewBind.llDel.visibility = View.VISIBLE
        viewBind.installHeadView.isEnabled = true
        progressDrawable.stop()
        installSuccess = true

        if (localDataRepo.isAutoDel()) {
            File(apkInfoEntity.filePath!!).delete()
            showToast(getString(R.string.apk_deleted, apkInfoEntity.appName))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initApkDetails() {
        viewBind.installHeadView.setAppIcon(apkInfoEntity.getIcon()!!)
        viewBind.installHeadView.setAppName(apkInfoEntity.appName!!)
        viewBind.installHeadView.setAppVersion(apkInfoEntity.version)

        viewBind.sbAutoDel.setOnCheckedChangeListener { _, isChecked -> localDataRepo.setAutoDel(isChecked) }
        viewBind.tvApkSource.text = getString(R.string.text_apk_source, PackageUtils.getAppNameByPackageName(this, apkSource))
        viewBind.ivApkSource.setImageDrawable(PackageUtils.getAppIconByPackageName(this, apkSource))

        val apkSize = FileIOUtils.byteToString(FileIOUtils.getFileSize(apkInfoEntity.filePath))
        viewBind.tvInstallMsg.text =
                resources.getString(R.string.info_pkg_name) + " " + apkInfoEntity.packageName + "\n" +
                        resources.getString(R.string.info_apk_path) + " " + apkInfoEntity.filePath + "\n" +
                        resources.getString(R.string.text_apk_file_size, apkSize)
        if (apkInfoEntity.hasInstalledApp()) {
            viewBind.tvInstallMsg.append("\n${resources.getString(R.string.info_installed_version)} ${apkInfoEntity.installedVersion}")
            viewBind.installHeadView.showVersionTip(apkInfoEntity.versionCode, apkInfoEntity.installedVersionCode)
        }
        loadingPermActInfo()
    }

    private fun setViewStatus(isEnable: Boolean) {
        viewBind.clAct.visibleOrGone(isEnable)
        viewBind.clPerm.visibleOrGone(isEnable)
        viewBind.tvCancel.visibleOrGone(isEnable)
        viewBind.tvSilently.visibleOrGone(isEnable)

        viewBind.fabInstall.isEnabled = isEnable
        viewBind.rlToSource.isEnabled = isEnable
        viewBind.installHeadView.isEnabled = isEnable
    }

    private fun loadingPermActInfo() {
        viewBind.clPerm.setTitle(getString(R.string.app_permissions, "0"))
        apkInfoEntity.permissions?.let {
            val permList = ArrayList<PermFullEntity>()
            val (group, lab, des) = apkInfoEntity.permissionsDesc!!
            for (index in it.indices) {
                permList.add(PermFullEntity(it[index], group[index], des[index], lab[index]))
            }
            viewBind.clPerm.setScrollView(viewBind.fabInstall)
            viewBind.clPerm.setListData(permList)
            viewBind.clPerm.setTitle(getString(R.string.app_permissions, it.size.toString()))
        }
        viewBind.clAct.setTitle(getString(R.string.apk_activity, "0"))
        apkInfoEntity.activities?.let {
            val actList = ArrayList<String>()
            for (act in it) {
                actList.add(act.name)
            }
            viewBind.clAct.setScrollView(viewBind.fabInstall)
            viewBind.clAct.setListData(actList)
            viewBind.clAct.setTitle(getString(R.string.apk_activity, it.size.toString()))
        }
    }

    private fun installSuccess() {
        val launch = packageManager.getLaunchIntentForPackage(apkInfoEntity.packageName!!)
        viewBind.fabInstall.apply {
            icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_open)
            text = getString(R.string.open_installed_app)
            if (launch != null) {
                isEnabled = true
            } else {
                isEnabled = false
                backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.color8, null))
            }
        }
        viewBind.installHeadView.setAppName(getString(R.string.install_successful))
        viewBind.tvCancel.text = getString(R.string.back_track)
    }

    private fun installFailure() {
        viewBind.fabInstall.apply {
            backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.color7, null))
            icon = ContextCompat.getDrawable(this@InstallerActivity, R.drawable.ic_close)
            text = getString(R.string.install_failed_msg)
            isEnabled = false
        }
        viewBind.installHeadView.setAppName(getString(R.string.install_failed_msg))
        viewBind.tvCancel.text = getString(R.string.exit_app)

        if (!localDataRepo.isNeverShowUsePkg()) {
            CustomizeDialog.getInstance(this)
                    .setTitle(R.string.dialog_title_tip)
                    .setMessage(R.string.use_system_pkg)
                    .setPositiveButton(R.string.dialog_btn_ok) { _, _ ->
                        CommonUtils.startSystemPkgInstall(this, apkInfoEntity.filePath)
                        finish()
                    }
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .setNeutralButton(R.string.dialog_no_longer_prompt) { _, _ ->
                        localDataRepo.setNeverShowUsePkg()
                    }
                    .setCancelable(false)
                    .show()
        }
    }

    override fun onInstallLog(installLog: String) {
        viewBind.tvInstallMsg.append(installLog)
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
        viewBind.clAct.visibleOrGone(localDataRepo.isShowActivity())
        viewBind.clPerm.visibleOrGone(localDataRepo.isShowPermission())
    }

}
