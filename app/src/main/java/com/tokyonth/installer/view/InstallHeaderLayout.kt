package com.tokyonth.installer.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.resources.MaterialAttributes
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.SettingsActivity
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.databinding.LayoutInstallHeaderBinding
import com.tokyonth.installer.utils.ktx.color
import com.tokyonth.installer.utils.ktx.dp2px
import com.tokyonth.installer.utils.ktx.lazyBind
import com.tokyonth.installer.utils.ktx.string

class InstallHeaderLayout : FrameLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private val binding: LayoutInstallHeaderBinding by lazyBind()

    private fun initView() {
        changeNightModeStatus()
        binding.ibNightMode.setOnClickListener {
            changeNightMode()
        }
        binding.ibSettings.setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
        addView(binding.root)
    }

    fun setTitle(title: String) {
        binding.tvAppName.text = title
    }

    @SuppressLint("RestrictedApi")
    fun setAppInfo(apkInfo: ApkInfoEntity) {
        binding.tvAppName.text = apkInfo.appName
        binding.tvAppVersion.text = apkInfo.version

        post {
            startIconAnimation(apkInfo.icon)
        }
        val mColor =
            MaterialAttributes.resolve(context, com.google.android.material.R.attr.colorSurface)
        val surfaceColor = color(mColor!!.resourceId)
        val gDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            gradientType = GradientDrawable.LINEAR_GRADIENT
            colors = intArrayOf(
                surfaceColor and 0x4DFFFFFF,
                surfaceColor and 0x99FFFFFF.toInt(),
                surfaceColor and 0xE6FFFFFF.toInt(),
                surfaceColor
            )
        }
        binding.viewLayer.background = gDrawable
    }

    private fun startIconAnimation(bitmap: Bitmap?) {
        binding.ivBigIcon.let {
            val width = it.measuredWidth
            val height = it.measuredHeight
            ViewAnimationUtils.createCircularReveal(
                it,
                width / 2,
                height / 2,
                0f,
                height.toFloat()
            ).apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        it.setImageBitmap(bitmap)
                    }
                })
                duration = 500
                start()
            }
        }
    }

    private fun showVersionTip(versionCode: Int, installedVersionCode: Int) {
        val translateAnimation = TranslateAnimation(0f, 0f, -200f, 0f)
        translateAnimation.duration = 500
        binding.tvVersionTips.apply {
            alpha = 0.70f
            animation = translateAnimation
            startAnimation(translateAnimation)
            visibility = View.VISIBLE
            text = checkVersion(versionCode, installedVersionCode)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.ibNightMode.isEnabled = enabled
        binding.ibSettings.isEnabled = enabled
    }

    private fun checkVersion(version: Int, installedVersion: Int): String {
        return when {
            version == installedVersion -> string(R.string.apk_equal_version)
            version > installedVersion -> string(R.string.apk_new_version)
            else -> {
                if (!SPDataManager.instance.isNeverShowTip()) {
                    MaterialAlertDialogBuilder(context)
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

    private fun changeNightModeStatus() {
        SPDataManager.instance.isNightMode().let {
            val res = if (it) {
                R.drawable.round_wb_sunny_24
            } else {
                R.drawable.round_bedtime_24
            }
            binding.ibNightMode.setImageResource(res)
        }
    }

    private fun changeNightMode() {
        SPDataManager.instance.isNightMode().let {
            if (it) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            SPDataManager.instance.setNightMode(!it)
        }
        changeNightModeStatus()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.makeMeasureSpec(220.dp2px().toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, height)
    }

}
