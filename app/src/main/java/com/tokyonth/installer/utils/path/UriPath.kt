package com.tokyonth.installer.utils.path

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.File

object UriPath {

    private var uri: Uri? = null

    private var shardUid: String? = null

    private var uriPath: String? = null

    private var authority: String? = null

    private var SDCARD: String? = null

    private var getExternalStorageDirectory: String? = null

    private var getExternalRootDir: String? = null

    private var getExternalFilesDir: String? = null

    private var getExternalCacheDir: String? = null

    private var getStorageIsolationDir: String? = null

    private var getFirstPathSegment: String? = null

    private var getPathFromIndex1PathSegment: String? = null

    private var getLastPathSegment: String? = null

    /**
     * 获取pkgName的SharedUserId,兼容"存储空间隔离"新特性
     */
    private fun getSharedUserId(context: Context, pkgName: String?): String? {
        val pm = context.packageManager
        var applicationInfo: ApplicationInfo? = null
        var sharedUserId: String? = null
        try {
            applicationInfo = pm.getApplicationInfo(pkgName!!, 0)
        } catch (ignore: Exception) {
            // ignore
        }
        if (applicationInfo != null) {
            val pkgInfo =
                pm.getPackageArchiveInfo(applicationInfo.sourceDir, PackageManager.GET_ACTIVITIES)
            if (pkgInfo != null) {
                sharedUserId = pkgInfo.sharedUserId
            }
        }
        return sharedUserId
    }

    //显示Uri的一些信息
    private fun showDetail(uri: Uri) {
        Log.e("--Uri--", uri.toString() + "")
        Log.e("--getPath--", "[" + uri.path + "]")
        Log.e("--getLastPathSegment--", "[" + uri.lastPathSegment + "]")
        Log.e("--getQuery--", "[" + uri.query + "]")
        Log.e("--getScheme--", "[" + uri.scheme + "]")
        Log.e("--getEncodedPath--", "[" + uri.encodedPath + "]")
        Log.e("--getAuthority--", "[" + uri.authority + "]")
        Log.e("--getEncodedAuthority--", "[" + uri.encodedAuthority + "]")
        Log.e("--getEncodedFragment--", "[" + uri.encodedFragment + "]")
        Log.e("--getUserInfo--", uri.userInfo + "")
        Log.e("--getHost--", uri.host + "")
        Log.e("--getPathSegments--", uri.pathSegments.toString() + "")
        Log.e("--getSchemeSpecificPart", uri.schemeSpecificPart + "")
        Log.e("--getPort--", uri.port.toString() + "")
        Log.e("-getQueryParameterNames", uri.queryParameterNames.toString() + "")
        Log.e("--isAbsolute--", uri.isAbsolute.toString() + "")
        Log.e("--isHierarchical--", uri.isHierarchical.toString() + "")
        Log.e("--isOpaque--", uri.isOpaque.toString() + "")
        Log.e("--isRelative--", uri.isRelative.toString() + "")
    }

