package com.tokyonth.installer.helper;

public class VerHelper {

    public static int NEW_VER = 1;
    public static int LOW_VER = 2;
    public static int EQUAL_VER = 3;

    public static int CheckVer(int ver, int install_ver) {

        if (ver == install_ver) {
            return EQUAL_VER;
        } else if (ver > install_ver) {
            return NEW_VER;
        } else if (ver < install_ver) {
            return LOW_VER;
        }

        return 0;
    }

}
