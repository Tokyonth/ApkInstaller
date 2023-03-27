package com.catchingnow.icebox.sdk_client;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

class AuthorizeUtil {

    private static PendingIntent authorizePendingIntent = null;

    static PendingIntent getAuthorizedPI(Context context) {
        if (authorizePendingIntent == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                authorizePendingIntent = PendingIntent.getActivity(context, 0x333, new Intent(context, StateReceiver.class), PendingIntent.FLAG_IMMUTABLE);
            } else {
                authorizePendingIntent = PendingIntent.getActivity(context, 0x333, new Intent(context, StateReceiver.class), PendingIntent.FLAG_ONE_SHOT);
            }
        }
        return authorizePendingIntent;
    }

}