    //主要根据getAuthority和 getDataColumn获取file
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        var path: String? = null
        val getScheme = uri.scheme
        val getAuthority = uri.authority + ""
        Log.e("query_authority", "" + uri.authority)

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = split[0]
            Log.e("docId", docId)
            when (getAuthority) {
                "com.android.providers.downloads.documents" -> if (split.size > 1) {
                    if ("raw".equals(type, ignoreCase = true)) {
                        path = split[1]
                    }
                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        //不支持Android10 及以上
                        // 需要android.permission.ACCESS_ALL_DOWNLOADS
                        val uriString = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads"
                        )
                        for (s in uriString) {
                            path = getDataColumn(
                                context,
                                ContentUris.withAppendedId(Uri.parse(s), docId.toLong()),
                                null,
                                null
                            )
                            if (path != null) {
                                break
                            }
                        }
                    }
                }
                "com.android.externalstorage.documents" -> if (split.size > 1) {
                    if ("primary".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else if ("home".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory()
                            .toString() + "/Documents/" + split[1]
                    }
                }
                "com.android.providers.media.documents" -> {
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    path = getDataColumn(context, contentUri, selection, selectionArgs)
                }
                else -> {}
            }
        } else if (ContentResolver.SCHEME_FILE == getScheme) {
            path = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == getScheme) {
            // Return the remote address
            path = if ("com.google.android.apps.photos.content" == getAuthority) {
                uri.lastPathSegment
            } else {
                getDataColumn(context, uri, null, null)
            }
        }
        return if (checkFileOrPath(path)) {
            File(path!!)
        } else null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        Log.e("query_uri", uri.toString() + "")
        val column = MediaStore.MediaColumns.DATA
        val projection = arrayOf(
            column
        )
        try {
            context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    DatabaseUtils.dumpCursor(cursor)
                    val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //一些变量的初始化
    @SuppressLint("SdCardPath")
    private fun initValue(context: Context, referrer: String, fromUri: Uri) {
        //        showDetail(uri);
        uri = fromUri
        shardUid = getSharedUserId(context, referrer)
        try {
            uriPath = uri!!.path
            authority = uri!!.authority
            //"/storage/emulated/0"
            getExternalStorageDirectory = Environment.getExternalStorageDirectory().path
            SDCARD = "/sdcard"
            getExternalRootDir = "/Android/data/$referrer"
            getExternalFilesDir = "/Android/data/$referrer/files"
            getExternalCacheDir = "/Android/data/$referrer/cache"
            getStorageIsolationDir = "/Android/data/$referrer/sdcard"

            //获取索引0路径段
            getFirstPathSegment = uri!!.pathSegments[0]
            //最后一个路径段
            getLastPathSegment = uri!!.lastPathSegment

            //删索引0路径段和其前面的"/"
            getPathFromIndex1PathSegment = uriPath!!.substring(getFirstPathSegment!!.length + 1)
        } catch (e: Exception) {
            authority = null
        }
    }

    //方法 1
    private val someFile: File?
        get() = if (authority == null) {
            null
        } else {
            Log.e("getSomeFile_FROM_URI", uri.toString() + "")
            var file: File? = null
            try {
                file = someFileFromAuthorityAndUri
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (file == null) {
                file = someFileFromReferrerAndUri
            }
            file
        }//content://com.coolapk.market.vn.fileProvider/external_storage_root/6/file.apk

    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"

    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"
//content://com.coolapk.market.vn.fileProvider/files_root/file.apk
    //content://com.coolapk.market.vn.fileProvider/files_root/files/Download/file.apk

    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer"
//content://com.coolapk.market.fileprovider/external_storage_root/6/file.apk

    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"


    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"


    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"+"/GDTDOWNLOAD"
//content://com.coolapk.market.fileprovider/gdt_sdk_download_path/file.apk

    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/GDTDOWNLOAD"
//content://com.coolapk.market.fileprovider/external_files_path/file.apk
    //content://com.coolapk.market.fileprovider/external_files_path/files/Download/file.apk

    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/files"+"/Download"
//content://com.coolapk.market.fileprovider/files_root/file.apk
    //content://com.coolapk.market.fileprovider/files_root/files/Download/file.apk

    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer"

    //方法 1.1 根据authority的自定义规则获取file
    @get:SuppressLint("SdCardPath")
    private val someFileFromAuthorityAndUri: File?
        get() {
            var path: String? = ""
            val path0: String?
            val pathList = ArrayList<String?>()
            when (authority) {
                "moe.shizuku.redirectstorage.ServerFileProvider" -> if (uri!!.pathSegments.size > 2) {
                    val getIndex1PathSegment = uri!!.pathSegments[1]
                    val getPathfromIndex2PathSegment = uriPath!!.substring(
                        getFirstPathSegment!!.length + 1 + getIndex1PathSegment.length + 1
                    )
                    if (SDCARD.equals("/$getIndex1PathSegment", ignoreCase = true)) {
                        path = getPathFromIndex1PathSegment
                        pathList.add(path)
                    }
                    path =
                        "$getExternalStorageDirectory/Android/data/$getIndex1PathSegment/sdcard$getPathfromIndex2PathSegment"
                    pathList.add(path)
                }
                "com.taptap.fileprovider" -> {
                    if ("downloads_external" == getFirstPathSegment) {
                        path =
                            "$getExternalStorageDirectory$getExternalFilesDir/Download$getPathFromIndex1PathSegment"
                    }
                    when (getFirstPathSegment) {
                        "files_root" ->                         //content://com.coolapk.market.fileprovider/files_root/file.apk
                            //content://com.coolapk.market.fileprovider/files_root/files/Download/file.apk

                            //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer"
                            path =
                                getExternalStorageDirectory + getExternalRootDir + getPathFromIndex1PathSegment
                        "external_files_path" ->                         //content://com.coolapk.market.fileprovider/external_files_path/file.apk
                            //content://com.coolapk.market.fileprovider/external_files_path/files/Download/file.apk

                            //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/files"+"/Download"
                            path =
                                "$getExternalStorageDirectory$getExternalFilesDir/Download$getPathFromIndex1PathSegment"
                        "gdt_sdk_download_path" ->                         //content://com.coolapk.market.fileprovider/gdt_sdk_download_path/file.apk

                            //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/GDTDOWNLOAD"
                            path =
                                "$getExternalStorageDirectory/GDTDOWNLOAD$getPathFromIndex1PathSegment"
                        "external_storage_root" ->                         //content://com.coolapk.market.fileprovider/external_storage_root/6/file.apk

                            //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"
                            path = getExternalStorageDirectory + getPathFromIndex1PathSegment
                        else -> {}
                    }
                    path0 = path


                    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"
                    path =
                        getExternalStorageDirectory + getStorageIsolationDir + getPathFromIndex1PathSegment
                    pathList.add(path)


                    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"+"/GDTDOWNLOAD"
                    path =
                        "$getExternalStorageDirectory$getStorageIsolationDir/GDTDOWNLOAD$getPathFromIndex1PathSegment"
                    pathList.add(path)
                    pathList.add(path0)
                }
                "com.coolapk.market.fileprovider" -> {
                    when (getFirstPathSegment) {
                        "files_root" -> path =
                            getExternalStorageDirectory + getExternalRootDir + getPathFromIndex1PathSegment
                        "external_files_path" -> path =
                            "$getExternalStorageDirectory$getExternalFilesDir/Download$getPathFromIndex1PathSegment"
                        "gdt_sdk_download_path" -> path =
                            "$getExternalStorageDirectory/GDTDOWNLOAD$getPathFromIndex1PathSegment"
                        "external_storage_root" -> path =
                            getExternalStorageDirectory + getPathFromIndex1PathSegment
                        else -> {}
                    }
                    path0 = path
                    path =
                        getExternalStorageDirectory + getStorageIsolationDir + getPathFromIndex1PathSegment
                    pathList.add(path)
                    path =
                        "$getExternalStorageDirectory$getStorageIsolationDir/GDTDOWNLOAD$getPathFromIndex1PathSegment"
                    pathList.add(path)
                    pathList.add(path0)
                }
                "com.coolapk.market.vn.fileProvider" -> {
                    when (getFirstPathSegment) {
                        "files_root" ->                         //content://com.coolapk.market.vn.fileProvider/files_root/file.apk
                            //content://com.coolapk.market.vn.fileProvider/files_root/files/Download/file.apk

                            //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer"
                            path =
                                getExternalStorageDirectory + getExternalRootDir + getPathFromIndex1PathSegment
                        "external_storage_root" ->                         //content://com.coolapk.market.vn.fileProvider/external_storage_root/6/file.apk

                            //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"
                            path = getExternalStorageDirectory + getPathFromIndex1PathSegment
                        else -> {}
                    }
                    path0 = path
                    pathList.add(path)

                    //删索引0路径段和其前面的"/",前面加"/storage/emulated/0"+"/Android/data/com.referrer/sdcard"
                    path =
                        getExternalStorageDirectory + getStorageIsolationDir + getPathFromIndex1PathSegment
                    pathList.add(path)
                    pathList.add(path0)
                }
                "com.tencent.mm.external.fileprovider", "com.tencent.mobileqq.fileprovider", "com.mi.android.globalFileexplorer.myprovider", "in.mfile.files", "com.estrongs.files", "com.ktls.fileinfo.provider", "pl.solidexplorer2.files", "cn.ljt.p7zip.fileprovider" -> {
                    if ("downloads" == getFirstPathSegment) {
                        if (uri!!.pathSegments.size > 1) {
                            path =
                                "$getExternalStorageDirectory/Download$getPathFromIndex1PathSegment"
                            path0 = path
                            path =
                                "$getExternalStorageDirectory$getStorageIsolationDir/Download$getPathFromIndex1PathSegment"
                            pathList.add(path)
                            pathList.add(path0)
                            return pickValidFileFromPathList(pathList)
                        }
                    }
                    return null
                }
                else -> {
                    if ("downloads" == getFirstPathSegment) {
                        if (uri!!.pathSegments.size > 1) {
                            path =
                                "$getExternalStorageDirectory/Download$getPathFromIndex1PathSegment"
                            path0 = path
                            path =
                                "$getExternalStorageDirectory$getStorageIsolationDir/Download$getPathFromIndex1PathSegment"
                            pathList.add(path)
                            pathList.add(path0)
                            return pickValidFileFromPathList(pathList)
                        }
                    }
                    return null
                }
            }
            return pickValidFileFromPathList(pathList)
        }

    //在pathList挑选有效的file
    private fun pickValidFileFromPathList(pathList: ArrayList<String?>?): File? {
        return if (pathList == null) {
            null
        } else {
            for (getPath: String? in pathList) {
                if (checkFileOrPath(getPath)) {
                    return getPath?.let { File(it) }
                } else {
                    Log.e("fakePath", getPath + "")
                }
            }
            null
        }
    }//content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/file.apk//content://com.referrer.fileprovider/file.apk//content://in.mfile.files/storage/emulated/0/file.apk//content://in.mfile.files/storage/emulated/0/file.apk//content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Tencent/QQfile_recv/file.apk//content://in.mfile.files/storage/emulated/0/file.apk

    //方法 1.2 的一个具体操作获取PathList
    private val pathListAboutExternalStoragePublicDirectory: ArrayList<String?>?
        get() {
            return if (uriPath == null) {
                null
            } else {
                var pathList = ArrayList<String?>()
                val size = uri!!.pathSegments.size
                if (uriPath!!.startsWith(getExternalStorageDirectory!!)) {
                    if (size > 3) {
                        //content://in.mfile.files/storage/emulated/0/file.apk
                        pathList = getPathListStartWith(
                            getExternalStorageDirectory,
                            uriPath!!
                        )
                    }
                } else if (getPathFromIndex1PathSegment!!.startsWith(
                        getExternalStorageDirectory!!
                    )
                ) {
                    if (size > 4) {
                        //content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Tencent/QQfile_recv/file.apk
                        pathList = getPathListStartWith(
                            getExternalStorageDirectory,
                            getPathFromIndex1PathSegment!!
                        )
                    }
                } else if (SDCARD.equals("/$getFirstPathSegment", ignoreCase = true)) {
                    if (size > 1) {
                        //content://in.mfile.files/storage/emulated/0/file.apk
                        pathList = getPathListStartWith(
                            SDCARD,
                            uriPath!!
                        )
                    }
                } else if (getPathFromIndex1PathSegment!!.startsWith(
                        SDCARD!!
                    )
                ) {
                    if (size > 2) {
                        //content://in.mfile.files/storage/emulated/0/file.apk
                        pathList = getPathListStartWith(
                            SDCARD,
                            getPathFromIndex1PathSegment!!
                        )
                    }
                } else {
                    val path0 = getExternalStorageDirectory + uriPath
                    val path1 = getExternalStorageDirectory + getPathFromIndex1PathSegment
                    if (size > 0) {
                        //content://com.referrer.fileprovider/file.apk
                        pathList = getPathListStartWith(getExternalStorageDirectory, path0)
                    }
                    if (size > 1) {
                        //content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/file.apk
                        pathList.addAll(getPathListStartWith(getExternalStorageDirectory, path1))
                    }
                }
                pathList
            }
        }

    @SuppressLint("SdCardPath")
    private fun getPathListStartWith(startWith: String?, path: String): ArrayList<String?> {
        val pathList = ArrayList<String?>()
        val path0: String
        return if (path.startsWith(startWith!!)) {
            //content://in.mfile.files/storage/emulated/0/file.apk
            path0 = path
            pathList.add(getPathWithIsolation(startWith, getStorageIsolationDir, path))
            if (shardUid != null) {
                Log.e("shardUid", "" + shardUid)
                pathList.add(
                    getPathWithIsolation(
                        startWith,
                        "/Android/data/shared-$shardUid/sdcard",
                        path
                    )
                )
            }
            pathList.add(getPathWithIsolation(startWith, getExternalFilesDir, path))
            pathList.add(path0)
            pathList
        } else {
            pathList
        }
    }

    //兼容"存储空间隔离"
    private fun getPathWithIsolation(startWith: String?, prefix: String?, path: String): String {
        val stringBuilder = StringBuilder(path)
            .insert(startWith!!.length, prefix)
        return stringBuilder.toString()
    }

    //方法 1.2 根据对ExternalStoragePublicDirectory的相关操作获取file
    private val someFileFromReferrerAndUri: File?
        get() {
            val pathList =
                pathListAboutExternalStoragePublicDirectory
            return pickValidFileFromPathList(pathList)
        }

    private fun checkFileOrPath(file: File?): Boolean {
        return file != null && file.exists() && !file.isDirectory
    }

    private fun checkFileOrPath(path: String?): Boolean {
        return if (path == null) {
            false
        } else {
            checkFileOrPath(File(path))
        }
    }

    //两种方法获取file
    fun getFile(context: Context, referrer: String, fromUri: Uri): File? {
        var file: File? = null
        try {
            initValue(context, referrer, fromUri)
            file = someFile

            Log.e("getSomeFile", file.toString() + "")
            if (file == null) {
                file = getFileFromUri(context, fromUri)
                Log.e("getFileFromUri", file.toString() + "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("getFileFromUri", e.stackTraceToString())
        }
        return file
    }

}
