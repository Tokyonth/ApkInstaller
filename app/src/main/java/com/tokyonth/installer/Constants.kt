package com.tokyonth.installer

import java.io.File

object Constants {

    private var PKG_NAME = "com.tokyonth.installer"
    var SP_FILE_NAME = "config"
    var SYS_PKG_NAME = "com.android.packageinstaller"
    var SP_AUTO_DELETE = "auto_delete"
    var SP_NIGHT_MODE = "night_mode"
    var SP_SHOW_PERMISSION = "show_permission"
    var SP_SHOW_ACTIVITY = "show_activity"
    var SP_INSTALLED_VIBRATE = "installed_vibrate"
    var SP_USE_SYS_PKG = "use_system_pkg"
    var SP_NEVER_TIP_VERSION = "never_show_version_tips"
    var URI_DATA_TYPE = "application/vnd.android.package-archive"
    var PROVIDER_STR = "$PKG_NAME.FileProvider"
    var DATA_PATH_PREFIX = "/data/data"
    var SE_LINUX_COMMAND = "setenforce permissive"
    var INSTALL_COMMAND = "pm install -r -d --user 0 -i $PKG_NAME "
    var UNINSTALL_COMMAND = "pm uninstall "
    var FREEZE_COMMAND = "pm disable "
    var UNFREEZE_COMMAND = "pm enable "
    var CACHE_APK_DIR = "/storage/emulated/0/Android/data/$PKG_NAME/cache/"
    val SHELL_SCRIPT_CACHE_FILE = File(CACHE_APK_DIR, "run.sh")
    var PERMISSION_REQUEST_CODE = 100
    var SP_USE_SHIZUKU_MODE = "use_shizuku"
    var SP_NEVER_SHOW_USE_SYSTEM_PKG = "never_show_use_system_pkg"
    var SP_NIGHT_FOLLOW_SYSTEM = "night_follow_system"
    var APK_SOURCE = "apkSource"
    var SP_USE_ICEBOX_MODE = "use_icebox"
    val SP_INSTALL_MODE_KEY = "install_mode"

    enum class INSTALL_MODE {
        SHELL, SHIZUKU, ICEBOX
    }

}
