package com.tokyonth.installer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.TranslateAnimation;

import com.google.android.material.appbar.AppBarLayout;
import com.kyleduo.switchbutton.SwitchButton;
import com.tokyonth.installer.base.BaseActivity;
import com.tokyonth.installer.Contents;
import com.tokyonth.installer.R;
import com.tokyonth.installer.adapter.ActivityAdapter;
import com.tokyonth.installer.bean.InitializeApk;
import com.tokyonth.installer.databinding.ActivityInstallerBinding;
import com.tokyonth.installer.widget.CustomizeDialog;
import com.tokyonth.installer.utils.helper.AppUtils;
import com.tokyonth.installer.utils.helper.VersionHelper;
import com.tokyonth.installer.adapter.PermissionAdapter;
import com.tokyonth.installer.apk.APKCommander;
import com.tokyonth.installer.bean.ApkInfoBean;
import com.tokyonth.installer.apk.CommanderCallback;
import com.tokyonth.installer.bean.permissions.PermFullBean;
import com.tokyonth.installer.widget.ProgressDrawable;
import com.tokyonth.installer.utils.ParsingContentUtil;
import com.tokyonth.installer.utils.file.FileUtils;
import com.tokyonth.installer.utils.helper.AssemblyUtils;
import com.tokyonth.installer.utils.file.SPUtils;

import java.io.File;
import java.util.ArrayList;

public class InstallerActivity extends BaseActivity implements CommanderCallback {

    private CardView perm_view;
    private CardView act_card;
    private RecyclerView act_rv;
    private RecyclerView perm_rv;
    private SwitchButton sb_auto_del;

    private ArrayList<PermFullBean> permFullBeanArrayList;
    private ArrayList<String> actStringArrayList;
    private ProgressDrawable progressDrawable;
    private PermissionAdapter permAdapter;
    private ActivityAdapter actAdapter;
    private APKCommander apkCommander;

    private String apkFilePath;
    private String apkFileName;
    private String apkSource;

    private boolean showPerm = false;
    private boolean showActivity = false;
    private boolean installComplete = false;

    private Uri uriData;
    private InitializeApk initializeApk;

    @Override
    public int setActivityView() {
        return R.layout.activity_installer;
    }

    @Override
    public void initActivity() {
        initView();
        initData();
    }

    private void initView() {
        perm_rv = findViewById(R.id.perm_rv);
        act_rv = findViewById(R.id.act_rv);
        perm_view = findViewById(R.id.card_perm);
        act_card = findViewById(R.id.card_act);
        sb_auto_del = findViewById(R.id.sb_auto_del);
    }

    private void initData() {
        ActivityInstallerBinding viewDataBinding = DataBindingUtil.setContentView(this, setActivityView());
        initializeApk = new InitializeApk();
        initializeApk.setCancelStr(getString(R.string.text_cancel_installation));
        initializeApk.setInstallIcon(getDrawable(R.drawable.ic_archive_24px));
        initializeApk.setInstallText(getString(R.string.install));



        viewDataBinding.setInitializeApk(initializeApk);
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
        sb_auto_del.setOnCheckedChangeListener((buttonView, isChecked) -> SPUtils.putData(Contents.SP_AUTO_DEL, isChecked));
    }

    private void setViewStatus() {
        int isShowPerm = (boolean)SPUtils.getData(Contents.SP_SHOW_PERM, true) ? View.VISIBLE : View.GONE;
        perm_view.setVisibility(isShowPerm);
        int isShowAct = (boolean)SPUtils.getData(Contents.SP_SHOW_ACT, true) ? View.VISIBLE : View.GONE;
        act_card.setVisibility(isShowAct);
        boolean isAutoDel = (boolean)SPUtils.getData(Contents.SP_AUTO_DEL, false);
        sb_auto_del.setChecked(isAutoDel);
    }

