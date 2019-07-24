package com.tokyonth.installer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.tokyonth.installer.R;

public class AvatarBehavior extends CoordinatorLayout.Behavior<ImageView> {

    private static final String TAG = AvatarBehavior.class.getSimpleName();
    // 缩放动画变化的支点
    private static final float ANIM_CHANGE_POINT = 0.2f;

    private Context mContext;
    // 整个滚动的范围
    private int mTotalScrollRange;
    // AppBarLayout高度
    private int mAppBarHeight;
    // AppBarLayout宽度
    private int mAppBarWidth;
    // 控件原始大小
    private int mOriginalSize;
    // 控件最终大小
    private int mFinalSize;
    // 控件最终缩放的尺寸,设置坐标值需要算上该值
    private float mScaleSize;
    // 原始x坐标
    private float mOriginalX;
    // 最终x坐标
    private float mFinalX;
    // 起始y坐标
    private float mOriginalY;
    // 最终y坐标
    private float mFinalY;
    // ToolBar高度
    private int mToolBarHeight;
    // AppBar的起始Y坐标
    private float mAppBarStartY;
    // 滚动执行百分比[0~1]
    private float mPercent;
    // Y轴移动插值器
    private DecelerateInterpolator mMoveYInterpolator;
    // X轴移动插值器
    private AccelerateInterpolator mMoveXInterpolator;
    // 最终变换的视图，因为在5.0以上AppBarLayout在收缩到最终状态会覆盖变换后的视图，所以添加一个最终显示的图片
    private ImageView mFinalView;
    // 最终变换的视图离底部的大小
    private int mFinalViewMarginBottom;


    public AvatarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mMoveYInterpolator = new DecelerateInterpolator();
        mMoveXInterpolator = new AccelerateInterpolator();
        if (attrs != null) {
            TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.AvatarImageBehavior);
            mFinalSize = (int) a.getDimension(R.styleable.AvatarBehavior_finalSize, 0);
            mFinalX = a.getDimension(R.styleable.AvatarBehavior_finalXx, 0);
            mToolBarHeight = (int) a.getDimension(R.styleable.AvatarBehavior_toolBarHeight, 0);
            a.recycle();
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageView child, View dependency) {

        if (dependency instanceof AppBarLayout) {
            _initVariables(child, dependency);

            mPercent = (mAppBarStartY - dependency.getY()) * 1.0f / mTotalScrollRange;
            float percentY = mMoveYInterpolator.getInterpolation(mPercent);
            setViewY(child, mOriginalY, mFinalY - mScaleSize, percentY);
            if (mPercent > ANIM_CHANGE_POINT) {
                float scalePercent = (mPercent - ANIM_CHANGE_POINT) / (1 - ANIM_CHANGE_POINT);
                float percentX = mMoveXInterpolator.getInterpolation(scalePercent);
                scaleView(child, mOriginalSize, mFinalSize, scalePercent);
                setViewX(child, mOriginalX, mFinalX - mScaleSize, percentX);
            } else {
                scaleView(child, mOriginalSize, mFinalSize, 0);
                setViewX(child, mOriginalX, mFinalX - mScaleSize, 0);
            }
            if (mFinalView != null) {
                if (percentY == 1.0f) {
                    // 滚动到顶时才显示
                    mFinalView.setVisibility(View.VISIBLE);
                } else {
                    mFinalView.setVisibility(View.GONE);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && dependency instanceof CollapsingToolbarLayout) {
            // 大于5.0才生成新的最终的头像，因为5.0以上AppBarLayout会覆盖变换后的头像
            if (mFinalView == null && mFinalSize != 0 && mFinalX != 0 && mFinalViewMarginBottom != 0) {
                mFinalView = new ImageView(mContext);
                mFinalView.setVisibility(View.GONE);
                // 添加为CollapsingToolbarLayout子视图
                ((CollapsingToolbarLayout) dependency).addView(mFinalView);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mFinalView.getLayoutParams();
                // 设置大小
                params.width = mFinalSize;
                params.height = mFinalSize;
                // 设置位置，最后显示时相当于变换后的头像位置
                params.gravity = Gravity.BOTTOM;
                params.leftMargin = (int) mFinalX;
                params.bottomMargin = mFinalViewMarginBottom;
                mFinalView.setLayoutParams(params);
                mFinalView.setImageDrawable(child.getDrawable());
                //mFinalView.setBorderColor(child.getBorderColor());
              //  mFinalView.setFillColor(child.getBorderColor());
               // int borderWidth = (int) ((mFinalSize * 1.0f / mOriginalSize) * child.getBorderWidth());
               // mFinalView.setBorderWidth(borderWidth);
            }
            if (mFinalView != null && mFinalSize != 0 && mFinalX != 0 && mFinalViewMarginBottom != 0) {
                mFinalView.setImageDrawable(child.getDrawable());
            }
        }

        return true;
    }

    /**
     * 初始化变量
     *
     * @param child
     * @param dependency
     */
    private void _initVariables(ImageView child, View dependency) {
        if (mAppBarHeight == 0) {
            mAppBarHeight = dependency.getHeight();
            mAppBarStartY = dependency.getY();
        }
        if (mTotalScrollRange == 0) {
            mTotalScrollRange = ((AppBarLayout) dependency).getTotalScrollRange();
        }
        if (mOriginalSize == 0) {
            mOriginalSize = child.getWidth();
        }
        if (mFinalSize == 0) {
            mFinalSize = mContext.getResources().getDimensionPixelSize(R.dimen.avatar_final_size);
        }
        if (mAppBarWidth == 0) {
            mAppBarWidth = dependency.getWidth();
        }
        if (mOriginalX == 0) {
            mOriginalX = child.getX();
        }
        if (mFinalX == 0) {
            mFinalX = mContext.getResources().getDimensionPixelSize(R.dimen.avatar_final_x);
        }
        if (mOriginalY == 0) {
            mOriginalY = child.getY();
        }
        if (mFinalY == 0) {
            if (mToolBarHeight == 0) {
                mToolBarHeight = mContext.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
            }
            int statusBarHeight = mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
            mFinalY = (mToolBarHeight - mFinalSize) / 2 + mAppBarStartY + statusBarHeight;
        }
        if (mScaleSize == 0) {
            mScaleSize = (mOriginalSize - mFinalSize) * 1.0f / 2;
        }
        if (mFinalViewMarginBottom == 0) {
            mFinalViewMarginBottom = (mToolBarHeight - mFinalSize) / 2;
        }
    }

    public void setViewX(View view, float originalX, float finalX, float percent) {
        float calcX = (finalX - originalX) * percent + originalX;
        view.setX(calcX);
    }

    public void setViewY(View view, float originalY, float finalY, float percent) {
        float calcY = (finalY - originalY) * percent + originalY;
        view.setY(calcY);
    }

    public static void scaleView(View view, float originalSize, float finalSize, float percent) {
        float calcSize = (finalSize - originalSize) * percent + originalSize;
        float caleScale = calcSize / originalSize;
        view.setScaleX(caleScale);
        view.setScaleY(caleScale);
    }
}