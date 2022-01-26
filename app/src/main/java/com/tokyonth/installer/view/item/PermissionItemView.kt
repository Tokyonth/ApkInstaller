package com.tokyonth.installer.view.item

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.tokyonth.installer.R
import com.tokyonth.installer.data.PermFullEntity
import com.tokyonth.installer.utils.ktx.color
import com.tokyonth.installer.utils.ktx.dp2px
import com.tokyonth.installer.utils.ktx.visibleOrGone

class PermissionItemView : LinearLayout {

    private val padding = 16.dp2px().toInt()

    private lateinit var tvLab: TextView

    private lateinit var tvDes: TextView

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

    private fun initView() {
        orientation = VERTICAL
        padding.let {
            setPadding(it, it, it, it)
        }

        tvLab = createTextView()
        tvDes = createTextView()
        addView(tvLab)
        addView(tvDes)
    }

    private fun createTextView(): TextView {
        return TextView(context).apply {
            setTextColor(color(R.color.colorTextSub))
        }
    }

    fun setData(data: PermFullEntity) {
        tvLab.text = data.perm
        tvDes.apply {
            visibleOrGone(data.des.isNotEmpty())
            text = data.des
        }
    }

}
