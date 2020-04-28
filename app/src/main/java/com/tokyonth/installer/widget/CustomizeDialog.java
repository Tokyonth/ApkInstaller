package com.tokyonth.installer.widget;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tokyonth.installer.Contents;
import com.tokyonth.installer.R;
import com.tokyonth.installer.utils.SPUtils;

public class CustomizeDialog extends MaterialAlertDialogBuilder {

    public CustomizeDialog(Context context) {
        super(context);
    }

    private CustomizeDialog(Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    public static CustomizeDialog getInstance(Context context) {
        int theme = (boolean) SPUtils.getData(Contents.SP_NIGHT_MODE, false) ?
                R.style.DialogTheme : 0;
        return  new CustomizeDialog(context, theme);

    }

}
