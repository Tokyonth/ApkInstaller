package com.tokyonth.installer.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.ViewAnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.resources.MaterialAttributes
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.SettingsActivity
import com.tokyonth.installer.data.ApkInfoEntity
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.databinding.LayoutInstallHeaderBinding
import com.tokyonth.installer.utils.ktx.*

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
        binding.ibNightMode.click {
            changeNightMode()
        }
        binding.ibSettings.click {
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

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.ibNightMode.isEnabled = enabled
        binding.ibSettings.isEnabled = enabled
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
        val h = CommonUtils.getScreenHeight() * 0.3
        val height = MeasureSpec.makeMeasureSpec(h.toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, height)
    }

}
