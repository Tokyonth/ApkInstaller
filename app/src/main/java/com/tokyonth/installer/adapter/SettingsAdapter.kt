package com.tokyonth.installer.adapter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.catchingnow.icebox.sdk_client.IceBox

import com.kyleduo.switchbutton.SwitchButton
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.FreezeActivity
import com.tokyonth.installer.base.BaseApplication
import com.tokyonth.installer.bean.SettingsBean
import com.tokyonth.installer.utils.PermissionHelper
import com.tokyonth.installer.utils.SPUtils
import com.tokyonth.installer.widget.BurnRoundView
import com.tokyonth.installer.widget.CustomizeDialog
import moe.shizuku.api.ShizukuApiConstants

import java.util.ArrayList

class SettingsAdapter(private val activity: Activity, private val list: ArrayList<SettingsBean>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onItemSwitchClick: OnItemSwitchClick? = null

    fun setOnItemClick(onItemSwitchClick: OnItemSwitchClick) {
        this.onItemSwitchClick = onItemSwitchClick
    }

    interface OnItemSwitchClick {
        fun onItemClick(view: View, pos: Int, bool: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_settings_item, parent, false)
        return CommonViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommonViewHolder) {
            val bean = list!![position]
            holder.title.text = bean.title
            holder.sub.text = bean.sub
            holder.icon.setBurnSrc(bean.icon, bean.color, true)
            holder.switchBtn.isChecked = bean.selected

            if (position == 4) {
                holder.switchBtn.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    CustomizeDialog.getInstance(activity)
                            .setTitle(R.string.dialog_text_title)
                            .setSingleChoiceItems(R.array.install_mode_arr, SPUtils.getData(Constants.SP_INSTALL_MODE_KEY, 0) as Int) {
                                dialog, which ->
                                SPUtils.putData(Constants.SP_INSTALL_MODE_KEY, which)
                                if (which == 1) {
                                    PermissionHelper.requestPermissionByShizuku(activity)
                                } else if (which == 2) {
                                    PermissionHelper.requestPermissionByIcebox(activity)
                                }
                                dialog.dismiss()
                            }
                            .setCancelable(false)
                            .create().show()
                }
            }
           /* if (position == 5) {
                holder.switchBtn.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    activity.startActivity(Intent(BaseApplication.context, FreezeActivity::class.java))
                }
            }

            */
            holder.switchBtn.setOnCheckedChangeListener { compoundButton, isChecked
                -> onItemSwitchClick!!.onItemClick(compoundButton, position, isChecked)
            }
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    internal class CommonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.settings_item_title)
        val sub: TextView = itemView.findViewById(R.id.settings_item_sub)
        val icon: BurnRoundView = itemView.findViewById(R.id.settings_item_icon)
        val switchBtn: SwitchButton = itemView.findViewById(R.id.settings_item_switch)

    }

}

