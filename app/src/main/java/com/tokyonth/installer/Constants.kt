package com.tokyonth.installer

import android.graphics.Bitmap

object Constants {

    private const val PKG_NAME = "com.tokyonth.installer"
    const val SP_FILE_NAME = "config"
    const val APK_SOURCE = "apkSource"
    const val APK_INFO = "apkInfo"
    const val IS_FORM_INSTALL_ACT = "isFormInstallAct"
    const val URI_DATA_TYPE = "application/vnd.android.package-archive"
    const val DEFAULT_SYS_PKG_NAME = "com.android.packageinstaller"
    const val PROVIDER_STR = "$PKG_NAME.FileProvider"
    const val SE_LINUX_COMMAND = "setenforce permissive"
    const val INSTALL_COMMAND = "pm install -r -d --user 0 -i $PKG_NAME "
    const val UNINSTALL_COMMAND = "pm uninstall "
    const val ANDROID_DATA_STR = "Android/data"

    const val ICEBOX_PKG_NAME = "com.catchingnow.icebox"
    const val SHIZUKU_PKG_NAME = "moe.shizuku.privileged.api"

    const val PERM_CODE = 955
    const val API_R_CODE = 11

    var APP_ICON_BITMAP: Bitmap? = null

}
