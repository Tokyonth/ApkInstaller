package com.tokyonth.installer.view.item

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.tokyonth.installer.R
import com.tokyonth.installer.data.PermFullEntity
import com.tokyonth.installer.utils.visibleOrGone

class PermissionItemView : LinearLayout {

    private val padding = 16

    private lateinit var tvLab: TextView

    private lateinit var tvDes: TextView

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    fun setData(data: PermFullEntity) {
        tvLab.text = data.perm
        tvDes.apply {
            visibleOrGone(data.des.isNotEmpty())
            text = data.des
        }
    }

    private fun initView() {
        orientation = VERTICAL
        padding.let {
            setPadding(it, it, it, it)
        }

        tvLab = labTextView()
        tvDes = desTextView()
        addView(tvLab)
        addView(tvDes)
    }

    private fun labTextView(): TextView {
        val color = ResourcesCompat.getColor(context.resources, R.color.colorTextSub, null)
        return TextView(context).apply {
            setTextColor(color)
        }
    }

    private fun desTextView(): TextView {
        val color = ResourcesCompat.getColor(context.resources, R.color.colorTextSub, null)
        return TextView(context).apply {
            setTextColor(color)
        }
    }

}