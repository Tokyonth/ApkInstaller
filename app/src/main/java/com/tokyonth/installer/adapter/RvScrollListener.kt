package com.tokyonth.installer.adapter

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class RvScrollListener(private val view: ExtendedFloatingActionButton?) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> view!!.alpha = 1f
            RecyclerView.SCROLL_STATE_DRAGGING -> view!!.alpha = 0.2f
        }
    }

}