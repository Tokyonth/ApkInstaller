package com.tokyonth.installer.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.tokyonth.installer.R;
import com.tokyonth.installer.utils.SPUtils;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox cb_show_progress_bar;
    private CheckBox cb_show_perm;
    private CheckBox cb_vibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        if ((boolean)SPUtils.getData("NIGHT_MODE", false)) {
            toolbar.setNavigationIcon(R.drawable.ic_title_arrow_left_night);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_title_arrow_left);
        }
        setSupportActionBar(toolbar);
        setTitle(null);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initView();
        initSettings();
    }

    private void initView() {
        TextView tv = findViewById(R.id.tv_version);
        PackageManager pm = this.getPackageManager();

        try {
            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
            String appVersion = pi.versionName;
            tv.append(appVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        cb_show_progress_bar = findViewById(R.id.cb_show_progress_bar);
        cb_show_perm = findViewById(R.id.cb_show_perm);
        cb_vibration = findViewById(R.id.cb_vibrate);

        cb_show_perm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData("show_perm", true);
                } else {
                    SPUtils.putData("show_perm", false);
                }
            }
        });
        cb_show_progress_bar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData("show_progress_bar", true);
                } else {
                    SPUtils.putData("show_progress_bar", false);
                }
            }
        });
        cb_vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData("vibrate", true);
                } else {
                    SPUtils.putData("vibrate", false);
                }
            }
        });

    }

    private void initSettings() {
        if ((boolean)SPUtils.getData("show_progress_bar", true)) {
            cb_show_progress_bar.setChecked(true);
        } else {
            cb_show_progress_bar.setChecked(false);
        }

        if ((boolean)SPUtils.getData("show_perm", true)) {
            cb_show_perm.setChecked(true);
        } else {
            cb_show_perm.setChecked(false);
        }

        if ((boolean)SPUtils.getData("vibrate", false)) {
            cb_vibration.setChecked(true);
        } else {
            cb_vibration.setChecked(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
