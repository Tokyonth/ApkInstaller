package com.tokyonth.installer.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.kyleduo.switchbutton.SwitchButton
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.ActivityAdapter
import com.tokyonth.installer.widget.CircleImageView
import com.tokyonth.installer.widget.CustomizeDialog
import com.tokyonth.installer.utils.GetAppInfoUtils
import com.tokyonth.installer.utils.VersionHelper
import com.tokyonth.installer.adapter.PermissionAdapter
import com.tokyonth.installer.adapter.RvScrollListener
import com.tokyonth.installer.install.APKCommander
import com.tokyonth.installer.bean.ApkInfoBean
import com.tokyonth.installer.install.CommanderCallback
import com.tokyonth.installer.bean.permissions.PermFullBean
import com.tokyonth.installer.widget.ProgressDrawable
import com.tokyonth.installer.utils.ParsingContentUtil
import com.tokyonth.installer.utils.FileUtils
import com.tokyonth.installer.utils.AssemblyUtils
import com.tokyonth.installer.utils.SPUtils

import java.io.File
import java.util.ArrayList

class InstallerActivity : BaseActivity(), CommanderCallback, View.OnClickListener {

    private var sbAutoDel: SwitchButton? = null
    private var rvPerm: RecyclerView? = null
    private var rvActivity: RecyclerView? = null
    private var cardPerm: CardView? = null
    private var cardActivity: CardView? = null

    private var permFullBeanArrayList: ArrayList<PermFullBean>? = null
    private var actStringArrayList: ArrayList<String>? = null
    private var progressDrawable: ProgressDrawable? = null
    private var permAdapter: PermissionAdapter? = null
    private var actAdapter: ActivityAdapter? = null
    private var apkCommander: APKCommander? = null

    private var apkFilePath: String? = null
    private var apkFileName: String? = null
    private var apkSource: String? = null

    private var installComplete = false
    private var showActivity = false
    private var showPerm = false

    private var uriData: Uri? = null

    private var fabInstall: ExtendedFloatingActionButton? = null
    private var apkSourceIcon: CircleImageView? = null
    private var bottomAppBar: BottomAppBar? = null
    private var tvVersionTips: TextView? = null
    private var tvApkSource: TextView? = null
    private var tvAppVersion: TextView? = null
    private var tvInstallMsg: TextView? = null
    private var tvAppName: TextView? = null
    private var tvSilently: TextView? = null
    private var tvCancel: TextView? = null

    override fun setActivityView(): Int {
        return R.layout.activity_installer
    }

    override fun initActivity(savedInstanceState: Bundle?) {
        initView()
        initListener()
        initData()
    }

    private fun initView() {
        rvPerm = findViewById(R.id.perm_rv)
        rvActivity = findViewById(R.id.act_rv)
        cardPerm = findViewById(R.id.card_perm)
        cardActivity = findViewById(R.id.card_act)
        sbAutoDel = findViewById(R.id.sb_auto_del)
        bottomAppBar = findViewById(R.id.bottom_app_bar)
        apkSourceIcon = findViewById(R.id.iv_apk_source)
        tvApkSource = findViewById(R.id.tv_apk_source)
        tvVersionTips = findViewById(R.id.tv_version_tips)
        tvAppName = findViewById(R.id.tv_app_name)
        tvAppVersion = findViewById(R.id.tv_app_version)
        tvInstallMsg = findViewById(R.id.tv_install_msg)
        fabInstall = findViewById(R.id.fab_install)
        tvCancel = findViewById(R.id.tv_cancel)
        tvSilently = findViewById(R.id.tv_silently)

        bottomAppBar!!.visibility = View.INVISIBLE
    }

