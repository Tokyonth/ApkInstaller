package com.tokyonth.installer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private Context mContext;

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        saveCrashReport2SD(mContext, ex);
        showCrashToast();
    }

    public void initCrashHandler(Context context) {
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void showCrashToast() {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                ToastUtil.showToast(mContext, "程序出现异常,请查看SDCard下的崩溃日志!", ToastUtil.DEFAULT_SITE);
                Looper.loop();
            }
        }.start();
        try {
            Thread.sleep(2000);
            killProcess(myPid());
            System.exit(0);
            System.gc();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, String> obtainSimpleInfo(Context context) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        PackageManager mPackageManager = context.getPackageManager();
        PackageInfo mPackageInfo = null;
        try {
            mPackageInfo = mPackageManager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        map.put("品牌", "" + Build.BRAND);
        map.put("型号", "" + Build.MODEL);
        map.put("SDK版本", "" + Build.VERSION.SDK_INT);
        assert mPackageInfo != null;
        map.put("versionName", mPackageInfo.versionName);
        map.put("versionCode", "" + mPackageInfo.versionCode);
        map.put("crashTime", parserTime(System.currentTimeMillis()));
        return map;
    }

    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter mStringWriter = new StringWriter();
        PrintWriter mPrintWriter = new PrintWriter(mStringWriter);
        throwable.printStackTrace(mPrintWriter);
        mPrintWriter.close();

        Log.e(TAG, mStringWriter.toString());
        return mStringWriter.toString();
    }

    private void saveCrashReport2SD(Context context, Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : obtainSimpleInfo(context).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(" = ").append(value).append("\n");
        }
        sb.append(obtainExceptionInfo(ex));
        sb.append("\n请将日志发送到邮箱1948226838@qq.com 或者在 酷安 进行反馈");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory().getPath());
            if (!dir.exists()) {
                boolean isCreate = dir.mkdirs();
                Log.e(TAG, "Create dir" + isCreate);
            }
            try {
                String fileName = dir.toString() + File.separator + "安装器崩溃日志.log";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String parserTime(long milliseconds) {
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return format.format(new Date(milliseconds));
    }

}