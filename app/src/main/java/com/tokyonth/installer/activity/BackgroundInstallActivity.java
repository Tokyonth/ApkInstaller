package com.tokyonth.installer.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.tokyonth.installer.R;
import com.tokyonth.installer.apk.APKCommander;
import com.tokyonth.installer.apk.ApkInfo;
import com.tokyonth.installer.apk.ICommanderCallback;
import com.tokyonth.installer.utils.SPUtils;
import com.tokyonth.installer.utils.ToastUtil;

public class BackgroundInstallActivity extends Activity implements ICommanderCallback {

    private APKCommander apkCommander;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getData() != null) {
            apkCommander = new APKCommander(this, getIntent().getData(),
                    this, getReferrer().getHost());
        } else {
            showToast(getString(R.string.unable_to_install_apk));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    private void showToast(String text) {
        ToastUtil.showToast(this, text, Toast.LENGTH_SHORT);
    }

    @Override
    public void onStartParseApk(Uri uri) {
        showToast(getString(R.string.parsing));
    }

    @Override
    public void onApkParsed(ApkInfo apkInfo) {
        apkCommander.startInstall();
    }

    @Override
    public void onApkPreInstall(ApkInfo apkInfo) {
        showToast(getString(R.string.start_install, apkInfo.getApkFile().getPath()));
    }

    @Override
    public void onApkInstalled(ApkInfo apkInfo, int resultCode) {
        if (resultCode == 0) {
            showToast(getString(R.string.apk_installed, apkInfo.getAppName()));
            if (!apkInfo.isFakePath() && (boolean)SPUtils.getData("auto_delete", false)) {
                Toast.makeText(this, getString(R.string.apk_deleteed, apkInfo.getApkFile().getName()), Toast.LENGTH_SHORT).show();
            }
        } else {
            showToast(getString(R.string.install_failed, apkInfo.getAppName()));
        }
        finish();
    }

    @Override
    public void onInstallLog(ApkInfo apkInfo, String logText) {

    }

}