package com.tokyonth.installer;

public class Contents {

    private static String PKG_NAME = "com.tokyonth.installer";
    public static String SP_FILE_NAME = "conf";
    public static String SYS_PKG_NAME = "com.android.packageinstaller";
    public static String SP_AUTO_DEL = "auto_delete";
    public static String SP_NIGHT_MODE = "night_mode";
    public static String SP_SHOW_PERM = "show_perm";
    public static String SP_SHOW_ACT = "show_act";
    public static String SP_VIBRATE = "vibrate";
    public static String SP_USE_SYS_PKG = "use_sys_pkg";
    public static String URI_DATA_TYPE = "application/vnd.android.package-archive";
    public static String PROVIDER_STR = PKG_NAME + ".FileProvider";
    public static String DATA_PATH_PREFIX = "/data/data";
    public static String SE_LINUX_COMMAND = "setenforce permissive";
    public static String INSTALL_COMMAND = "pm install -r --user 0 ";
    public static String UNINSTALL_COMMAND = "pm uninstall -k --user 0 ";
    public static String SP_NO_TIP_VERSION = "not_show_version_tips";
    public static String CACHE_APK_DIR = "/storage/emulated/0/Android/data/com.tokyonth.installer/cache/";
    public static String MT2_PKG_NAME = "bin.mt.plus";

}
