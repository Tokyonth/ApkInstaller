package com.tokyonth.installer.apk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AndroidRuntimeException;

import com.tokyonth.installer.utils.ShellUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class APKCommander {

    private Context context;
    private Uri uri;
    private ApkInfo mApkInfo;
    private ICommanderCallback callback;
    private Handler handler;

    public APKCommander(Context context, Uri uri, ICommanderCallback commanderCallback) {
        this.context = context;
        this.uri = uri;
        this.callback = commanderCallback;
        handler = new Handler(Looper.getMainLooper());
        new ParseApkTask().start();
    }

    public void startInstall() {
        new InstallApkTask().start();
    }

    public ApkInfo getApkInfo() {
        return mApkInfo;
    }

    private class InstallApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onApkPreInstall(mApkInfo);
                }
            });
            final int retCode = ShellUtils.execWithRoot("pm install -r --user 0 \"" + mApkInfo.getApkFile().getPath() + "\"" + "\n", new ShellUtils.Result() {
                @Override
                public void onStdout(final String text) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onInstallLog(mApkInfo, text);
                        }
                    });
                }

                @Override
                public void onStderr(final String text) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onInstallLog(mApkInfo, text);
                        }
                    });
                }

                @Override
                public void onCommand(String command) {

                }

                @Override
                public void onFinish(int resultCode) {

                }
            });
            if (retCode == 0 && mApkInfo.isFakePath())
                mApkInfo.getApkFile().delete();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onApkInstalled(mApkInfo, retCode);
                }
            });
        }
    }

    private class ParseApkTask extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStartParseApk(uri);
                    }
                });
                mApkInfo = new ApkInfo();
                String apkSourcePath = ContentUriUtils.getPath(context, uri);
                if (apkSourcePath == null) {
                    mApkInfo.setFakePath(true);
                    File tempFile = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".apk");
                    try {
                        InputStream is = context.getContentResolver().openInputStream(uri);
                        if (is != null) {
                            OutputStream fos = new FileOutputStream(tempFile);
                            byte[] buf = new byte[4096 * 1024];
                            int ret;
                            while ((ret = is.read(buf)) != -1) {
                                fos.write(buf, 0, ret);
                                fos.flush();
                            }
                            fos.close();
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mApkInfo.setApkFile(tempFile);
                } else {
                    mApkInfo.setApkFile(new File(apkSourcePath));
                }
                //读取apk的信息
                PackageManager pm = context.getPackageManager();
                PackageInfo pkgInfo = pm.getPackageArchiveInfo(mApkInfo.getApkFile().getPath(), PackageManager.GET_PERMISSIONS);
                if (pkgInfo != null) {
                    pkgInfo.applicationInfo.sourceDir = mApkInfo.getApkFile().getPath();
                    pkgInfo.applicationInfo.publicSourceDir = mApkInfo.getApkFile().getPath();
                    mApkInfo.setAppName(pm.getApplicationLabel(pkgInfo.applicationInfo).toString());
                    mApkInfo.setPackageName(pkgInfo.applicationInfo.packageName);
                    mApkInfo.setVersionName(pkgInfo.versionName);
                    mApkInfo.setVersionCode(pkgInfo.versionCode);
                    mApkInfo.setIcon(pkgInfo.applicationInfo.loadIcon(pm));
                    try {
                        PackageInfo installedPkgInfo = pm.getPackageInfo(mApkInfo.getPackageName(), 0);
                        mApkInfo.setInstalledVersionName(installedPkgInfo.versionName);
                        mApkInfo.setInstalledVersionCode(installedPkgInfo.versionCode);
                        mApkInfo.setHasInstalledApp(true);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        mApkInfo.setHasInstalledApp(false);
                    }
                    mApkInfo.setPermissions(pkgInfo.requestedPermissions);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onApkParsed(mApkInfo);
                    }
                });
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onApkParsed(null);
                    }
                });
                e.printStackTrace();
                throw new AndroidRuntimeException(e);

            }
        }
    }
}