    private fun initListener() {
        fabInstall!!.setOnClickListener(this)
        tvCancel!!.setOnClickListener(this)
        tvSilently!!.setOnClickListener(this)
        findViewById<LinearLayout>(R.id.act_ll).setOnClickListener(this)
        findViewById<LinearLayout>(R.id.perm_ll).setOnClickListener(this)
        findViewById<ImageButton>(R.id.ib_night_mode).setOnClickListener(this)
        findViewById<ImageButton>(R.id.ib_settings).setOnClickListener(this)
        findViewById<LinearLayout>(R.id.ll_to_source).setOnClickListener(this)
    }

    private fun initData() {
        uriData = intent.data
        apkSource = ParsingContentUtil.reflectGetReferrer(this)
        apkCommander = uriData?.let { APKCommander(this@InstallerActivity, it, this, apkSource as String) }
        permFullBeanArrayList = ArrayList()
        actStringArrayList = ArrayList()
        permAdapter = PermissionAdapter(permFullBeanArrayList, this)
        actAdapter = ActivityAdapter(actStringArrayList)
        rvPerm!!.layoutManager = LinearLayoutManager(this)
        rvActivity!!.layoutManager = LinearLayoutManager(this)
        rvPerm!!.adapter = permAdapter
        rvActivity!!.adapter = actAdapter

        rvPerm!!.addOnScrollListener(RvScrollListener(fabInstall))
        rvActivity!!.addOnScrollListener(RvScrollListener(fabInstall))

        sbAutoDel!!.setOnCheckedChangeListener { _, isChecked -> SPUtils.putData(Constants.SP_AUTO_DEL, isChecked) }

        tvApkSource!!.text = getString(R.string.text_apk_source, GetAppInfoUtils.getApplicationNameByPackageName(this, apkSource))
        apkSourceIcon!!.setImageDrawable(GetAppInfoUtils.getApplicationIconByPackageName(this, apkSource))
    }

    private fun setViewStatus() {
        val isShowPerm = if (SPUtils.getData(Constants.SP_SHOW_PERM, true) as Boolean) View.VISIBLE else View.GONE
        cardPerm!!.visibility = isShowPerm
        val isShowAct = if (SPUtils.getData(Constants.SP_SHOW_ACT, true) as Boolean) View.VISIBLE else View.GONE
        cardActivity!!.visibility = isShowAct
        val isAutoDel = SPUtils.getData(Constants.SP_AUTO_DEL, false) as Boolean
        sbAutoDel!!.isChecked = isAutoDel
    }

