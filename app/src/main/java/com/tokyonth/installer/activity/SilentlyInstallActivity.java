package com.tokyonth.installer.activity;

import android.net.Uri;
import android.util.Log;

import com.tokyonth.installer.Contents;
import com.tokyonth.installer.R;
import com.tokyonth.installer.apk.APKCommander;
import com.tokyonth.installer.base.BaseActivity;
import com.tokyonth.installer.bean.ApkInfoBean;
import com.tokyonth.installer.apk.CommanderCallback;
import com.tokyonth.installer.utils.file.SPUtils;

public class SilentlyInstallActivity extends BaseActivity implements CommanderCallback {

    private APKCommander apkCommander;

    @Override
    public int setActivityView() {
        return 0;
    }

    @Override
    public void initActivity() {
        if (getIntent().getData() != null) {
            String apkSource = getIntent().getStringExtra("apkSource");
            apkCommander = new APKCommander(this, getIntent().getData(), this, apkSource);
        } else {
            showToast(getString(R.string.unable_to_install_apk));
            finish();
        }
    }

    @Override
    public void onStartParseApk(Uri uri) {
        showToast(getString(R.string.parsing));
    }

    @Override
    public void onApkParsed(ApkInfoBean apkInfo) {
        apkCommander.startInstall();
    }

    @Override
    public void onApkPreInstall(ApkInfoBean apkInfo) {
        showToast(getString(R.string.start_install, apkInfo.getApkFile().getPath()));
    }

    @Override
    public void onApkInstalled(ApkInfoBean apkInfo, int resultCode) {
        if (resultCode == 0) {
            showToast(getString(R.string.apk_installed, apkInfo.getAppName()));
            if (!apkInfo.isFakePath() && (boolean)SPUtils.getData(Contents.SP_AUTO_DEL, false)) {
                showToast(getString(R.string.apk_deleted, apkInfo.getApkFile().getName()));
            }
        } else {
            showToast(getString(R.string.install_failed, apkInfo.getAppName()));
        }
        finish();
    }

    @Override
    public void onInstallLog(ApkInfoBean apkInfo, String logText) {
        Log.e("SilentlyInstall", logText);
    }

}