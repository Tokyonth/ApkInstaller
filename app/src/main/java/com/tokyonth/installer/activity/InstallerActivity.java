package com.tokyonth.installer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.kyleduo.switchbutton.SwitchButton;
import com.tokyonth.installer.base.BaseActivity;
import com.tokyonth.installer.Constants;
import com.tokyonth.installer.R;
import com.tokyonth.installer.adapter.ActivityAdapter;
import com.tokyonth.installer.widget.CircleImageView;
import com.tokyonth.installer.widget.CustomizeDialog;
import com.tokyonth.installer.utils.GetAppInfoUtils;
import com.tokyonth.installer.utils.VersionHelper;
import com.tokyonth.installer.adapter.PermissionAdapter;
import com.tokyonth.installer.apk.APKCommander;
import com.tokyonth.installer.bean.ApkInfoBean;
import com.tokyonth.installer.apk.CommanderCallback;
import com.tokyonth.installer.bean.permissions.PermFullBean;
import com.tokyonth.installer.widget.ProgressDrawable;
import com.tokyonth.installer.utils.ParsingContentUtil;
import com.tokyonth.installer.utils.FileUtils;
import com.tokyonth.installer.utils.AssemblyUtils;
import com.tokyonth.installer.utils.SPUtils;

import java.io.File;
import java.util.ArrayList;

public class InstallerActivity extends BaseActivity implements CommanderCallback {

    private SwitchButton sb_auto_del;
    private RecyclerView perm_rv;
    private RecyclerView act_rv;
    private CardView perm_view;
    private CardView act_card;

    private ArrayList<PermFullBean> permFullBeanArrayList;
    private ArrayList<String> actStringArrayList;
    private ProgressDrawable progressDrawable;
    private PermissionAdapter permAdapter;
    private ActivityAdapter actAdapter;
    private APKCommander apkCommander;

    private String apkFilePath;
    private String apkFileName;
    private String apkSource;

    private boolean installComplete = false;
    private boolean showActivity = false;
    private boolean showPerm = false;

    private Uri uriData;

    private ExtendedFloatingActionButton fabInstall;
    private CircleImageView apkSourceIcon;
    private BottomAppBar bottomAppBar;
    private TextView tvVersionTips;
    private TextView tvApkSource;
    private TextView tvAppVersion;
    private TextView tvInstallMsg;
    private TextView tvAppName;
    private TextView tvSilently;
    private TextView tvCancel;

    @Override
    public int setActivityView() {
        return R.layout.activity_installer;
    }

    @Override
    public void initActivity(@Nullable Bundle savedInstanceState) {
        initView();
        initData();
    }

    private void initView() {
        perm_rv = findViewById(R.id.perm_rv);
        act_rv = findViewById(R.id.act_rv);
        perm_view = findViewById(R.id.card_perm);
        act_card = findViewById(R.id.card_act);
        sb_auto_del = findViewById(R.id.sb_auto_del);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        apkSourceIcon = findViewById(R.id.iv_apk_source);
        tvApkSource = findViewById(R.id.tv_apk_source);
        tvVersionTips = findViewById(R.id.tv_version_tips);
        tvAppName = findViewById(R.id.tv_app_name);
        tvAppVersion = findViewById(R.id.tv_app_version);
        tvInstallMsg = findViewById(R.id.tv_install_msg);
        fabInstall = findViewById(R.id.fab_install);
        tvCancel = findViewById(R.id.tv_cancel);
        tvSilently = findViewById(R.id.tv_silently);

        bottomAppBar.setVisibility(View.INVISIBLE);
    }

    private void initData() {
        uriData = getIntent().getData();
        apkSource = ParsingContentUtil.reflectGetReferrer(this);
        apkCommander = new APKCommander(InstallerActivity.this, uriData, this, apkSource);
        permFullBeanArrayList = new ArrayList<>();
        actStringArrayList = new ArrayList<>();
        permAdapter = new PermissionAdapter(permFullBeanArrayList, this);
        actAdapter = new ActivityAdapter(actStringArrayList);
        perm_rv.setLayoutManager(new LinearLayoutManager(this));
        act_rv.setLayoutManager(new LinearLayoutManager(this));
        perm_rv.setAdapter(permAdapter);
        act_rv.setAdapter(actAdapter);
        sb_auto_del.setOnCheckedChangeListener((buttonView, isChecked) -> SPUtils.putData(Constants.SP_AUTO_DEL, isChecked));

        tvApkSource.setText(getString(R.string.text_apk_source, GetAppInfoUtils.getApplicationNameByPackageName(this, apkSource)));
        apkSourceIcon.setImageDrawable(GetAppInfoUtils.getApplicationIconByPackageName(this, apkSource));
    }

