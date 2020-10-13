package com.tokyonth.installer.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ProviderUtils {

    public static String getFPUriToPath(Context context, Uri uri) {
        try {
            List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
            if (packs != null) {
                String fileProviderClassName = FileProvider.class.getName();
                for (PackageInfo pack : packs) {
                    ProviderInfo[] providers = pack.providers;
                    if (providers != null) {
                        for (ProviderInfo provider : providers) {
                            // if (uri.getAuthority().equals(provider.authority)) {
                            //     if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
                            Class<FileProvider> fileProviderClass = FileProvider.class;
                            try {
                                Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
                                getPathStrategy.setAccessible(true);
                                Object invoke = getPathStrategy.invoke(null, context, uri.getAuthority());
                                if (invoke != null) {
                                    String PathStrategyStringClass = FileProvider.class.getName() + "$PathStrategy";
                                    Class<?> PathStrategy = Class.forName(PathStrategyStringClass);
                                    Method getFileForUri = PathStrategy.getDeclaredMethod("getFileForUri", Uri.class);
                                    getFileForUri.setAccessible(true);
                                    Object invoke1 = getFileForUri.invoke(invoke, uri);
                                    if (invoke1 instanceof File) {
                                        String filePath = ((File) invoke1).getAbsolutePath();
                                        return filePath;
                                    }
                                }
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;
                            //     }
                            //     break;
                            // }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将uri转换成真实路径 * * @param selectedVideoUri * @param contentResolver * @return
     */
    public static String getFilePathFromContentUri(Activity context, Uri selectedVideoUri) {
        String filePath = "";
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(selectedVideoUri, filePathColumn, null, null, null);
        // 也可用下面的方法拿到cursor //
      //  Cursor cursor = context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getColumnIndex(filePathColumn[0]);
                if (id > -1) filePath = cursor.getString(id);
            }
            cursor.close();
        }
        return filePath;
    }

    private static Uri getFileContentUri(Context context, File file) {
        String volumeName = "external";
        String filePath = file.getAbsolutePath();
        String[] projection = new String[]{MediaStore.Files.FileColumns._ID};
        Uri uri = null;
        Cursor cursor = context.getContentResolver().query(MediaStore.Files.getContentUri(volumeName), projection, MediaStore.Images.Media.DATA + "=? ", new String[]{filePath}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                uri = MediaStore.Files.getContentUri(volumeName, id);
            }
            cursor.close();
        }
        return uri;
    }

    private static Uri forceGetFileUri(File shareFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                @SuppressLint("PrivateApi") Method rMethod = StrictMode.class.getDeclaredMethod("disableDeathOnFileUriExposure");
                rMethod.invoke(null);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return Uri.parse("file://" + shareFile.getAbsolutePath());
    }

}

