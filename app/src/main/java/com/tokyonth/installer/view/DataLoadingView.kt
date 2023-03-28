package com.tokyonth.installer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.tokyonth.installer.databinding.LayoutErrorBinding

import com.tokyonth.installer.databinding.LayoutLoadingBinding
import com.tokyonth.installer.utils.ktx.click
import com.tokyonth.installer.utils.ktx.lazyBind
import com.tokyonth.installer.utils.ktx.visibleOrGone

class DataLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val CONTENT = "type_content"
        private const val LOADING = "type_loading"
        private const val ERROR = "type_error"

        private const val TAG_LOADING = "TAG_LOADING"
        private const val TAG_ERROR = "TAG_ERROR"
        private const val TAG_CUSTOM = "TAG_CUSTOM"
    }

    private val lnlLoading: LayoutLoadingBinding by lazyBind()

    private val lnlError: LayoutErrorBinding by lazyBind()

    private val contentViews = mutableListOf<View>()

    init {
        lnlLoading.root.tag = TAG_LOADING
        addView(lnlLoading.root)

        lnlError.root.tag = TAG_ERROR
        addView(lnlError.root)

        showContentView()
    }

    fun showLoading() {
        switchState(LOADING)
    }

    fun showContentView() {
        switchState(CONTENT)
    }

    fun showErrorView(msg: String) {
        lnlError.tvErrorMsg.text = msg
        switchState(ERROR)
    }

    private fun hideLoadingView() {
        lnlLoading.root.visibleOrGone(false)
    }

    private fun setLoadingView() {
        lnlLoading.root.visibleOrGone(true)
    }

    private fun hideErrorView() {
        lnlError.root.visibleOrGone(false)
    }

    private fun setErrorView() {
        lnlError.root.visibleOrGone(true)
    }

    private fun switchState(state: String) {
        when (state) {
            CONTENT -> {
                hideLoadingView()
                hideErrorView()
                setContentVisibility(true)
            }
            LOADING -> {
                hideErrorView()
                setLoadingView()
                setContentVisibility(false)
            }
            ERROR -> {
                hideLoadingView()
                setErrorView()
                setContentVisibility(false)
            }
        }
    }

    fun setErrorClickListener(clickListener: OnClickListener) {
        lnlError.root.click {
            clickListener.onClick(lnlError.root)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (child.tag == null || child.tag != TAG_LOADING &&
            child.tag != TAG_ERROR && child.tag != TAG_CUSTOM
        ) {
            contentViews.add(child)
        }
    }

    private fun setContentVisibility(visible: Boolean) {
        for (v in contentViews) {
            v.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

}
