package com.tokyonth.installer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.florent37.runtimepermission.RuntimePermission;
import com.github.florent37.runtimepermission.callbacks.PermissionListener;
import com.tokyonth.installer.activity.BackgroundInstallActivity;
import com.tokyonth.installer.activity.SettingsActivity;
import com.tokyonth.installer.apk.APKCommander;
import com.tokyonth.installer.apk.ApkInfo;
import com.tokyonth.installer.apk.ICommanderCallback;
import com.tokyonth.installer.settings.Prefs;

import java.util.List;

public class InstallerActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        ICommanderCallback, View.OnClickListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private TextView titleName;
    private ImageView icon;

    private RelativeLayout mTitleContainer;
    private AppBarLayout mAppBarLayout;

    private Toolbar toolbar;
    private TextView tvAppName;
    private TextView tvAppVer;
    private LinearLayout layoutAppDetails;
    private ImageView imgAppIcon;

    private ProgressBar progressBar;
    private LinearLayout layoutPermissionList;
    private LinearLayout layoutButtons;

    private Button btnInstall;
    private Button btnSilently;
    private Button btnCancel;

    private APKCommander apkCommander;

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
            checkPermission();
        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        mAppBarLayout = findViewById(R.id.appBar_layout);
        layoutAppDetails = findViewById(R.id.layout_app_details);
        tvAppName = findViewById(R.id.tv_app_name);
        tvAppVer = findViewById(R.id.tv_app_ver);
        imgAppIcon = findViewById(R.id.icon);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        btnInstall = findViewById(R.id.btn_install);
        btnSilently = findViewById(R.id.btn_silently);
        btnCancel = findViewById(R.id.btn_cancel);
        icon = findViewById(R.id.icon);
        titleName = findViewById(R.id.title_name);
        mTitleContainer = findViewById(R.id.title_container);
        setSupportActionBar(toolbar);
        mAppBarLayout.addOnOffsetChangedListener(this);

        Glide.with(this).load(R.mipmap.ic_launcher).into(icon);
        startAlphaAnimation(titleName, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
        mIsTheTitleVisible = true;
        tvAppName.setText(R.string.parsing);

        layoutButtons = (LinearLayout) btnInstall.getParent();
        btnInstall.setEnabled(true);
        btnInstall.setOnClickListener(this);
        btnSilently.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;
        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!mIsTheTitleVisible) {
                startAlphaAnimation(titleName, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }
        } else {
            if (mIsTheTitleVisible) {
                titleName.setVisibility(View.VISIBLE);
                startAlphaAnimation(titleName, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }
        } else {
            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
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
    }

    private LinearLayout createAppPermissionView(String perm) {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.info_item_permission, null, false);
        TextView tv1 = (TextView) layout.getChildAt(0);
        tv1.setText(perm);
        return layout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadSettings();
    }

    private void checkPermission() {
        RuntimePermission.askPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .ask(new PermissionListener() {
                    @Override
                    public void onAccepted(RuntimePermission runtimePermission, List<String> accepted) {
                        apkCommander = new APKCommander(InstallerActivity.this, getIntent().getData(), InstallerActivity.this);
                    }

                    @Override
                    public void onDenied(RuntimePermission runtimePermission, List<String> denied, List<String> foreverDenied) {
                        Toast.makeText(InstallerActivity.this, R.string.no_permissions, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void initDetails(ApkInfo apkInfo) {
        layoutAppDetails.removeAllViews();
        tvAppName.setText(apkInfo.getAppName());
        tvAppVer.setText(apkInfo.getVersion());
        titleName.setText(apkInfo.getAppName());

        Glide.with(this).load(apkInfo.getIcon()).into(imgAppIcon);
        layoutAppDetails.addView(createAppInfoView(getString(R.string.info_pkg_name), apkInfo.getPackageName()));
        layoutAppDetails.addView(createAppInfoView(getString(R.string.info_apk_path), apkInfo.getApkFile().getPath()));
        if (apkInfo.hasInstalledApp()) {
            layoutAppDetails.addView(createAppInfoView(getString(R.string.info_installed_version), apkInfo.getInstalledVersion()));
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
        if (Prefs.getPreference(this).getBoolean("show_perm", true)) {
            if (apkInfo.getPermissions() != null && apkInfo.getPermissions().length > 0) {
                layoutPermissionList = new LinearLayout(this);
                layoutPermissionList.setOrientation(LinearLayout.VERTICAL);
                layoutPermissionList.addView(createAppInfoView(null, getString(R.string.app_permissions)));
                for (String perm : apkInfo.getPermissions()) {
                    layoutPermissionList.addView(createAppPermissionView(perm));
                }
                layoutAppDetails.addView(layoutPermissionList);
            }
        }
    }

    private LinearLayout createAppInfoView(String key, String value) {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.info_item, null, false);
        TextView tv1 = (TextView) layout.getChildAt(0);
        TextView tv2 = (TextView) layout.getChildAt(1);
        tv1.setText(key);
        tv2.setText(value);
        if (TextUtils.isEmpty(value)) {
            layout.removeView(tv2);
            tv1.setTypeface(Typeface.MONOSPACE);
            tv1.setGravity(Gravity.START);
        }
        if (TextUtils.isEmpty(key)) {
            layout.removeView(tv2);
            tv1.setText(value);
        }
        return layout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apkCommander.getApkInfo() != null && apkCommander.getApkInfo().isFakePath())
            apkCommander.getApkInfo().getApkFile().delete();
    }

    @Override
    public void onStartParseApk(Uri uri) {
        TextView textView = new TextView(this);
        textView.setTextColor(Color.RED);
        textView.setText(getString(R.string.parsing) + " : " + uri.toString());
        layoutAppDetails.addView(textView);
        btnInstall.setVisibility(View.GONE);
    }

    @Override
    public void onApkParsed(ApkInfo apkInfo) {
        if (apkInfo != null && !TextUtils.isEmpty(apkInfo.getPackageName())) {
            initDetails(apkInfo);
            btnInstall.setVisibility(View.VISIBLE);
        } else {
            Uri uri = getIntent().getData();
            String s = null;
            if (uri != null)
                s = uri.toString();
            TextView textView = new TextView(this);
            textView.setTextColor(Color.RED);
            textView.setText(getString(R.string.parse_apk_failed, s));
            layoutAppDetails.addView(textView);
        }
    }

    @Override
    public void onApkPreInstall(ApkInfo apkInfo) {
        if (layoutPermissionList != null)
            layoutAppDetails.removeView(layoutPermissionList);
        tvAppName.setText(R.string.installing);
        btnInstall.setEnabled(false);
        btnSilently.setEnabled(false);
        progressBar.setVisibility(
                Prefs.getPreference(this).getBoolean("show_progress_bar", true) ?
                        View.VISIBLE : View.INVISIBLE);
        layoutButtons.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onApkInstalled(ApkInfo apkInfo, int resultCode) {
        getString(R.string.install_finished_with_result_code, resultCode);
        btnInstall.setEnabled(false);
        btnSilently.setEnabled(false);
        if (resultCode == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.apk_installed, apkInfo.getAppName()), Toast.LENGTH_SHORT).show();
            tvAppName.setText(R.string.successful);
            btnInstall.setEnabled(true);
            btnInstall.setText(R.string.open_app);
            btnCancel.setText(R.string.text_install_complete);
            btnSilently.setVisibility(View.GONE);
            if (!apkInfo.isFakePath() && Prefs.getPreference(this).getBoolean("auto_delete", false)) {
                Toast.makeText(this, getString(R.string.apk_deleteed, apkInfo.getApkFile().getName()), Toast.LENGTH_SHORT).show();
            }
        } else {
            tvAppName.setText(R.string.failed);
        }
        progressBar.setVisibility(View.INVISIBLE);
        layoutButtons.setVisibility(View.VISIBLE);
    }

    @Override
    public void onInstallLog(ApkInfo apkInfo, String logText) {
        layoutAppDetails.addView(createAppInfoView(logText, null));
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
            case R.id.btn_cancel:
                finish();
                break;
        }
    }

}
