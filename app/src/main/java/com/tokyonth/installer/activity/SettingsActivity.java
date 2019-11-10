package com.tokyonth.installer.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.kyleduo.switchbutton.SwitchButton;
import com.tokyonth.installer.Config;
import com.tokyonth.installer.R;
import com.tokyonth.installer.ui.CustomDialog;
import com.tokyonth.installer.utils.SPUtils;
import com.tokyonth.installer.utils.ToastUtil;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private SwitchButton cb_show_progress_bar;
    private SwitchButton cb_show_perm;
    private SwitchButton cb_vibration;
    private SwitchButton cb_show_act;
    private SwitchButton cb_use_sys_pkg;
    private TextView tv_sys_pkg_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        if ((boolean)SPUtils.getData(Config.SP_NIGHT_MODE, false)) {
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

        tv_sys_pkg_name = findViewById(R.id.tv_pkg_name);
        cb_show_progress_bar = findViewById(R.id.cb_show_progress_bar);
        cb_show_perm = findViewById(R.id.cb_show_perm);
        cb_vibration = findViewById(R.id.cb_vibrate);
        cb_show_act = findViewById(R.id.cb_show_act);
        cb_use_sys_pkg = findViewById(R.id.cb_use_sys_pkg);

        cb_show_perm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData(Config.SP_SHOW_PERM, true);
                } else {
                    SPUtils.putData(Config.SP_SHOW_PERM, false);
                }
            }
        });
        cb_show_progress_bar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData(Config.SP_PROGRESS, true);
                } else {
                    SPUtils.putData(Config.SP_PROGRESS, false);
                }
            }
        });
        cb_show_act.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData(Config.SP_SHOW_ACT, true);
                } else {
                    SPUtils.putData(Config.SP_SHOW_ACT, false);
                }
            }
        });
        cb_vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData(Config.SP_VIBRATE, true);
                } else {
                    SPUtils.putData(Config.SP_VIBRATE, false);
                }
            }
        });
        cb_use_sys_pkg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData(Config.SP_USE_SYS_PKG, true);
                } else {
                    SPUtils.putData(Config.SP_USE_SYS_PKG, false);
                }
            }
        });

        findViewById(R.id.card_pkg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View inView = View.inflate(SettingsActivity.this, R.layout.layout_input_pkg, null);
                final TextInputEditText edit = inView.findViewById(R.id.et_sys_pkg_name);
                final CustomDialog dialog = new CustomDialog(SettingsActivity.this);
                dialog.setCustView(inView);
                dialog.setTitle(getResources().getString(R.string.text_title_input));
                dialog.setYesOnclickListener(getResources().getString(R.string.dialog_ok), new CustomDialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick() {
                        String str = Objects.requireNonNull(edit.getText()).toString().trim();
                        if (str.isEmpty()) {
                            ToastUtil.showToast(SettingsActivity.this, getResources().getString(R.string.text_input_empty), Toast.LENGTH_SHORT);
                        } else {
                            SPUtils.putData(Config.SYS_PKG_NAME, str);
                            tv_sys_pkg_name.setText(str);
                        }
                    }
                });
                dialog.setNoOnclickListener(getResources().getString(R.string.dialog_btn_cancel), new CustomDialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick() {
                        dialog.dismiss();
                    }
                });
                dialog.create();
                dialog.show();
            }
        });

    }

    private void initSettings() {
        if ((boolean)SPUtils.getData(Config.SP_PROGRESS, true)) {
            cb_show_progress_bar.setChecked(true);
        } else {
            cb_show_progress_bar.setChecked(false);
        }

        if ((boolean)SPUtils.getData(Config.SP_SHOW_PERM, true)) {
            cb_show_perm.setChecked(true);
        } else {
            cb_show_perm.setChecked(false);
        }

        if ((boolean)SPUtils.getData(Config.SP_SHOW_ACT, true)) {
            cb_show_act.setChecked(true);
        } else {
            cb_show_act.setChecked(false);
        }

        if ((boolean)SPUtils.getData(Config.SP_VIBRATE, false)) {
            cb_vibration.setChecked(true);
        } else {
            cb_vibration.setChecked(false);
        }

        if ((boolean)SPUtils.getData(Config.SP_USE_SYS_PKG, false)) {
            cb_use_sys_pkg.setChecked(true);
        } else {
            cb_use_sys_pkg.setChecked(false);
        }

        if (!SPUtils.getData(Config.SYS_PKG_NAME, Config.SYS_PKG_NAME).equals(Config.SYS_PKG_NAME)) {
            tv_sys_pkg_name.setText(SPUtils.getData(Config.SYS_PKG_NAME, Config.SYS_PKG_NAME).toString());
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
