package com.tokyonth.installer

import java.io.File

object Constants {

    private var PKG_NAME = "com.tokyonth.installer"
    var SP_FILE_NAME = "config"
    var SYS_PKG_NAME = "com.android.packageinstaller"
    var SP_AUTO_DEL = "auto_delete"
    var SP_NIGHT_MODE = "night_mode"
    var SP_SHOW_PERM = "show_perm"
    var SP_SHOW_ACT = "show_act"
    var SP_VIBRATE = "vibrate"
    var SP_USE_SYS_PKG = "use_sys_pkg"
    var URI_DATA_TYPE = "application/vnd.android.package-archive"
    var PROVIDER_STR = "$PKG_NAME.FileProvider"
    var DATA_PATH_PREFIX = "/data/data"
    var SE_LINUX_COMMAND = "setenforce permissive"
    var INSTALL_COMMAND = "pm install -r --user 0 "
    var UNINSTALL_COMMAND = "pm uninstall -k --user 0 "
    var FREEZE_COMMAND = "pm disable "
    var UNFREEZE_COMMAND = "pm enable "
    var SP_NOT_TIP_VERSION = "not_show_version_tips"
    var CACHE_APK_DIR = "/storage/emulated/0/Android/data/$PKG_NAME/cache/"
    var MT2_PKG_NAME = "bin.mt.plus"
    internal val SHELL_SCRIPT_CACHE_FILE = File(CACHE_APK_DIR, "run.sh")
    var PERMISSION_REQUEST_CODE = 100
    var SP_IS_SHIZUKU_MODE = "use_shizuku"

}
