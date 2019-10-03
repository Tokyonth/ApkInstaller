package com.tokyonth.installer.utils;

import android.text.TextUtils;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {

    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static final String byteToString(long size) {

        long GB = 1024 * 1024 * 1024;//定义GB的计算常量
        long MB = 1024 * 1024;//定义MB的计算常量
        long KB = 1024;//定义KB的计算常量

        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize;
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + " B   ";
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

    public static long getFilesSize(String[] filepaths) {
        if (filepaths == null || filepaths.length == 0) {
            return 0;
        } else {
            long total = 0;
            for (int i = 0; i < filepaths.length; i++) {
                total += getFileSize(filepaths[i]);
            }
            return total;
        }
    }

}
