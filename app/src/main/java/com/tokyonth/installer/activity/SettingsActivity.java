package com.tokyonth.installer.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kyleduo.switchbutton.SwitchButton;
import com.tokyonth.installer.Constants;
import com.tokyonth.installer.R;
import com.tokyonth.installer.adapter.SettingsAdapter;
import com.tokyonth.installer.utils.StatusBarColorUtils;
import com.tokyonth.installer.widget.CustomizeDialog;
import com.tokyonth.installer.bean.SettingsBean;
import com.tokyonth.installer.utils.GetAppInfoUtils;
import com.tokyonth.installer.utils.FileUtils;
import com.tokyonth.installer.utils.SPUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private SwitchButton switchButtonUseSysPkg;
    private TextView textViewSysPkgName;
    private TextView textViewApkCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarColorUtils.setStatusBarDarkIcon(this,
                !(boolean) SPUtils.getData(Constants.SP_NIGHT_MODE, false));
        setContentView(R.layout.activity_settings);
        initViewData();
        initSettings();
    }

    private void initViewData() {
        ArrayList<SettingsBean> settingsBeanArrayList = new ArrayList<>();
        settingsBeanArrayList.add(new SettingsBean(getString(R.string.title_show_perm),
                getString(R.string.summary_show_perm),
                R.drawable.ic_verified_user_24px, getResources().getColor(R.color.color0)));
        settingsBeanArrayList.add(new SettingsBean(getString(R.string.title_show_act),
                getString(R.string.summary_show_act),
                R.drawable.ic_widgets_24px, getResources().getColor(R.color.color1)));
        settingsBeanArrayList.add(new SettingsBean(getString(R.string.vibrate),
                getString(R.string.install_vibrate),
                R.drawable.ic_waves_24px, getResources().getColor(R.color.color2)));

        SettingsAdapter adapter = new SettingsAdapter(settingsBeanArrayList);
        RecyclerView rvSettings = findViewById(R.id.rv_settings_item);
        rvSettings.setLayoutManager(new LinearLayoutManager(this));
        rvSettings.setAdapter(adapter);
        adapter.setOnItemClick((view, pos, bool) -> {
            switch (pos) {
                case 0:
                    SPUtils.putData(Constants.SP_SHOW_PERM, bool);
                    break;
                case 1:
                    SPUtils.putData(Constants.SP_SHOW_ACT, bool);
                    break;
                case 2:
                    SPUtils.putData(Constants.SP_VIBRATE, bool);
                    break;
            }
        });
        TextView textViewVersion = findViewById(R.id.tv_version);
        textViewVersion.append(GetAppInfoUtils.getVersionName(this));

        textViewApkCache = findViewById(R.id.tv_apk_cache);
        textViewSysPkgName = findViewById(R.id.tv_pkg_name);
        switchButtonUseSysPkg = findViewById(R.id.cb_use_sys_pkg);
        switchButtonUseSysPkg.setOnCheckedChangeListener((compoundButton, isChecked) -> SPUtils.putData(Constants.SP_USE_SYS_PKG, isChecked));

        String cacheSize = FileUtils.byteToString(FileUtils.getFileOrFolderSize(new File(Constants.CACHE_APK_DIR)));
        textViewApkCache.setText(getString(R.string.text_apk_cache, cacheSize));

        findViewById(R.id.card_apk_cache).setOnClickListener(view -> {
            FileUtils.deleteFolderFile(Constants.CACHE_APK_DIR, true);
            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.text_apk_cache_complete), Snackbar.LENGTH_SHORT).show();
            textViewApkCache.setText(getString(R.string.text_apk_cache, cacheSize));
        });

        findViewById(R.id.card_pkg).setOnClickListener(view -> {
            View inView = View.inflate(SettingsActivity.this, R.layout.layout_input_pkg, null);
            final TextInputEditText edit = inView.findViewById(R.id.et_sys_pkg_name);

            CustomizeDialog.getInstance(this)
                    .setTitle(R.string.text_title_input)
                    .setView(inView)
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                        String str = Objects.requireNonNull(edit.getText()).toString().trim();
                        if (str.isEmpty()) {
                            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.text_input_empty), Snackbar.LENGTH_SHORT).show();
                        } else {
                            SPUtils.putData(Constants.SYS_PKG_NAME, str);
                            textViewSysPkgName.setText(str);
                        }
                    })
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .setCancelable(false).create().show();
        });
    }

    private void initSettings() {
        boolean useSysPkg = (boolean) SPUtils.getData(Constants.SP_USE_SYS_PKG, false);
        switchButtonUseSysPkg.setChecked(useSysPkg);
        if (!SPUtils.getData(Constants.SYS_PKG_NAME, Constants.SYS_PKG_NAME).equals(Constants.SYS_PKG_NAME)) {
            textViewSysPkgName.setText(SPUtils.getData(Constants.SYS_PKG_NAME, Constants.SYS_PKG_NAME).toString());
        }
    }

}
