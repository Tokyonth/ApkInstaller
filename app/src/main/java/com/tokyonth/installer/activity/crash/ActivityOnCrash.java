package com.tokyonth.installer.activity.crash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.tokyonth.installer.utils.PackageUtils;

public final class ActivityOnCrash {

    private final static String TAG = ActivityOnCrash.class.getSimpleName();
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";
    private static final String STACK_TRACE_STRING_INTENT = "stackTraceString";
    private static final int MAX_STACK_TRACE_SIZE = 131071; //128 KB - 1

    private static Application application;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void install(@Nullable final Context context) {
        try {
            if (context == null) {
                Log.e(TAG, "Install failed: context is null!");
            } else {
                final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
                if (oldHandler != null && !oldHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                    Log.e(TAG, "IMPORTANT WARNING! You already have an UncaughtExceptionHandler.");
                }
                application = (Application) context.getApplicationContext();
                Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                    Intent intent = new Intent(application, ErrorActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    String stackTraceString = sw.toString();
                    if (stackTraceString.length() > MAX_STACK_TRACE_SIZE) {
                        String disclaimer = " [stack trace too large]";
                        stackTraceString = stackTraceString.substring(0, MAX_STACK_TRACE_SIZE - disclaimer.length()) + disclaimer;
                    }
                    intent.putExtra(STACK_TRACE_STRING_INTENT, stackTraceString);
                    application.startActivity(intent);
                });
            }
        } catch (Throwable t) {
            Log.e(TAG, t.toString());
        }
    }

    @NonNull
    public static String getAllErrorDetailsFromIntent(@NonNull Context context, Intent intent) {
        String versionName = PackageUtils.INSTANCE.getVersionName(context);
        String errorDetails = "";
        errorDetails += "Build Version: " + versionName + " \n";
        errorDetails += "SDK Version: " + Build.VERSION.SDK_INT + "\n";
        errorDetails += "Device: " + getDeviceModelName() + " \n \n";
        errorDetails += "Stack trace:  \n";
        errorDetails += getStackTraceMsg(intent);
        return errorDetails;
    }

    public static void closeApplication(@NonNull Activity activity) {
        activity.finish();
        killCurrentProcess();
    }

    private static String getStackTraceMsg(Intent intent) {
        return intent.getStringExtra(STACK_TRACE_STRING_INTENT);
    }

    @NonNull
    private static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    @NonNull
    private static String capitalize(@Nullable String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
