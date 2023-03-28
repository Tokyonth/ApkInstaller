package com.tokyonth.installer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.ActivityAdapter
import com.tokyonth.installer.adapter.PermissionAdapter
import com.tokyonth.installer.data.PermissionInfoEntity
import com.tokyonth.installer.databinding.LayoutBaseListBinding
import com.tokyonth.installer.utils.ktx.click
import com.tokyonth.installer.utils.ktx.lazyBind
import com.tokyonth.installer.utils.ktx.string
import com.tokyonth.installer.utils.ktx.visibleOrGone

class BaseListLayout : FrameLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private val binding: LayoutBaseListBinding by lazyBind()

    private fun initView() {
        binding.rvBaseList.layoutManager = LinearLayoutManager(context)
        binding.root.click {
            changeView()
        }
        addView(binding.root)
    }

    fun setTitle(title: String) {
        binding.tvListTitle.text = title
    }

    fun setScrollView(view: View) {
        binding.rvBaseList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> view.alpha = 1f
                    RecyclerView.SCROLL_STATE_DRAGGING -> view.alpha = 0.2f
                }
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> setData(list: MutableList<T>) {
        when (list[0]) {
            is String -> {
                binding.rvBaseList.adapter = ActivityAdapter(list as MutableList<String>)
            }
            is PermissionInfoEntity -> {
                binding.rvBaseList.adapter =
                    PermissionAdapter(list as MutableList<PermissionInfoEntity>).apply {
                        setItemClickListener {
                            showPermissionDes(it)
                        }
                    }
            }
        }
    }

    private fun showPermissionDes(permissionInfo: PermissionInfoEntity) {
        val lab = permissionInfo.permissionLabel.ifEmpty {
            string(R.string.text_no_description)
        }
        val des = permissionInfo.permissionDesc.ifEmpty {
            string(R.string.text_no_description)
        }

        MaterialAlertDialogBuilder(context)
            .setMessage(permissionInfo.permissionName + "\n\n" + lab + "\n\n" + des)
            .setPositiveButton(R.string.dialog_btn_ok, null)
            .create()
            .show()
    }

    private fun changeView() {
        binding.rvBaseList.visibility.let { vis ->
            (vis == View.GONE).let {
                binding.ivListArrow.animate().rotation(if (it) 90F else 0F).start()
                binding.rvBaseList.visibleOrGone(it)
            }
        }
    }

}
