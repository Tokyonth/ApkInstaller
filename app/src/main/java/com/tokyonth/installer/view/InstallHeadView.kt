package com.tokyonth.installer.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.SettingsActivity
import com.tokyonth.installer.data.LocalDataRepo
import com.tokyonth.installer.databinding.LayoutInstallTopBinding
import com.tokyonth.installer.utils.AppHelper
import com.tokyonth.installer.utils.ktx.lazyBind

class InstallHeadView : FrameLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private val binding: LayoutInstallTopBinding by lazyBind()

    private val local = LocalDataRepo.instance

    private fun initView() {
        nightModeStatus()
        binding.ibNightMode.setOnClickListener {
            startNightModeFun()
        }
        binding.ibSettings.setOnClickListener {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
        addView(binding.root)
    }

    fun setAppIcon(icon: Bitmap) {
        binding.ivAppIcon.setImageBitmap(icon)
        initPaletteColor(icon)
    }

    fun setAppName(name: String) {
        binding.tvAppName.text = name
    }

    fun setAppVersion(version: String) {
        binding.tvAppVersion.text = version
    }

    fun showVersionTip(versionCode: Int, installedVersionCode: Int) {
        binding.tvVersionTips.apply {
            val translateAnimation = TranslateAnimation(0f, 0f, -200f, 0f)
            translateAnimation.duration = 500
            alpha = 0.70f
            animation = translateAnimation
            startAnimation(translateAnimation)
            visibility = View.VISIBLE
            text = AppHelper.checkVersion(context, versionCode, installedVersionCode)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        //super.setEnabled(enabled)
        binding.ibNightMode.isEnabled = enabled
        binding.ibSettings.isEnabled = enabled
    }

    private fun nightModeStatus() {
        local.isNightMode().let {
            val res = if (it) {
                R.drawable.ic_night
            } else {
                R.drawable.ic_dark
            }
            binding.ibNightMode.setImageResource(res)
        }
    }

    private fun initPaletteColor(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            val vibrantSwatch = palette!!.lightVibrantSwatch
            val color: Int = if (vibrantSwatch != null) {
                AppHelper.colorBurn(vibrantSwatch.rgb)
            } else {
                ContextCompat.getColor(context, R.color.colorAccent)
            }
            val drawableHead = ContextCompat.getDrawable(context, R.drawable.bg_round_8)?.mutate()
            drawableHead?.setTint(color)

            binding.tvVersionTips.setTextColor(color)
            binding.root.let {
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
                            it.background = drawableHead
                        }
                    })
                    duration = 500
                    start()
                }
            }
        }
    }

    private fun startNightModeFun() {
        local.isNightMode().let {
            if (it) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            local.setNightMode(!it)
        }
        nightModeStatus()
    }

}
