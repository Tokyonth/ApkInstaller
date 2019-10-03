package com.tokyonth.installer.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.tokyonth.installer.BaseActivity;
import com.tokyonth.installer.R;
import com.tokyonth.installer.ui.TouchRecyclerViewScroll;
import com.tokyonth.installer.adapter.PermissionAdapter;
import com.tokyonth.installer.apk.APKCommander;
import com.tokyonth.installer.apk.ApkInfo;
import com.tokyonth.installer.apk.ICommanderCallback;
import com.tokyonth.installer.bean.InfoBean;
import com.tokyonth.installer.utils.FileUtils;
import com.tokyonth.installer.utils.MoreTools;
import com.tokyonth.installer.utils.SPUtils;
import com.tokyonth.installer.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InstallerActivity extends BaseActivity implements ICommanderCallback, View.OnClickListener {

    private AppBarLayout mAppBarLayout;
    private TextView tvAppName, tvAppVer;
    private TextView tv_install_msg, tv_apk_size;
    private TextView perm_index, tv_path, tv_ver, tv_pkg;

    private ImageView imgAppIcon,  perm_iv;
    private ProgressBar progressBar;

    private Button btnInstall, btnSilently, btnCancel;
    private CardView info_card, install_bar;

    private APKCommander apkCommander;
    private RecyclerView main_rv;
    private PermissionAdapter adapter;

    private LinearLayout ll;
    private View install_complete_view, perm_view;
    private SwitchButton sb_auto_del;

    private String path_str, apk_name;
    private List<InfoBean> list_info = new ArrayList<>();
    private boolean tag = false;
    private boolean tag_perm = false;
    private boolean tag_install = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        setContentView(R.layout.activity_installer);

        initView();
        loadSettings();
        if (getIntent().getData() == null) {
            finish();
        } else {
            this.setPermCheck(new PermCheck() {
                @Override
                public void isPerm(boolean b) {
                    if (b) {
                        apkCommander = new APKCommander(InstallerActivity.this, getIntent().getData(), InstallerActivity.this);
                    }
                }
            });
            checkPermission();
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        main_rv = (RecyclerView) findViewById(R.id.main_rv);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBar_layout);
        tvAppName = (TextView) findViewById(R.id.tv_app_name);
        tvAppVer = (TextView) findViewById(R.id.tv_app_ver);
        imgAppIcon = (ImageView) findViewById(R.id.icon);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnInstall = (Button) findViewById(R.id.btn_install);
        btnSilently = (Button) findViewById(R.id.btn_silently);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        info_card = (CardView) findViewById(R.id.info_card);

        perm_index = (TextView) findViewById(R.id.tv1);
        tv_pkg = (TextView) findViewById(R.id.tv_pkg);
        tv_path = (TextView) findViewById(R.id.tv_path);
        tv_ver = (TextView) findViewById(R.id.tv_ver);
        ll = (LinearLayout) findViewById(R.id.perm_ll);
        perm_iv = (ImageView) findViewById(R.id.perm_iv);
        install_bar = (CardView) findViewById(R.id.card_bar);
        tv_install_msg = (TextView) findViewById(R.id.tv_install_msg);
        perm_view = findViewById(R.id.perm_view);
        install_complete_view = findViewById(R.id.install_complete_view);
        sb_auto_del = (SwitchButton) findViewById(R.id.sb_auto_del);
        tv_apk_size = (TextView) findViewById(R.id.tv_app_size);

        tvAppName.setText(R.string.parsing);
        btnInstall.setEnabled(true);
        btnInstall.setOnClickListener(this);
        btnSilently.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        ll.setOnClickListener(this);

        adapter = new PermissionAdapter();
        adapter.setList(list_info);
        main_rv.setAdapter(adapter);
        main_rv.setLayoutManager(new LinearLayoutManager(this));
        main_rv.setOnScrollListener(new TouchRecyclerViewScroll(install_bar));
        sb_auto_del.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    SPUtils.putData("auto_delete", true);
                } else {
                    SPUtils.putData("auto_delete", false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, 100);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSettings() {
        if (apkCommander != null && apkCommander.getApkInfo() != null && apkCommander.getApkInfo().getApkFile() != null) {
            initDetails(apkCommander.getApkInfo());
        }
        if ((boolean)SPUtils.getData("auto_delete", false)) {
            sb_auto_del.setChecked(true);
        } else {
            sb_auto_del.setChecked(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tag = true;
        if (!tag_install) {
            loadSettings();
        }
    }

    private void initDetails(ApkInfo apkInfo) {
        tvAppName.setText(apkInfo.getAppName());
        tvAppVer.setText(apkInfo.getVersion());

        Bitmap bitmap = MoreTools.DrawableToBitmap(apkInfo.getIcon());
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    mAppBarLayout.setBackgroundColor(MoreTools.ColorBurn(vibrantSwatch.getRgb()));
                }
            }
        });

        info_card.setVisibility(View.VISIBLE);
        perm_view.setVisibility(View.VISIBLE);
        install_bar.setVisibility(View.VISIBLE);
        imgAppIcon.setImageDrawable(apkInfo.getIcon());

        apk_name = apkInfo.getAppName();
        path_str = apkInfo.getApkFile().getPath();
        tv_apk_size.setText(getResources().getString(R.string.text_size) + FileUtils.byteToString(FileUtils.getFileSize(path_str)));

        if (!tag) {
            tv_pkg.append(apkInfo.getPackageName());
            tv_path.append(path_str);
            if (apkInfo.hasInstalledApp()) {
                tv_ver.setVisibility(View.VISIBLE);
                tv_ver.append(apkInfo.getInstalledVersion());

                new AlertDialog.Builder(this).setTitle(R.string.dialog_title_tips)
                        .setMessage(R.string.dialog_msg_tips)
                        .setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setPositiveButton(R.string.dialog_btn_continue, null)
                        .setCancelable(false)
                        .show();
            }
        }
        if ((boolean)SPUtils.getData("show_perm", true)) {
            ll.setVisibility(View.VISIBLE);
            if (apkInfo.getPermissions() != null && apkInfo.getPermissions().length > 0) {

                int i = 0;
                for (String perm : apkInfo.getPermissions()) {
                    if (!tag && (boolean)SPUtils.getData("show_perm", true)) {
                        list_info.add(new InfoBean(perm, apkCommander.getInfo().getPermissionDescription().get(i++)));
                    }
                }
            }
        } else {
            perm_view.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
        perm_index.setText(getResources().getString(R.string.app_permissions) + "(" + adapter.getItemCount() + ")");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apkCommander.getApkInfo() != null && apkCommander.getApkInfo().isFakePath())
            apkCommander.getApkInfo().getApkFile().delete();
    }

    @Override
    public void onStartParseApk(Uri uri) {
        btnInstall.setVisibility(View.GONE);
        tv_install_msg.setText(getString(R.string.parsing) + " : " + uri.toString());
    }

    @Override
    public void onApkParsed(ApkInfo apkInfo) {
        list_info.clear();
        if (apkInfo != null && !TextUtils.isEmpty(apkInfo.getPackageName())) {
            initDetails(apkInfo);
            btnInstall.setVisibility(View.VISIBLE);
            tv_install_msg.setVisibility(View.GONE);
        } else {
            Uri uri = getIntent().getData();
            String s = null;
            if (uri != null) {
                s = uri.toString();
            }
            tv_install_msg.setText(getString(R.string.parse_apk_failed, s));
        }
    }

    @Override
    public void onApkPreInstall(ApkInfo apkInfo) {
        perm_view.setVisibility(View.GONE);
        tvAppName.setText(R.string.installing);
        tv_install_msg.setText("");

        progressBar.setVisibility(
                (boolean)SPUtils.getData("show_progress_bar", true) ?
                        View.VISIBLE : View.INVISIBLE);
        install_bar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onApkInstalled(ApkInfo apkInfo, int resultCode) {
        getString(R.string.install_finished_with_result_code, resultCode);
        btnInstall.setEnabled(false);
        btnSilently.setEnabled(false);
        if (resultCode == 0) {
            ToastUtil.showToast(this, getString(R.string.apk_installed, apkInfo.getAppName()), Toast.LENGTH_SHORT);
            tvAppName.setText(R.string.successful);
            btnInstall.setEnabled(true);
            btnInstall.setText(R.string.open_app);
            btnCancel.setText(R.string.text_install_complete);
            btnSilently.setVisibility(View.GONE);
            install_complete_view.setVisibility(View.VISIBLE);
        } else {
            tvAppName.setText(R.string.failed);
            btnInstall.setTextColor(Color.GRAY);
            btnSilently.setTextColor(Color.GRAY);
        }
        progressBar.setVisibility(View.GONE);
        install_bar.setVisibility(View.VISIBLE);
        tag_install = true;
    }

    @Override
    public void onInstallLog(ApkInfo apkInfo, String logText) {
        tv_install_msg.setVisibility(View.VISIBLE);
        tv_pkg.setVisibility(View.GONE);
        tv_path.setVisibility(View.GONE);
        tv_ver.setVisibility(View.GONE);
        tv_apk_size.setVisibility(View.GONE);
        tv_install_msg.append(logText + "\n");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_install:
                if (btnInstall.getText().toString().equalsIgnoreCase(getString(R.string.open_app))) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(apkCommander.getApkInfo().getPackageName());
                    startActivity(intent);
                    finish();
                } else {
                    apkCommander.startInstall();
                }
                break;
            case R.id.btn_silently:
                Intent intent = new Intent(this, BackgroundInstallActivity.class);
                intent.setData(getIntent().getData());
                startActivity(intent);
                finish();
                break;
            case R.id.perm_ll:
                if (tag_perm) {
                    main_rv.setVisibility(View.GONE);
                    perm_iv.setImageResource(R.drawable.ic_arrow_right);
                    tag_perm = false;
                } else {
                    main_rv.setVisibility(View.VISIBLE);
                    perm_iv.setImageResource(R.drawable.ic_arrow_open);
                    tag_perm = true;
                }
                break;
            case R.id.btn_cancel:
                if ((boolean)SPUtils.getData("auto_delete", false)) {
                    File file = new File(path_str);
                    file.delete();
                    ToastUtil.showToast(this, getString(R.string.apk_deleteed, apk_name), Toast.LENGTH_SHORT);
                }
                finish();
                break;
        }
    }

}