    private void setViewStatus() {
        int isShowPerm = (boolean) SPUtils.getData(Constants.SP_SHOW_PERM, true) ? View.VISIBLE : View.GONE;
        perm_view.setVisibility(isShowPerm);
        int isShowAct = (boolean) SPUtils.getData(Constants.SP_SHOW_ACT, true) ? View.VISIBLE : View.GONE;
        act_card.setVisibility(isShowAct);
        boolean isAutoDel = (boolean) SPUtils.getData(Constants.SP_AUTO_DEL, false);
        sb_auto_del.setChecked(isAutoDel);
    }

    @SuppressLint("SetTextI18n")
    private void initDetails(final ApkInfoBean apkInfo) {
        ImageView ivAppIcon = findViewById(R.id.iv_app_icon);
        ivAppIcon.setImageDrawable(apkInfo.getIcon());
        Bitmap bitmap = AssemblyUtils.DrawableToBitmap(apkInfo.getIcon());
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                assert palette != null;
                Palette.Swatch vibrantSwatch = palette.getLightVibrantSwatch();
                int color;
                if (vibrantSwatch != null) {
                    color = AssemblyUtils.ColorBurn(vibrantSwatch.getRgb());
                } else {
                    color = getResources().getColor(R.color.colorAccent);
                }
                tvVersionTips.setTextColor(color);
                AppBarLayout targetView = findViewById(R.id.app_bar_layout);
                final int width = targetView.getMeasuredWidth();
                final int height = targetView.getMeasuredHeight();
                Animator animator = ViewAnimationUtils.createCircularReveal(targetView, width / 2, height / 2, 0, height);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        targetView.setBackgroundColor(color);
                    }
                });
                animator.setDuration(500);
                animator.start();
            }
        });

        bottomAppBar.setVisibility(View.VISIBLE);
        perm_view.setVisibility(View.VISIBLE);
        act_card.setVisibility(View.VISIBLE);
        apkFileName = apkInfo.getAppName();
        apkFilePath = apkInfo.getApkFile().getPath();

        tvAppName.setText(apkInfo.getAppName());
        tvAppVersion.setText(apkInfo.getVersion());

        tvInstallMsg.setText(getResources().getString(R.string.info_pkg_name) + apkInfo.getPackageName() + "\n" +
                getResources().getString(R.string.info_apk_path) + apkFilePath + "\n" +
                getResources().getString(R.string.text_size, FileUtils.byteToString(FileUtils.getFileSize(apkFilePath))));
        if (apkInfo.hasInstalledApp()) {
            tvInstallMsg.append("\n" + getResources().getString(R.string.info_installed_version) + apkInfo.getInstalledVersion());
            CardView cardView = findViewById(R.id.tip_card);
            cardView.setAlpha(0.70f);
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -200, 0);
            translateAnimation.setDuration(500);
            cardView.setAnimation(translateAnimation);
            cardView.startAnimation(translateAnimation);
            cardView.setVisibility(View.VISIBLE);
            tvVersionTips.setText(VersionHelper.CheckVer(this, apkInfo.getVersionCode(), apkInfo.getInstalledVersionCode()));
        }

        if ((boolean) SPUtils.getData(Constants.SP_SHOW_PERM, true)) {
            perm_view.setVisibility(View.VISIBLE);
            if (apkInfo.getPermissions() != null && apkInfo.getPermissions().length > 0) {
                for (int i = 0; i < apkInfo.getPermissions().length; i++) {
                    if ((boolean) SPUtils.getData(Constants.SP_SHOW_PERM, true)) {
                        permFullBeanArrayList.add(new PermFullBean(apkInfo.getPermissions()[i],
                                apkCommander.getPermInfo().getPermissionGroup().get(i),
                                apkCommander.getPermInfo().getPermissionDescription().get(i),
                                apkCommander.getPermInfo().getPermissionLabel().get(i)));
                    }
                }
            }
        } else {
            perm_view.setVisibility(View.GONE);
        }
        if ((boolean) SPUtils.getData(Constants.SP_SHOW_ACT, true)) {
            act_card.setVisibility(View.VISIBLE);
            if (apkInfo.getActivities() != null && apkInfo.getActivities().size() > 0) {
                actStringArrayList.addAll(apkInfo.getActivities());
            }
        } else {
            act_card.setVisibility(View.GONE);
        }
        permAdapter.notifyDataSetChanged();
        actAdapter.notifyDataSetChanged();

        TextView tvPermQuantity = findViewById(R.id.tv_perm_quantity);
        TextView tvActQuantity = findViewById(R.id.tv_act_quantity);
        tvPermQuantity.setText(getString(R.string.app_permissions, String.valueOf(permAdapter.getItemCount())));
        tvActQuantity.setText(getString(R.string.app_act, String.valueOf(actAdapter.getItemCount())));
    }

    @Override
    public void onStartParseApk(Uri uri) {
        fabInstall.setVisibility(View.GONE);
    }

    @Override
    public void onApkParsed(ApkInfoBean apkInfo) {
        if (apkInfo != null && !TextUtils.isEmpty(apkInfo.getPackageName())) {
            initDetails(apkInfo);
            fabInstall.setVisibility(View.VISIBLE);
        } else {
            Uri uri = getIntent().getData();
            String str = null;
            if (uri != null) {
                str = uri.toString();
            }
            tvInstallMsg.setText(getString(R.string.parse_apk_failed, str));
        }
    }

    @Override
    public void onApkPreInstall(ApkInfoBean apkInfo) {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        perm_view.setVisibility(View.GONE);
        act_card.setVisibility(View.GONE);
        fabInstall.setEnabled(false);
        tvCancel.setVisibility(View.GONE);
        tvSilently.setVisibility(View.GONE);

        progressDrawable = new ProgressDrawable();
        progressDrawable.putColor(Color.WHITE);
        progressDrawable.animatorDuration(1500);
        progressDrawable.start();
        fabInstall.setIcon(progressDrawable);
        tvAppName.setText(getString(R.string.installing));
        tvInstallMsg.setText("");
    }

    @Override
    public void onApkInstalled(ApkInfoBean apkInfo, int resultCode) {
        if (resultCode == 0) {
            findViewById(R.id.card_del).setVisibility(View.VISIBLE);
            showToast(getString(R.string.apk_installed, apkInfo.getAppName()));
            if ((boolean) SPUtils.getData(Constants.SP_VIBRATE, false)) {
                Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
                assert vibrator != null;
                vibrator.vibrate(800);
            }
            progressDrawable.stop();
            tvAppName.setText(getString(R.string.successful));
            fabInstall.setIcon(getDrawable(R.drawable.ic_offline_bolt_24px));
            fabInstall.setText(getString(R.string.open_app));
            fabInstall.setEnabled(true);
            tvCancel.setVisibility(View.VISIBLE);
            tvCancel.setText(getString(R.string.back));
        } else {
            progressDrawable.stop();
            fabInstall.setIcon(getDrawable(R.drawable.ic_cancel_24px));
            fabInstall.setText(getString(R.string.failed));
            tvAppName.setText(getString(R.string.failed));
            tvCancel.setVisibility(View.VISIBLE);

            CustomizeDialog.getInstance(this)
                    .setTitle(R.string.dialog_text_title)
                    .setMessage(R.string.use_system_pkg)
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                        AssemblyUtils.StartSystemPkgInstall(InstallerActivity.this, apkFilePath);
                        finish();
                    })
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .setCancelable(false).create().show();
        }
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        installComplete = true;
    }

    @Override
    public void onInstallLog(ApkInfoBean apkInfo, String logText) {
        tvInstallMsg.append(logText);
    }

    private void isAutoDel() {
        if ((boolean) SPUtils.getData(Constants.SP_AUTO_DEL, false)) {
            if (new File(apkFilePath).delete()) {
                showToast(getString(R.string.apk_deleted, apkFileName));
            }
        }
    }

    public void installFab(View view) {
        if (installComplete) {
            startActivity(getPackageManager().getLaunchIntentForPackage(apkCommander.getApkInfo().getPackageName()));
            isAutoDel();
            finish();
        } else {
            apkCommander.startInstall();
        }
    }

    public void cancelButton(View view) {
        isAutoDel();
        finish();
    }

    public void silentlyButton(View view) {
        Intent intent = new Intent(InstallerActivity.this, SilentlyInstallActivity.class);
        intent.setData(uriData);
        intent.putExtra("apkSource", apkSource);
        startActivity(intent);
        finish();
    }

    public void isShowPerm(View view) {
        ImageView iv = findViewById(R.id.iv_perm_arrow);
        if (showPerm) {
            perm_rv.setVisibility(View.GONE);
            iv.setImageResource(R.drawable.ic_arrow_right);
            showPerm = false;
        } else {
            perm_rv.setVisibility(View.VISIBLE);
            iv.setImageResource(R.drawable.ic_arrow_open);
            showPerm = true;
        }
    }

    public void isShowAct(View view) {
        ImageView iv = findViewById(R.id.iv_act_arrow);
        if (showActivity) {
            act_rv.setVisibility(View.GONE);
            iv.setImageResource(R.drawable.ic_arrow_right);
            showActivity = false;
        } else {
            act_rv.setVisibility(View.VISIBLE);
            iv.setImageResource(R.drawable.ic_arrow_open);
            showActivity = true;
        }
    }

    public void isNightMode(View view) {
        if ((boolean) SPUtils.getData(Constants.SP_NIGHT_MODE, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            SPUtils.putData(Constants.SP_NIGHT_MODE, false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            SPUtils.putData(Constants.SP_NIGHT_MODE, true);
        }
    }

    public void settingsButton(View view) {
        startActivityForResult(new Intent(this, SettingsActivity.class), 100);
    }

    public void toSourceApkSettings(View view) {
        GetAppInfoUtils.toSelfSetting(this, apkSource);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (installComplete)
            setViewStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apkCommander.getApkInfo() != null && apkCommander.getApkInfo().isFakePath()) {
            if (!apkCommander.getApkInfo().getApkFile().delete()) {
                Log.e("InstallerActivity", "failed to delete！");
            }
        }
    }

}
