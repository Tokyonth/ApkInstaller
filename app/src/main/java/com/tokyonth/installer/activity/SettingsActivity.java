package com.tokyonth.installer.activity;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyonth.installer.R;
import com.tokyonth.installer.utils.FileUtils;
import com.tokyonth.installer.utils.SPUtils;
import com.tokyonth.installer.utils.ToastUtil;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox cb_show_progress_bar;
    private CheckBox cb_show_perm;
    private LinearLayout ll_donate, ll_clean;
    private TextView tv_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_title_arrow_left);
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
        cb_show_progress_bar = (CheckBox) findViewById(R.id.cb_show_progress_bar);
        cb_show_perm = (CheckBox) findViewById(R.id.cb_show_perm);
        ll_donate = (LinearLayout) findViewById(R.id.donate);
        ll_clean = (LinearLayout) findViewById(R.id.clean);
        tv_size = (TextView) findViewById(R.id.tv_cache_size);

        final String cache_path = "/storage/emulated/0/Android/data/com.tokyonth.installer/cache/";
        final File file = new File(cache_path);
        tv_size.append(FileUtils.byteToString(FileUtils.getFileOrFolderSize(file)));

        ll_donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //    DonateToMe.show(SettingsActivity.this);
            }
        });
        ll_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileUtils.deleteFolderFile(cache_path, false);
                //Toast.makeText(SettingsActivity.this, R.string.clean_complete, Toast.LENGTH_SHORT).show();
                ToastUtil.showToast(SettingsActivity.this, getResources().getString(R.string.clean_complete), Toast.LENGTH_SHORT);
                tv_size.setText(getResources().getString(R.string.text_cache_size) + FileUtils.byteToString(FileUtils.getFileOrFolderSize(file)));
            }
        });
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
