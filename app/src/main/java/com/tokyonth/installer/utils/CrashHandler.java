package com.tokyonth.installer.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tokyonth.installer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private Context context;

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        saveCrashReport2SD(context, ex);
        showCrashToast();
    }

    public void initCrashHandler(Context context) {
        this.context = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void showCrashToast() {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                ToastUtil.showToast(context, context.getString(R.string.crash_tips), ToastUtil.DEFAULT_SITE);
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

        map.put("phoneBrand", "" + Build.BRAND);
        map.put("phoneModel", "" + Build.MODEL);
        map.put("sdkVersion", "" + Build.VERSION.SDK_INT);
        if (mPackageInfo != null) {
            map.put("versionName", mPackageInfo.versionName);
            map.put("versionCode", "" + mPackageInfo.versionCode);
        }
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
        sb.append("\n").append(context.getString(R.string.crash_feedback));
        sb.append("\n").append("\n").append(obtainExceptionInfo(ex));

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory().getPath());
            if (!dir.exists()) {
                boolean isCreate = dir.mkdirs();
                Log.e(TAG, "Create dir" + isCreate);
            }
            try {
                String fileName = dir.toString() + File.separator + context.getString(R.string.crash_file_name);
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}