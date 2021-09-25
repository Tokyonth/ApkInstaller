package com.tokyonth.installer.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RvScrollListener(private val view: View) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> view.alpha = 1f
            RecyclerView.SCROLL_STATE_DRAGGING -> view.alpha = 0.2f
        }
    }

}