    private void initDetails(final ApkInfoBean apkInfo) {
        initializeApk.setBottomAppVisibility(true);
        initializeApk.setApkIcon(apkInfo.getIcon());
        initializeApk.setSourceApkIcon(AppUtils.getApplicationIconByPackageName(this, apkSource));

        Bitmap bitmap = AssemblyUtils.DrawableToBitmap(apkInfo.getIcon());
        Palette.generateAsync(bitmap, palette -> {
            assert palette != null;
            Palette.Swatch vibrantSwatch = palette.getLightVibrantSwatch();
            int color;
            if (vibrantSwatch != null) {
                color = AssemblyUtils.ColorBurn(vibrantSwatch.getRgb());
            } else {
                color = getResources().getColor(R.color.colorAccent);
            }
            initializeApk.setApkVersionTipsColor(color);
            AppBarLayout targetView = findViewById(R.id.app_bar_layout);
            final int width = targetView.getMeasuredWidth();
            final int height = targetView.getMeasuredHeight();
            Animator animator = ViewAnimationUtils.createCircularReveal(targetView, width/2, height/2, 0, height);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    targetView.setBackgroundColor(color);
                }
            });
            animator.setDuration(500);
            animator.start();
        });

        apkFileName = apkInfo.getAppName();
        apkFilePath = apkInfo.getApkFile().getPath();
        perm_view.setVisibility(View.VISIBLE);
        act_card.setVisibility(View.VISIBLE);


        initializeApk.setApkName(apkInfo.getAppName());
        initializeApk.setApkVersion(apkInfo.getVersion());
        initializeApk.setApkSource(getString(R.string.text_apk_source, AppUtils.getApplicationNameByPackageName(this, apkSource)));
        initializeApk.setApkInstallMsg(getResources().getString(R.string.info_pkg_name) + apkInfo.getPackageName() + "\n" +
                getResources().getString(R.string.info_apk_path) + apkFilePath + "\n" +
                getResources().getString(R.string.text_size, FileUtils.byteToString(FileUtils.getFileSize(apkFilePath))));
        if (apkInfo.hasInstalledApp()) {
            initializeApk.appendApkInstallMsg("\n" + getResources().getString(R.string.info_installed_version) + apkInfo.getInstalledVersion());

            CardView cardView = findViewById(R.id.tip_card);
            cardView.setAlpha(0.70f);
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -200, 0);
            translateAnimation.setDuration(500);
            cardView.setAnimation(translateAnimation);
            cardView.startAnimation(translateAnimation);
            cardView.setVisibility(View.VISIBLE);
            initializeApk.setApkVersionTips(VersionHelper.CheckVer(this, apkInfo.getVersionCode(), apkInfo.getInstalledVersionCode()));
        }

        if ((boolean)SPUtils.getData(Contents.SP_SHOW_PERM, true)) {
            perm_view.setVisibility(View.VISIBLE);
            if (apkInfo.getPermissions() != null && apkInfo.getPermissions().length > 0) {
                for (int i = 0; i < apkInfo.getPermissions().length; i++) {
                    if ((boolean)SPUtils.getData(Contents.SP_SHOW_PERM, true)) {
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
        if ((boolean)SPUtils.getData(Contents.SP_SHOW_ACT, true)) {
            act_card.setVisibility(View.VISIBLE);
            if (apkInfo.getActivities() != null && apkInfo.getActivities().size() > 0) {
                actStringArrayList.addAll(apkInfo.getActivities());
            }
        } else {
            act_card.setVisibility(View.GONE);
        }
        permAdapter.notifyDataSetChanged();
        actAdapter.notifyDataSetChanged();

        initializeApk.setPermIndex(getString(R.string.app_permissions, String.valueOf(permAdapter.getItemCount())));
        initializeApk.setActIndex(getString(R.string.app_act, String.valueOf(actAdapter.getItemCount())));
    }

    @Override
    public void onStartParseApk(Uri uri) {
        initializeApk.setInstallVisibility(false);
    }

    @Override
    public void onApkParsed(ApkInfoBean apkInfo) {
        if (apkInfo != null && !TextUtils.isEmpty(apkInfo.getPackageName())) {
            initDetails(apkInfo);
            initializeApk.setInstallVisibility(true);
        } else {
            Uri uri = getIntent().getData();
            String str = null;
            if (uri != null) {
                str = uri.toString();
            }
            initializeApk.setApkInstallMsg(getString(R.string.parse_apk_failed, str));
        }
    }

    @Override
    public void onApkPreInstall(ApkInfoBean apkInfo) {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        perm_view.setVisibility(View.GONE);
        act_card.setVisibility(View.GONE);
        initializeApk.setInstallEnable(false);
        initializeApk.setCancelVisibility(false);
        initializeApk.setSilentlyVisibility(false);

        progressDrawable = new ProgressDrawable();
        progressDrawable.putColor(Color.WHITE);
        progressDrawable.animatorDuration(1500);
        progressDrawable.start();
        initializeApk.setInstallIcon(progressDrawable);
        initializeApk.setInstallText(getString(R.string.installing));
        initializeApk.setApkInstallMsg("");
    }

    @Override
    public void onApkInstalled(ApkInfoBean apkInfo, int resultCode) {
        if (resultCode == 0) {
            findViewById(R.id.card_del).setVisibility(View.VISIBLE);
            showToast(getString(R.string.apk_installed, apkInfo.getAppName()));
            if ((boolean)SPUtils.getData(Contents.SP_VIBRATE, false)) {
                Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
                assert vibrator != null;
                vibrator.vibrate(800);
            }
            progressDrawable.stop();
            initializeApk.setApkName(getString(R.string.successful));
            initializeApk.setInstallIcon(getDrawable(R.drawable.ic_offline_bolt_24px));
            initializeApk.setInstallText(getString(R.string.open_app));
            initializeApk.setInstallEnable(true);
            initializeApk.setCancelVisibility(true);
            initializeApk.setCancelStr(getString(R.string.back));
        } else {
            progressDrawable.stop();
            initializeApk.setInstallIcon(getDrawable(R.drawable.ic_cancel_24px));
            initializeApk.setInstallText(getString(R.string.failed));
            initializeApk.setApkName(getString(R.string.failed));
            initializeApk.setCancelVisibility(true);

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
        initializeApk.appendApkInstallMsg(logText);
    }

    private void isAutoDel() {
        if ((boolean)SPUtils.getData(Contents.SP_AUTO_DEL, false)) {
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
        if (showPerm) {
            perm_rv.setVisibility(View.GONE);
            initializeApk.setPermArrowDirect(R.drawable.ic_arrow_right);
            showPerm = false;
        } else {
            perm_rv.setVisibility(View.VISIBLE);
            initializeApk.setPermArrowDirect(R.drawable.ic_arrow_open);
            showPerm = true;
        }
    }

    public void isShowAct(View view) {
        if (showActivity) {
            act_rv.setVisibility(View.GONE);
            initializeApk.setActArrowDirect(R.drawable.ic_arrow_right);
            showActivity = false;
        } else {
            act_rv.setVisibility(View.VISIBLE);
            initializeApk.setActArrowDirect(R.drawable.ic_arrow_open);
            showActivity = true;
        }
    }

    public void isNightMode(View view) {
        if ((boolean)SPUtils.getData(Contents.SP_NIGHT_MODE, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            SPUtils.putData(Contents.SP_NIGHT_MODE, false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            SPUtils.putData(Contents.SP_NIGHT_MODE, true);
        }
    }

    public void settingsButton(View view) {
        Intent intent_settings = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent_settings, 100);
    }

    public void toSourceApkSettings(View view) {
        AppUtils.toSelfSetting(this, apkSource);
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
