package com.tokyonth.installer.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tokyonth.installer.R;

public class ToastUtil {

    /**
     * refreshToast:在屏幕下部显示Toast提示信息. <br/>
     *
     * @param context  -    上下文
     * @param msg      -    提示消息
     * @param lastTime -    持续时间，0-短时间，LENGTH_SHORT；1-长时间，LENGTH_LONG；
     */
    public static void showToast(Context context, String msg, int lastTime) {
        View view = View.inflate(context, R.layout.common_toast, null);
        Toast toast = new Toast(context);
        toast.setView(view);
        ((TextView) view.findViewById(R.id.common_toast_tv)).setText(msg);
        toast.setDuration(lastTime);
        toast.show();
    }

}