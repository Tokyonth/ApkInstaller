package com.tokyonth.installer.widget.statusbar;

import android.app.Activity;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

/**
 * @author kiva
 */
public class StatusBarToast {

    public static final long DURATION_UNLIMITED = 0;
    public static final long DURATION_SHORT = 1000;
    public static final long DURATION_LONG = 3000;

    private final StatusBarView statusBarView;

    private StatusBarToast(StatusBarView statusBarView) {
        this.statusBarView = statusBarView;
    }

    public void dismiss() {
        statusBarView.dismiss();
    }

    public void show() {
        statusBarView.show();
    }

    public void updateText(CharSequence text) {
        statusBarView.setText(text);
    }

    public void setProgressBar(boolean show) {
        statusBarView.setShowProgressBar(show);
    }

    public static class Builder {
        private final StatusBarView statusBarView;
        private final Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;
            this.statusBarView = new StatusBarView(activity);
        }

        public Builder setText(CharSequence text) {
            statusBarView.setText(text);
            return this;
        }

        public Builder setText(@StringRes int res) {
            return setText(activity.getText(res));
        }

        public Builder setBackgroundColor(int color) {
            statusBarView.setBackgroundColor(color);
            return this;
        }

        public Builder setBackgroundColorResource(@ColorRes int res) {
            return setBackgroundColor(ContextCompat.getColor(activity, res));
        }

        public Builder setDuration(long duration) {
            statusBarView.setDuration(duration);
            return this;
        }

        public Builder setShowProgressBar(boolean showProgressBar) {
            statusBarView.setShowProgressBar(showProgressBar);
            return this;
        }

        public void show() {
            statusBarView.show();
        }

        public StatusBarToast build() {
            return new StatusBarToast(statusBarView);
        }
    }
}
