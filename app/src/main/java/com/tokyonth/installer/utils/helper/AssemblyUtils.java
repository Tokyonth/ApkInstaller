package com.tokyonth.installer.utils.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.tokyonth.installer.Contents;
import com.tokyonth.installer.utils.file.SPUtils;

import java.io.File;

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

    public static void StartSystemPkgInstall(Context context, String filePath) {
        Intent intent = new Intent();
        String act = null;
        String sysPkgName;
        if ((boolean) SPUtils.getData(Contents.SP_USE_SYS_PKG, false)) {
            sysPkgName = (String) SPUtils.getData(Contents.SYS_PKG_NAME, Contents.SYS_PKG_NAME);
        } else {
            sysPkgName = Contents.SYS_PKG_NAME;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(sysPkgName, PackageManager.GET_ACTIVITIES);
            act = packageInfo.activities[0].name;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert act != null;
        ComponentName cn = new ComponentName(sysPkgName, act);
        intent.setComponent(cn);
        Uri apkUri = FileProvider.getUriForFile(context, Contents.PROVIDER_STR,
                new File(filePath));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, Contents.URI_DATA_TYPE);
        context.startActivity(intent);
    }

}

