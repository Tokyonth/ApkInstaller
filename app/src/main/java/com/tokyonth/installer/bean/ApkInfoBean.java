package com.tokyonth.installer.bean;

import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.List;

public class ApkInfoBean {

    private File apkFile;
    private String appName;
    private Drawable icon;
    private String versionName;
    private int versionCode;
    private String packageName;
    private boolean hasInstalledApp;
    private String installedVersionName;
    private int installedVersionCode;
    private boolean isFakePath;

    private String[] permissions;
    private List<String> activities;

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public boolean isHasInstalledApp() {
        return hasInstalledApp;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public void setApkFile(File apkFile) {
        this.apkFile = apkFile;
    }

    public boolean isFakePath() {
        return isFakePath;
    }

    public void setFakePath(boolean fakePath) {
        isFakePath = fakePath;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return versionName + "(" + versionCode + ")";
    }

    public String getInstalledVersion() {
        return hasInstalledApp ? installedVersionName + "(" + installedVersionCode + ")" : "NO";
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean hasInstalledApp() {
        return hasInstalledApp;
    }

    public void setHasInstalledApp(boolean hasInstalledApp) {
        this.hasInstalledApp = hasInstalledApp;
    }

    public String getInstalledVersionName() {
        return installedVersionName;
    }

    public void setInstalledVersionName(String installedVersionName) {
        this.installedVersionName = installedVersionName;
    }

    public int getInstalledVersionCode() {
        return installedVersionCode;
    }

    public void setInstalledVersionCode(int installedVersionCode) {
        this.installedVersionCode = installedVersionCode;
    }

    public String getAppName() {
        return appName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public File getApkFile() {
        return apkFile;
    }

    public String getFileName() {
        return apkFile == null ? null : apkFile.getName();
    }

}
