package com.tokyonth.installer.apk;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.AndroidRuntimeException;

import com.tokyonth.installer.bean.ApkInfoBean;
import com.tokyonth.installer.bean.permissions.PermInfoBean;
import com.tokyonth.installer.utils.FileProviderPathUtil;
import com.tokyonth.installer.utils.ParsingContentUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class ParseApkTask extends Thread {

    private Uri uri;
    private Handler handler;
    private Context context;
    private String referrer;
    private PackageManager packageManager;
    private CommanderCallback commanderCallback;

    private PermInfoBean permInfo;
    private ApkInfoBean mApkInfo;

    void StartParseApkTask(Uri uri, Context context, Handler handler, CommanderCallback commanderCallback, String referrer) {
        this.uri = uri;
        this.handler = handler;
        this.context = context;
        this.referrer = referrer;
        this.commanderCallback = commanderCallback;
        packageManager = context.getPackageManager();
    }

    protected abstract void setApkInfo(ApkInfoBean mApkInfo);

    protected abstract void setPermInfo(PermInfoBean permInfo);

    @Override
    public void run() {
        super.run();
        try {
            handler.post(() -> commanderCallback.onStartParseApk(uri));
            mApkInfo = new ApkInfoBean();
            permInfo = new PermInfoBean();

            File QueryContent = ParsingContentUtil.getFile(context, uri);
            String apkSourcePath = (QueryContent == null) ?
                    FileProviderPathUtil.getFilePath(context, uri, referrer) : QueryContent.getPath();
            mApkInfo.setApkFile(new File(apkSourcePath));

            PackageInfo pkgInfo = packageManager.getPackageArchiveInfo(mApkInfo.getApkFile().getPath(), PackageManager.GET_ACTIVITIES);
            if (pkgInfo != null) {
                pkgInfo.applicationInfo.sourceDir = mApkInfo.getApkFile().getPath();
                pkgInfo.applicationInfo.publicSourceDir = mApkInfo.getApkFile().getPath();
                mApkInfo.setAppName(packageManager.getApplicationLabel(pkgInfo.applicationInfo).toString());
                mApkInfo.setPackageName(pkgInfo.applicationInfo.packageName);
                mApkInfo.setVersionName(pkgInfo.versionName);
                mApkInfo.setVersionCode(pkgInfo.versionCode);
                mApkInfo.setIcon(pkgInfo.applicationInfo.loadIcon(packageManager));

                ArrayList<String> activity_list = new ArrayList<>();
                if (pkgInfo.activities != null) {
                    for (ActivityInfo activity : pkgInfo.activities) {
                        activity_list.add(activity.name);
                    }
                    mApkInfo.setActivities(activity_list);
                }
                try {
                    PackageInfo installedPkgInfo = packageManager.getPackageInfo(mApkInfo.getPackageName(), PackageManager.GET_CONFIGURATIONS);
                    mApkInfo.setInstalledVersionName(installedPkgInfo.versionName);
                    mApkInfo.setInstalledVersionCode(installedPkgInfo.versionCode);
                    mApkInfo.setHasInstalledApp(true);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    mApkInfo.setHasInstalledApp(false);
                }
                PackageInfo PermPkgInfo = packageManager.getPackageArchiveInfo(mApkInfo.getApkFile().getPath(), PackageManager.GET_PERMISSIONS);
                assert PermPkgInfo != null;
                mApkInfo.setPermissions(PermPkgInfo.requestedPermissions);
                List<String> str_list = new ArrayList<>();
                if (PermPkgInfo.requestedPermissions != null) {
                    Collections.addAll(str_list, PermPkgInfo.requestedPermissions);
                    getPermissionInfo(str_list);
                }
            }
            handler.post(() -> commanderCallback.onApkParsed(mApkInfo));
            setApkInfo(mApkInfo);
        } catch (Exception e) {
            handler.post(() -> commanderCallback.onApkParsed(null));
            e.printStackTrace();
            throw new AndroidRuntimeException(e);
        }
    }

    private void getPermissionInfo(List<String> permission) {
        ArrayList<String> Group = new ArrayList<>();
        ArrayList<String> Label = new ArrayList<>();
        ArrayList<String> Description = new ArrayList<>();
        for (String str : permission) {
            try {
                PermissionInfo permissionInfo = packageManager.getPermissionInfo(str, 0);
                assert permissionInfo.group != null;
                PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
                Group.add(permissionGroupInfo.loadLabel(packageManager).toString());

                String permissionLabel = permissionInfo.loadLabel(packageManager).toString();
                Label.add(permissionLabel);

                String permissionDescription = Objects.requireNonNull(permissionInfo.loadDescription(packageManager)).toString();
                Description.add(permissionDescription);

            } catch (PackageManager.NameNotFoundException e) {
                Description.add(null);
                Label.add(null);
                Group.add(null);
            }
        }
        permInfo.setPermissionDescription(Description);
        permInfo.setPermissionGroup(Group);
        permInfo.setPermissionLabel(Label);
        setPermInfo(permInfo);
    }

}