    @SuppressLint("SetTextI18n")
    private fun initDetails(apkInfo: ApkInfoBean) {
        val ivAppIcon = findViewById<ImageView>(R.id.iv_app_icon)
        ivAppIcon.setImageDrawable(apkInfo.icon)
        val bitmap = AssemblyUtils.DrawableToBitmap(apkInfo.icon)
        Palette.from(bitmap).generate { palette ->
            assert(palette != null)
            val vibrantSwatch = palette!!.lightVibrantSwatch
            val color: Int
            color = if (vibrantSwatch != null) {
                AssemblyUtils.ColorBurn(vibrantSwatch.rgb)
            } else {
                resources.getColor(R.color.colorAccent)
            }
            tvVersionTips!!.setTextColor(color)
            val targetView = findViewById<AppBarLayout>(R.id.app_bar_layout)
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

        bottomAppBar!!.visibility = View.VISIBLE
        cardPerm!!.visibility = View.VISIBLE
        cardActivity!!.visibility = View.VISIBLE
        apkFileName = apkInfo.appName
        apkFilePath = apkInfo.apkFile!!.path

        tvAppName!!.text = apkInfo.appName
        tvAppVersion!!.text = apkInfo.version

        tvInstallMsg!!.text = resources.getString(R.string.info_pkg_name) + apkInfo.packageName + "\n" +
                resources.getString(R.string.info_apk_path) + apkFilePath + "\n" +
                resources.getString(R.string.text_size, FileUtils.byteToString(FileUtils.getFileSize(apkFilePath)))
        if (apkInfo.hasInstalledApp()) {
            tvInstallMsg!!.append("\n" + resources.getString(R.string.info_installed_version) + apkInfo.installedVersion)
            val cardView = findViewById<CardView>(R.id.tip_card)
            cardView.alpha = 0.70f
            val translateAnimation = TranslateAnimation(0f, 0f, -200f, 0f)
            translateAnimation.duration = 500
            cardView.animation = translateAnimation
            cardView.startAnimation(translateAnimation)
            cardView.visibility = View.VISIBLE
            tvVersionTips!!.text = VersionHelper.CheckVer(this, apkInfo.versionCode, apkInfo.installedVersionCode)
        }

        if (SPUtils.getData(Constants.SP_SHOW_PERM, true) as Boolean) {
            cardPerm!!.visibility = View.VISIBLE
            if (apkInfo.permissions != null && apkInfo.permissions!!.isNotEmpty()) {
                for (i in apkInfo.permissions!!.indices) {
                    if (SPUtils.getData(Constants.SP_SHOW_PERM, true) as Boolean) {
                        permFullBeanArrayList!!.add(PermFullBean(apkInfo.permissions!![i],
                                apkCommander!!.permInfo?.permissionGroup?.get(i),
                                apkCommander!!.permInfo?.permissionDescription?.get(i),
                                apkCommander!!.permInfo?.permissionLabel?.get(i)))
                    }
                }
            }
        } else {
            cardPerm!!.visibility = View.GONE
        }
        if (SPUtils.getData(Constants.SP_SHOW_ACT, true) as Boolean) {
            cardActivity!!.visibility = View.VISIBLE
            if (apkInfo.activities != null && apkInfo.activities!!.isNotEmpty()) {
                actStringArrayList!!.addAll(apkInfo.activities!!)
            }
        } else {
            cardActivity!!.visibility = View.GONE
        }
        permAdapter!!.notifyDataSetChanged()
        actAdapter!!.notifyDataSetChanged()

        val tvPermQuantity = findViewById<TextView>(R.id.tv_perm_quantity)
        val tvActQuantity = findViewById<TextView>(R.id.tv_act_quantity)
        tvPermQuantity.text = getString(R.string.app_permissions, permAdapter!!.itemCount.toString())
        tvActQuantity.text = getString(R.string.app_act, actAdapter!!.itemCount.toString())
    }

    override fun onStartParseApk(uri: Uri) {
        fabInstall!!.visibility = View.GONE
    }

    override fun onApkParsed(apkInfo: ApkInfoBean) {
        if (!TextUtils.isEmpty(apkInfo.packageName)) {
            initDetails(apkInfo)
            fabInstall!!.visibility = View.VISIBLE
        } else {
            val uri = intent.data
            var str: String? = null
            if (uri != null) {
                str = uri.toString()
            }
            tvInstallMsg!!.text = getString(R.string.parse_apk_failed, str)
        }
    }

    override fun onApkPreInstall(apkInfo: ApkInfoBean) {
        findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        cardPerm!!.visibility = View.GONE
        cardActivity!!.visibility = View.GONE
        fabInstall!!.isEnabled = false
        tvCancel!!.visibility = View.GONE
        tvSilently!!.visibility = View.GONE

        progressDrawable = ProgressDrawable()
        progressDrawable!!.putColor(Color.WHITE)
        progressDrawable!!.animatorDuration(1500)
        progressDrawable!!.start()
        fabInstall!!.icon = progressDrawable
        tvAppName!!.text = getString(R.string.installing)
        tvInstallMsg!!.text = ""
    }

    override fun onApkInstalled(apkInfo: ApkInfoBean, resultCode: Int) {
        if (resultCode == 0) {
            findViewById<View>(R.id.card_del).visibility = View.VISIBLE
            showToast(getString(R.string.apk_installed, apkInfo.appName))
            if (SPUtils.getData(Constants.SP_VIBRATE, false) as Boolean) {
                val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(800)
            }
            progressDrawable!!.stop()
            tvAppName!!.text = getString(R.string.successful)
            fabInstall!!.icon = getDrawable(R.drawable.ic_offline_bolt_24px)
            fabInstall!!.text = getString(R.string.open_app)
            fabInstall!!.isEnabled = true
            tvCancel!!.visibility = View.VISIBLE
            tvCancel!!.text = getString(R.string.back)
        } else {
            progressDrawable!!.stop()
            fabInstall!!.icon = getDrawable(R.drawable.ic_cancel_24px)
            fabInstall!!.text = getString(R.string.failed)
            tvAppName!!.text = getString(R.string.failed)
            tvCancel!!.visibility = View.VISIBLE

            CustomizeDialog.getInstance(this)
                    .setTitle(R.string.dialog_text_title)
                    .setMessage(R.string.use_system_pkg)
                    .setPositiveButton(R.string.dialog_ok) { _, _ ->
                        AssemblyUtils.StartSystemPkgInstall(this@InstallerActivity, apkFilePath)
                        finish()
                    }
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .setCancelable(false).create().show()
        }
        findViewById<View>(R.id.progressBar).visibility = View.GONE
        installComplete = true
    }

    override fun onInstallLog(apkInfo: ApkInfoBean, logText: String) {
        tvInstallMsg!!.append(logText)
    }

    private fun isAutoDel() {
        if (SPUtils.getData(Constants.SP_AUTO_DEL, false) as Boolean) {
            if (File(apkFilePath!!).delete()) {
                showToast(getString(R.string.apk_deleted, apkFileName))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_install -> {
                if (installComplete) {
                    startActivity(apkCommander!!.apkInfo?.packageName.let { it?.let { it1 -> packageManager.getLaunchIntentForPackage(it1) } })
                    isAutoDel()
                    finish()
                } else {
                    apkCommander!!.startInstall()
                }
            }
            R.id.tv_cancel -> {
                isAutoDel()
                finish()
            }
            R.id.tv_silently -> {
                val intent = Intent(this@InstallerActivity, SilentlyInstallActivity::class.java)
                intent.data = uriData
                intent.putExtra("apkSource", apkSource)
                startActivity(intent)
                finish()
            }
            R.id.perm_ll -> {
                val iv = findViewById<ImageView>(R.id.iv_perm_arrow)
                if (showPerm) {
                    rvPerm!!.visibility = View.GONE
                    iv.setImageResource(R.drawable.ic_arrow_right)
                    showPerm = false
                } else {
                    rvPerm!!.visibility = View.VISIBLE
                    iv.setImageResource(R.drawable.ic_arrow_open)
                    showPerm = true
                }
            }
            R.id.act_ll -> {
                val iv = findViewById<ImageView>(R.id.iv_act_arrow)
                if (showActivity) {
                    rvActivity!!.visibility = View.GONE
                    iv.setImageResource(R.drawable.ic_arrow_right)
                    showActivity = false
                } else {
                    rvActivity!!.visibility = View.VISIBLE
                    iv.setImageResource(R.drawable.ic_arrow_open)
                    showActivity = true
                }
            }
            R.id.ib_night_mode -> {
                if (SPUtils.getData(Constants.SP_NIGHT_MODE, false) as Boolean) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    SPUtils.putData(Constants.SP_NIGHT_MODE, false)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    SPUtils.putData(Constants.SP_NIGHT_MODE, true)
                }
            }
            R.id.ib_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java), 200)
            R.id.ll_to_source -> GetAppInfoUtils.toSelfSetting(this, apkSource)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (installComplete)
            setViewStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (apkCommander!!.apkInfo != null && apkCommander!!.apkInfo?.isFakePath!!) {
            if (!apkCommander!!.apkInfo?.apkFile?.delete()!!) {
                Log.e("InstallerActivity", "failed to delete！")
            }
        }
    }

}
