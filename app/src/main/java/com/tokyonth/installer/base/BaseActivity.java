package com.tokyonth.installer.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tokyonth.installer.Contents;
import com.tokyonth.installer.R;
import com.tokyonth.installer.utils.file.SPUtils;
import com.tokyonth.installer.utils.ui.StatusBarColorUtils;
import com.tokyonth.installer.utils.ui.ToastUtil;

public abstract class BaseActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private boolean isAuthorize = false;
    private static int REQUEST_CODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarColorUtils.setStatusBarDarkIcon(this, !(boolean) SPUtils.getData(Contents.SP_NIGHT_MODE, false));
        if (setActivityView() != 0)
            setContentView(setActivityView());
        if (getIntent().getData() == null) {
            finish();
        } else {
            if (checkPermission()) {
                initActivity();
            } else {
                Toast.makeText(this, getResources().getString(R.string.no_permissions), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public abstract int setActivityView();

    public abstract void initActivity();

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                startRequestPermission();
            } else {
                isAuthorize = true;
            }
        } else {
            isAuthorize = true;
        }
        return isAuthorize;
    }

    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    public void showToast(String text) {
        ToastUtil.showToast(this, text, ToastUtil.DEFAULT_SITE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isAuthorize = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
        }
    }

}
