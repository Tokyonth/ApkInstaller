package com.tokyonth.installer

object Constants {

    private const val PKG_NAME = "com.tokyonth.installer"
    const val URI_DATA_TYPE = "application/vnd.android.package-archive"
    const val DEFAULT_SYS_PKG_NAME = "com.android.packageinstaller"
    const val MIUI_SYS_PKG_NAME = "com.miui.packageinstaller"
    const val PROVIDER_NAME = "${PKG_NAME}.ApkFileProvider"
    const val SE_LINUX_COMMAND = "setenforce permissive"
    const val INSTALL_COMMAND = "pm install -r -d --user 0 -i $PKG_NAME "

    const val ICEBOX_PKG_NAME = "com.catchingnow.icebox"
    const val SHIZUKU_PKG_NAME = "moe.shizuku.privileged.api"

    const val SP_FILE_NAME = "config"
    const val SP_SILENT_KEY = "defaultSilent"
    const val SP_INSTALL_MODE_KEY = "installMode"
    const val SP_CUSTOM_PKG_KEY = "systemPkgName"
    const val SP_IS_SYSTEM_PKG_KEY = "useSystemPkg"
    const val SP_AUTO_DELETE_KEY = "autoDelete"
    const val SP_SHOW_PERMISSION_KEY = "showPermission"
    const val SP_SHOW_ACTIVITY_KEY = "showActivity"
    const val SP_NIGHT_MODE_KEY = "nightMode"
    const val SP_FOLLOW_SYSTEM_NIGHT_KEY = "nightFollowSystem"
    const val SP_NEVER_VERSION_TIP_KEY = "neverVersionTips"
    const val SP_NEVER_SYSTEM_PKG_KEY = "neverUseSystemPkg"
    const val SP_IS_FIRST_BOOT_KEY = "isFirstBoot"

}
