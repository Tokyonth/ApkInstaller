package com.tokyonth.installer.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.google.android.material.color.MaterialColors
import com.google.android.material.resources.MaterialAttributes
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.SettingsActivity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.databinding.LayoutInstallTopBinding
import com.tokyonth.installer.utils.AppHelper
import com.tokyonth.installer.utils.ktx.color
import com.tokyonth.installer.utils.ktx.lazyBind
import kotlin.math.abs


class InstallHeadView : FrameLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private val binding: LayoutInstallTopBinding by lazyBind()

    private val local = SPDataManager.instance

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

    @SuppressLint("ResourceType")
    fun setAppIcon(icon: Bitmap) {
        binding.ivBigIcon.setImageBitmap(icon)

        initPaletteColor(icon)
        val m = MaterialAttributes.resolve(context, com.google.android.material.R.attr.colorSurface)
        val color = color(m!!.resourceId)
        val g = GradientDrawable().apply {
            colors = intArrayOf(
                color and 0x4DFFFFFF,
                color and 0xE6FFFFFF.toInt(),
                color
            )
        }

        binding.viewLayer.background = g
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
                //ContextCompat.getColor(context, )
                Color.WHITE
            }
            val drawableHead = ContextCompat.getDrawable(context, R.drawable.bg_round_8)?.mutate()
            drawableHead?.setTint(color)

            binding.tvVersionTips.setTextColor(color)
            return@generate
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
