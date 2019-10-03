package com.tokyonth.installer.bean;

public class InfoBean {

    private String perm;
    private String des;

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

    public InfoBean(String perm, String des) {
        this.perm = perm;
        this.des = des;
    }

}
