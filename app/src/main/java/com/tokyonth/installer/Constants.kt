package com.tokyonth.installer

object Constants {

    private const val PKG_NAME = "com.tokyonth.installer"
    const val SP_INSTALL_MODE = "installMode"
    const val SP_FILE_NAME = "config"
    const val SYS_PKG_NAME = "com.android.packageinstaller"
    const val SP_AUTO_DELETE = "autoDelete"
    const val SP_NIGHT_MODE = "nightMode"
    const val SP_SHOW_PERMISSION = "showPermission"
    const val SP_SHOW_ACTIVITY = "showActivity"
    const val SP_INSTALLED_VIBRATE = "installedVibrate"
    const val SP_USE_SYS_PKG = "useSystemPkg"
    const val SP_NEVER_TIP_VERSION = "neverShowVersionTips"
    const val URI_DATA_TYPE = "application/vnd.android.package-archive"
    const val PROVIDER_STR = "$PKG_NAME.FileProvider"
    const val SE_LINUX_COMMAND = "setenforce permissive"
    const val INSTALL_COMMAND = "pm install -r -d --user 0 -i $PKG_NAME "
    const val UNINSTALL_COMMAND = "pm uninstall "

    //var FREEZE_COMMAND = "pm disable "
    const val UNFREEZE_COMMAND = "pm enable "
    const val SP_NEVER_SHOW_USE_SYSTEM_PKG = "neverShowUseSystemPkg"
    const val SP_NIGHT_FOLLOW_SYSTEM = "nightFollowSystem"
    const val APK_SOURCE = "apkSource"

}
