package com.tokyonth.installer.widget.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tokyonth.installer.R;

/**
 * @author kiva
 */
final class StatusBarView {

    private static final float DEFAULT_TEXT_SIZE = 11f;
    private static final float DEFAULT_STATUS_BAR_HEIGHT = 25f;
    private static final long START_DELAY_SHOW = 350;
    private static final long START_DELAY_DISMISS = 500;
    private static final long ANIMATION_DURATION = 150;

    private ViewContainer viewContainer;
    private Activity activity;
    private CharSequence text;
    private boolean showProgressBar = true;
    private long duration = StatusBarToast.DURATION_SHORT;
    private int backgroundColor = Color.TRANSPARENT;

    private int statusBarHeight;
    private boolean translucent;
    private int originalColor;

    StatusBarView(Activity activity) {
        this.activity = activity;
        this.statusBarHeight = getStatusBarHeight(activity);
    }

    void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    void setShowProgressBar(boolean showProgressBar) {
        if (isShowing()) {
            viewContainer.post(() ->
                    viewContainer.progressBar.setVisibility(showProgressBar ? View.VISIBLE : View.INVISIBLE));
            return;
        }
        this.showProgressBar = showProgressBar;
    }

    void setDuration(long duration) {
        this.duration = duration;
    }

    public void setText(CharSequence text) {
        if (isShowing()) {
            viewContainer.post(() -> viewContainer.textView.setText(text));
            return;
        }
        this.text = text;
    }

    void show() {
        if (isShowing()) {
            return;
        }

        this.viewContainer = makeViewContainer();
        viewContainer.textView.setText(text);
        viewContainer.progressBar.setVisibility(showProgressBar ? View.VISIBLE : View.INVISIBLE);

        final Window window = activity.getWindow();
        final ViewGroup decorView = ((ViewGroup) window.getDecorView());

        backupStatusBar(window);

        decorView.addView(viewContainer);

        viewContainer.layout.setTranslationY(-statusBarHeight);
        viewContainer.layout.animate()
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(START_DELAY_SHOW)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        if (duration > 0) {
            viewContainer.postDelayed(this::dismiss, duration + 500);
        }
    }

    void dismiss() {
        if (isShowing()) {
            final Window window = activity.getWindow();
            final ViewGroup decorView = ((ViewGroup) window.getDecorView());

            restoreStatusBar(window);

            viewContainer.layout.animate()
                    .translationY(-statusBarHeight)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(START_DELAY_DISMISS)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            decorView.removeView(viewContainer);
                            viewContainer = null;
                        }
                    })
                    .start();
        }
    }

    boolean isShowing() {
        return viewContainer != null && viewContainer.getParent() != null;
    }

    private void backupStatusBar(Window window) {
        final View rootView = window.getDecorView().getRootView();

        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        rootView.setOnSystemUiVisibilityChangeListener(l ->
                viewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE));

        translucent = isTranslucentStatusBar(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            originalColor = window.getStatusBarColor();
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void restoreStatusBar(Window window) {
        window.getDecorView().getRootView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(originalColor);
            if (translucent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    private ViewContainer makeViewContainer() {
        ViewContainer viewContainer = new ViewContainer(activity);
        viewContainer.setBackgroundColor(backgroundColor);
        viewContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        viewContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight));

        viewContainer.initLayout(activity, statusBarHeight);
        return viewContainer;
    }

    private static int getStatusBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int id = resources.getIdentifier("status_bar_height",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        } else {
            return dpToPixel(activity, DEFAULT_STATUS_BAR_HEIGHT);
        }
    }

    private static int dpToPixel(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private static boolean isTranslucentStatusBar(Activity activity) {
        int flags = activity.getWindow().getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    }

    private static final class ViewContainer extends LinearLayout {
        private LinearLayout layout;
        private TextView textView;
        private ProgressBar progressBar;

        public ViewContainer(Context context) {
            super(context);
        }

        @Override
        protected void onDetachedFromWindow() {
            ((Activity) getContext()).getWindow().getDecorView()
                    .setOnSystemUiVisibilityChangeListener(null);
            super.onDetachedFromWindow();
        }

        void initLayout(Context context, int statusBarHeight) {
            final float textSize = DEFAULT_TEXT_SIZE;

            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    statusBarHeight));

            textView = new TextView(context);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    statusBarHeight));
            textView.setTextSize(textSize);
            textView.setTextColor(getResources().getColor(R.color.colorAccent));
            textView.setGravity(Gravity.CENTER);

            textView.setIncludeFontPadding(false);

            int pixel = dpToPixel(context, textSize);
            progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            progressBar.setLayoutParams(new ViewGroup.LayoutParams(pixel, pixel));

            View delimiter = new View(context);
            delimiter.setLayoutParams(new ViewGroup.LayoutParams(statusBarHeight / 6, statusBarHeight));

            layout.addView(progressBar);
            layout.addView(delimiter);
            layout.addView(textView);
            addView(layout);
        }
    }

}
