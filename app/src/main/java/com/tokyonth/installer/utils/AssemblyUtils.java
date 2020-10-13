package com.tokyonth.installer.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.tokyonth.installer.Constants;
import com.tokyonth.installer.R;

import java.io.File;
import java.lang.reflect.Field;

public class AssemblyUtils {

    public static int ColorBurn(int RGBValues) {
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 - 0.1));
        green = (int) Math.floor(green * (1 - 0.1));
        blue = (int) Math.floor(blue * (1 - 0.1));
        return Color.rgb(red, green, blue);
    }

    public static Bitmap DrawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    public static void StartSystemPkgInstall(Context context, String filePath) {
        Intent intent = new Intent();
        String activityName = null;
        String sysPkgName;
        if ((boolean) SPUtils.getData(Constants.INSTANCE.getSP_USE_SYS_PKG(), false)) {
            sysPkgName = (String) SPUtils.getData(Constants.INSTANCE.getSYS_PKG_NAME(), Constants.INSTANCE.getSYS_PKG_NAME());
        } else {
            sysPkgName = Constants.INSTANCE.getSYS_PKG_NAME();
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(sysPkgName, PackageManager.GET_ACTIVITIES);
            activityName = packageInfo.activities[0].name;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (activityName != null) {
            ComponentName componentName = new ComponentName(sysPkgName, activityName);
            intent.setComponent(componentName);
            Uri apkUri = FileProvider.getUriForFile(context, Constants.INSTANCE.getPROVIDER_STR(),
                    new File(filePath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, Constants.INSTANCE.getURI_DATA_TYPE());
            context.startActivity(intent);
        } else {
            ToastUtil.showToast(context, context.getString(R.string.open_sys_pkg_failure), ToastUtil.DEFAULT_SITE);
        }

    }

    /**
     * 获取准确的Intent Referrer
     */
    public static String reflectGetReferrer(Context context) {
        try {
            Class<?> activityClass = Class.forName("android.app.Activity");
            //noinspection JavaReflectionMemberAccess
            Field refererField = activityClass.getDeclaredField("mReferrer");
            refererField.setAccessible(true);
            return (String) refererField.get(context);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

}
