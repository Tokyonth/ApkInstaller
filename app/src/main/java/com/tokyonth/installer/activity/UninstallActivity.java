package com.tokyonth.installer.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.tokyonth.installer.Constants;
import com.tokyonth.installer.R;
import com.tokyonth.installer.base.BaseActivity;
import com.tokyonth.installer.database.SQLiteUtil;
import com.tokyonth.installer.widget.CustomizeDialog;
import com.tokyonth.installer.utils.ShellUtils;
import com.tokyonth.installer.utils.GetAppInfoUtils;

import java.util.Objects;

public class UninstallActivity extends BaseActivity {

    @Override
    public int setActivityView() {
        return 0;
    }

    @Override
    public void initActivity(@Nullable Bundle savedInstanceState) {
        String pkgName = Objects.requireNonNull(getIntent().getDataString()).replace("package:", "");
        CustomizeDialog.getInstance(this)
                .setTitle(R.string.text_uninstall)
                .setMessage(getString(R.string.text_confirm_uninstall_app, GetAppInfoUtils.getApplicationNameByPackageName(this, pkgName)))
                .setPositiveButton(R.string.text_uninstall, (dialog, which) -> {
                    int result = ShellUtils.execWithRoot(Constants.UNINSTALL_COMMAND + pkgName);
                    String str = result == 0 ? getString(R.string.text_uninstall_complete) : getString(R.string.text_uninstall_failure);
                    showToast(str);
                    finish();
                })
                .setNeutralButton(getText(R.string.uninstall_dialog_disable), (dialog, which) -> {
                    int result = ShellUtils.execWithRoot(Constants.FREEZE_COMMAND + pkgName);
                    String appName = GetAppInfoUtils.getApplicationNameByPackageName(this, pkgName);
                    String str = result == 0 ? getString(R.string.freeze_app_name, appName) : getString(R.string.freeze_failure);
                    if (result == 0) {
                        SQLiteUtil.addData(this, pkgName);
                    }
                    showToast(str);
                    finish();
                })
                .setNegativeButton(R.string.dialog_btn_cancel, (dialog, which) -> finish())
                .create().show();

    }

}