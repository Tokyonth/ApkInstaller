package com.tokyonth.installer.info;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.tokyonth.installer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by Bave on 2018/1/5.
 */

public class DonateToMe {
    public static void show(final Context context) {
        if (context == null) return;
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        // wechat
                        showSaveQRCodeDialog(context, R.drawable.wechat_money_revised);
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        // alipay
                        if (haveInstalledAlipay(context)) {
                            jumpToAlipyScreen(context);
                        } else {
                            showSaveQRCodeDialog(context, R.drawable.alipay_money_revised);
                        }
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };
        new AlertDialog.Builder(context).setView(R.layout.layout_donate_dialog)
                .setTitle(R.string.title_donate_dialog_donate_methods)
                .setNeutralButton(android.R.string.no, onClickListener)
                .setPositiveButton(R.string.title_donate_dialog_alipay, onClickListener)
                .setNegativeButton(R.string.title_donate_dialog_wechat, onClickListener)
                .show();

    }

    private static void showSaveQRCodeDialog(Context context, int resId) {
        final ImageView imageView = new ImageView(context);
        imageView.setImageResource(resId);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        new AlertDialog.Builder(context).setView(imageView)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.title_donate_dialog_save_qr_code, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveImage(imageView);
                    }
                })
                .show();
    }

    private static void saveBitmap(Bitmap mBitmap, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveImage(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = imageView.getDrawingCache();
        File qrCode = new File(imageView.getContext().getExternalCacheDir(), "qrcode.jpg");
        saveBitmap(bitmap, qrCode);
        imageView.setDrawingCacheEnabled(false);
        imageView.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + qrCode.getAbsolutePath())));
        Toast.makeText(imageView.getContext(), R.string.text_donate_dialog_qr_code_saved, Toast.LENGTH_SHORT).show();
    }

    public static boolean haveInstalledAlipay(Context context) {
        try {
            return context.getPackageManager().getPackageInfo("com.eg.android.AlipayGphone", PackageManager.GET_ACTIVITIES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void jumpToAlipyScreen(Context context) {
        String qrcode = URLEncoder.encode("HTTPS://QR.ALIPAY.COM/FKX05494PUYB5GFV1VNXAD");
        String url = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + qrcode + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

}
