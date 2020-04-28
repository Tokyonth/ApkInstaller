package com.tokyonth.installer.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyonth.installer.R;

import static android.widget.Toast.LENGTH_SHORT;

public class ToastUtil {

    public static int DEFAULT_SITE = 0;
    public static int CENTER_SITE = 1;

    public static void showToast(Context context, String msg, int site) {
        View view = View.inflate(context, R.layout.layout_common_toast, null);
        Toast toast = new Toast(context);
        toast.setView(view);
        ((TextView) view.findViewById(R.id.tv_common_toast)).setText(msg);
        toast.setDuration(LENGTH_SHORT);
        if (site == CENTER_SITE) {
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        toast.show();
    }

}
