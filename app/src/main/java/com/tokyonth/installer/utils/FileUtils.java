package com.tokyonth.installer.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {

    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    assert files != null;
                    for (File value : files) {
                        deleteFolderFile(value.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        boolean bool = file.delete();
                        if (!bool)
                            Log.e("FileUtils", "failed to delete");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String byteToString(long size) {
        long GB = 1024 * 1024 * 1024;
        long MB = 1024 * 1024;
        long KB = 1024;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String resultSize;
        if (size / GB >= 1) {
            resultSize = decimalFormat.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            resultSize = decimalFormat.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            resultSize = decimalFormat.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + "B";
        }
        return resultSize;
    }

    public static long getFileOrFolderSize(File file) {
        try {
            if (file == null) return 0;
            if (!file.exists()) return 0;
            if (!file.isDirectory()) return file.length();
            else {
                long total = 0;
                File[] files = file.listFiles();
                if (files == null || files.length == 0) return 0;
                for (File f : files) {
                    total += getFileOrFolderSize(f);
                }
                return total;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getFileSize(String filepath) {
        return filepath != null ? getFileOrFolderSize(new File(filepath)) : 0;
    }

    public static long getFilesSize(String[] filepath) {
        if (filepath == null || filepath.length == 0) {
            return 0;
        } else {
            long total = 0;
            for (String s : filepath) {
                total += getFileSize(s);
            }
            return total;
        }
    }

}
