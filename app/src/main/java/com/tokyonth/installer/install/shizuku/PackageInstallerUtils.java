package com.tokyonth.installer.install.shizuku;

import android.content.Context;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;

import com.tokyonth.installer.App;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings({"JavaReflectionMemberAccess", "ConstantConditions"})
public class PackageInstallerUtils {

    public static PackageInstaller createPackageInstaller(IPackageInstaller installer, String installerPackageName, int userId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (Build.VERSION.SDK_INT >= 26) {
            if (Build.VERSION.SDK_INT >= 31) {
                return PackageInstaller.class.getConstructor(IPackageInstaller.class, String.class, String.class, int.class)
                        .newInstance(installer, App.Companion.getContext().getAttributionTag(), installerPackageName, userId);
            } else {
                return PackageInstaller.class.getConstructor(IPackageInstaller.class, String.class, int.class)
                        .newInstance(installer, installerPackageName, userId);
            }
        } else {
            return PackageInstaller.class.getConstructor(Context.class, PackageManager.class, IPackageInstaller.class, String.class, int.class)
                    .newInstance(App.Companion.getContext(), App.Companion.getContext().getPackageManager(), installer, installerPackageName, userId);
        }
    }

    public static PackageInstaller.Session createSession(IPackageInstallerSession session) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return PackageInstaller.Session.class.getConstructor(IPackageInstallerSession.class)
                .newInstance(session);
    }

    public static int getInstallFlags(PackageInstaller.SessionParams params) throws NoSuchFieldException, IllegalAccessException {
        return (int) PackageInstaller.SessionParams.class.getDeclaredField("installFlags").get(params);
    }

    public static void setInstallFlags(PackageInstaller.SessionParams params, int newValue) throws NoSuchFieldException, IllegalAccessException {
        PackageInstaller.SessionParams.class.getDeclaredField("installFlags").set(params, newValue);
    }

}
