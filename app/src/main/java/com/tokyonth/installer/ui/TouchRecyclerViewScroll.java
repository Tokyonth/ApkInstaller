package com.tokyonth.installer.ui;

import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TouchRecyclerViewScroll extends RecyclerView.OnScrollListener {

    private View view;

    public TouchRecyclerViewScroll(View view) {
        this.view = view;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                // 判断是否滚动到顶部
                //控件显示的动画
                AlphaAnimation mShowAnim = new AlphaAnimation(0.0f, 1.0f);
                mShowAnim.setDuration(200);
                view.startAnimation(mShowAnim);
                view.setVisibility(View.VISIBLE);
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                //拖动中
                if (view.getVisibility() == View.VISIBLE) {
                    AlphaAnimation mHiddenAnim = new AlphaAnimation(1.0f, 0.0f);
                    mHiddenAnim.setDuration(200);
                    view.startAnimation(mHiddenAnim);
                    view.setVisibility(View.INVISIBLE);
                }
            }
    }
}
