package com.tokyonth.installer.ui;

import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.recyclerview.widget.RecyclerView;

//滑动监听
public class TouchRecyclerViewScroll extends RecyclerView.OnScrollListener {

    private View view;

    public TouchRecyclerViewScroll(View view) {
        this.view = view;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
       // LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        // 当不滚动时

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                // 判断是否滚动到顶部
                //控件显示的动画
                AlphaAnimation mShowAnim = new AlphaAnimation(0.0f, 1.0f);
                mShowAnim.setDuration(200);
                view.startAnimation(mShowAnim);
                view.setVisibility(View.VISIBLE);
                // Log.d("状态---------->>>", "不滚动");
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {//拖动中
                if (view.getVisibility() == View.VISIBLE) {
                    //控件隐藏的动画
                    AlphaAnimation mHiddenAmin = new AlphaAnimation(1.0f, 0.0f);
                    mHiddenAmin.setDuration(200);
                    view.startAnimation(mHiddenAmin);
                    view.setVisibility(View.INVISIBLE);//注意此处不要使用View.GONE
                }
                //  Log.d("状态---------->>>", "滚动中");
            }

    }
}
