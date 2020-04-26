package com.tokyonth.installer.bean.permissions;

public class PermFullBean {

    private String perm;
    private String des;
    private String lab;
    private String group;

    public PermFullBean(String perm, String group, String des, String lab) {
        this.perm = perm;
        this.group = group;
        this.des = des;
        this.lab = lab;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPerm() {
        return perm;
    }

    public void setPerm(String perm) {
        this.perm = perm;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getLab() {
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

}
