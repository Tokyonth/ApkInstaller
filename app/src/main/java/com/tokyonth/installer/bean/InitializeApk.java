package com.tokyonth.installer.bean;

import android.graphics.drawable.Drawable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.tokyonth.installer.BR;

public class InitializeApk extends BaseObservable {

    private String apkName;
    private String apkVersion;
    private String apkSource;
    private String apkInstallMsg;
    private Drawable apkIcon;

    private Drawable sourceApkIcon;

    private String apkVersionTips;
    private int apkVersionTipsColor;

    private String permIndex;
    private String actIndex;

    private String cancelStr;

    private boolean cancelVisibility = true;
    private boolean silentlyVisibility = true;

    private int permArrowDirect;
    private int actArrowDirect;

    private boolean installVisibility = true;
    private boolean installEnable = true;
    private Drawable installIcon;

    @Bindable
    public boolean isInstallVisibility() {
        return installVisibility;
    }

    public void setInstallVisibility(boolean installVisibility) {
        this.installVisibility = installVisibility;
        notifyPropertyChanged(BR.installVisibility);
    }

    @Bindable
    public boolean isInstallEnable() {
        return installEnable;
    }

    public void setInstallEnable(boolean installEnable) {
        this.installEnable = installEnable;
        notifyPropertyChanged(BR.installEnable);
    }

    @Bindable
    public Drawable getInstallIcon() {
        return installIcon;
    }

    public void setInstallIcon(Drawable installIcon) {
        this.installIcon = installIcon;
        notifyPropertyChanged(BR.installIcon);
    }

    @Bindable
    public String getInstallText() {
        return installText;
    }

    public void setInstallText(String installText) {
        this.installText = installText;
        notifyPropertyChanged(BR.installText);
    }

    private String installText;

    @Bindable
    public boolean isBottomAppVisibility() {
        return bottomAppVisibility;
    }

    public void setBottomAppVisibility(boolean bottomAppVisibility) {
        this.bottomAppVisibility = bottomAppVisibility;
        notifyPropertyChanged(BR.bottomAppVisibility);
    }

    private boolean bottomAppVisibility = false;


    @Bindable
    public Drawable getApkIcon() {
        return apkIcon;
    }

    public void setApkIcon(Drawable apkIcon) {
        this.apkIcon = apkIcon;
        notifyPropertyChanged(BR.apkIcon);
    }

    @Bindable
    public Drawable getSourceApkIcon() {
        return sourceApkIcon;
    }

    public void setSourceApkIcon(Drawable sourceApkIcon) {
        this.sourceApkIcon = sourceApkIcon;
        notifyPropertyChanged(BR.sourceApkIcon);
    }

    @Bindable
    public boolean isCancelVisibility() {
        return cancelVisibility;
    }

    public void setCancelVisibility(boolean cancelVisibility) {
        this.cancelVisibility = cancelVisibility;
        notifyPropertyChanged(BR.cancelVisibility);
    }

    @Bindable
    public boolean isSilentlyVisibility() {
        return silentlyVisibility;
    }

    public void setSilentlyVisibility(boolean silentlyVisibility) {
        this.silentlyVisibility = silentlyVisibility;
        notifyPropertyChanged(BR.silentlyVisibility);
    }

    @Bindable
    public int getPermArrowDirect() {
        return permArrowDirect;
    }

    public void setPermArrowDirect(int permArrowDirect) {
        this.permArrowDirect = permArrowDirect;
        notifyPropertyChanged(BR.permArrowDirect);
    }

    @Bindable
    public int getActArrowDirect() {
        return actArrowDirect;
    }

    public void setActArrowDirect(int actArrowDirect) {
        this.actArrowDirect = actArrowDirect;
        notifyPropertyChanged(BR.actArrowDirect);
    }

    @Bindable
    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
        notifyPropertyChanged(BR.apkName);
    }

    @Bindable
    public String getApkVersion() {
        return apkVersion;
    }

    public void setApkVersion(String apkVersion) {
        this.apkVersion = apkVersion;
        notifyPropertyChanged(BR.apkVersion);
    }

    @Bindable
    public String getApkSource() {
        return apkSource;
    }

    public void setApkSource(String apkSource) {
        this.apkSource = apkSource;
        notifyPropertyChanged(BR.apkSource);
    }

    @Bindable
    public String getApkInstallMsg() {
        return apkInstallMsg;
    }

    public void setApkInstallMsg(String apkInstallMsg) {
        this.apkInstallMsg = apkInstallMsg;
        notifyPropertyChanged(BR.apkInstallMsg);
    }

    public void appendApkInstallMsg(String str) {
        apkInstallMsg = apkInstallMsg + str;
        notifyPropertyChanged(BR.apkInstallMsg);
    }

    @Bindable
    public String getApkVersionTips() {
        return apkVersionTips;
    }

    public void setApkVersionTips(String apkVersionTips) {
        this.apkVersionTips = apkVersionTips;
        notifyPropertyChanged(BR.apkVersionTips);
    }

    @Bindable
    public int getApkVersionTipsColor() {
        return apkVersionTipsColor;
    }

    public void setApkVersionTipsColor(int apkVersionTipsColor) {
        this.apkVersionTipsColor = apkVersionTipsColor;
        notifyPropertyChanged(BR.apkVersionTipsColor);
    }

    @Bindable
    public String getPermIndex() {
        return permIndex;
    }

    public void setPermIndex(String permIndex) {
        this.permIndex = permIndex;
        notifyPropertyChanged(BR.permIndex);
    }

    @Bindable
    public String getActIndex() {
        return actIndex;
    }

    public void setActIndex(String actIndex) {
        this.actIndex = actIndex;
        notifyPropertyChanged(BR.actIndex);
    }

    @Bindable
    public String getCancelStr() {
        return cancelStr;
    }

    public void setCancelStr(String cancelStr) {
        this.cancelStr = cancelStr;
        notifyPropertyChanged(BR.cancelStr);
    }

}
