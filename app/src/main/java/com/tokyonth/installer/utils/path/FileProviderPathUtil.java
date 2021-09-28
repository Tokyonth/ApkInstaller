package com.tokyonth.installer.utils.path;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileProviderPathUtil {

    public static File getFileFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        } else if (uri.getScheme() != null && uri.getPath() != null) {
            switch (uri.getScheme()) {
                case "content":
                    return getFileFromContentUri(context, uri);
                case "file":
                    return new File(uri.getPath());
            }
        }
        return null;
    }

    private static File getFileFromContentUri(Context context, Uri contentUri) {
        if (contentUri == null) {
            return null;
        }
        File file = null;
        String filePath = null;
        String fileName = null;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        try {
            Cursor cursor = contentResolver.query(contentUri, filePathColumn, null,
                    null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                try {
                    filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
                cursor.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (filePath != null && !TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            } else {
                filePath = getPathFromInputStreamUri(context, contentUri, fileName);
            }
            if (!TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            }
        }
        return file;
    }

    public static String getPathFromInputStreamUri(Context context, Uri uri, String fileName) {
        InputStream inputStream = null;
        String filePath = null;
        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                File file = createTemporalFileFrom(context, inputStream, fileName);
                filePath = file.getPath();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    private static File createTemporalFileFrom(Context context, InputStream inputStream, String fileName)
            throws IOException {
        File targetFile = null;
        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = new File(context.getExternalCacheDir(), fileName);
            if (targetFile.exists()) {
                boolean bool = targetFile.delete();
                Log.e("Del", bool + "");
            }
            OutputStream outputStream = new FileOutputStream(targetFile);
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return targetFile;
    }

}
