package com.tokyonth.installer.bean;

public class SettingsBean {

    private String Title;
    private String Sub;
    private int color;
    private int Icon;

    public SettingsBean(String Title, String Sub, int Icon, int color) {
        this.Title = Title;
        this.Sub = Sub;
        this.Icon = Icon;
        this.color = color;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getSub() {
        return Sub;
    }

    public void setSub(String sub) {
        Sub = sub;
    }

    public int getIcon() {
        return Icon;
    }

    public void setIcon(int icon) {
        Icon = icon;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

}
