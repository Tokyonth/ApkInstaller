package com.tokyonth.installer.utils

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.tokyonth.installer.R

object ToastUtil {

    @JvmField
    var DEFAULT_SITE = 0
    var CENTER_SITE = 1

    @JvmStatic
    fun showToast(context: Context?, msg: String?, site: Int) {
        val view = View.inflate(context, R.layout.layout_common_toast, null)
        val toast = Toast(context)
        toast.view = view
        (view.findViewById<View>(R.id.tv_common_toast) as TextView).text = msg
        toast.duration = Toast.LENGTH_SHORT
        if (site == CENTER_SITE) {
            toast.setGravity(Gravity.CENTER, 0, 0)
        }
        toast.show()
    }

}