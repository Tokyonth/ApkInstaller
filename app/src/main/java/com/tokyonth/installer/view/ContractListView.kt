package com.tokyonth.installer.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.installer.databinding.LayoutContractListBinding
import com.tokyonth.installer.utils.ktx.lazyBind
import com.tokyonth.installer.utils.ktx.visibleOrGone

class ContractListView : FrameLayout {

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

    private val binding: LayoutContractListBinding by lazyBind()

    private fun initView() {
        binding.rvContract.layoutManager = LinearLayoutManager(context)
        binding.root.setOnClickListener {
            startViewCloseUpFun()
        }
        addView(binding.root)
    }

    fun setTitle(title: String) {
        binding.tvContract.text = title
    }

    fun setScrollView(view: View) {
        binding.rvContract.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> view.alpha = 1f
                    RecyclerView.SCROLL_STATE_DRAGGING -> view.alpha = 0.2f
                }
            }
        })
    }

    fun setAdapter(block: () -> RecyclerView.Adapter<*>) {
        binding.rvContract.adapter = block.invoke()
    }

    private fun startViewCloseUpFun() {
        binding.rvContract.visibility.let { vis ->
            (vis == View.GONE).let {
                binding.ivContractArrow.animate().rotation(if (it) 90F else 0F).start()
                binding.rvContract.visibleOrGone(it)
            }
        }
    }

}
