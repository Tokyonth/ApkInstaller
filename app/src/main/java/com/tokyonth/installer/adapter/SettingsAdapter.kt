package com.tokyonth.installer.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.bean.SettingsBean
import com.tokyonth.installer.databinding.ItemRvSettingsBinding
import com.tokyonth.installer.utils.CommonUtils
import com.tokyonth.installer.utils.SPUtils.get
import com.tokyonth.installer.utils.SPUtils.set
import com.tokyonth.installer.view.BurnRoundView
import com.tokyonth.installer.view.CustomizeDialog
import com.tokyonth.installer.view.SwitchButton

import java.util.ArrayList

class SettingsAdapter(private val activity: Activity, private val list: ArrayList<SettingsBean>) :
        RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private var onItemSwitchClick: OnItemSwitchClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val vb = ItemRvSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingsViewHolder(vb)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val bean = list[position]
        holder.title.text = bean.title
        holder.sub.text = bean.sub
        holder.icon.setBurnSrc(bean.icon, bean.color, true)
        holder.switchBtn.isChecked = bean.selected

        if (position == 4) {
            holder.switchBtn.visibility = View.GONE
            holder.itemView.setOnClickListener {
                CustomizeDialog.getInstance(activity)
                        .setSingleChoiceItems(R.array.install_mode_arr, activity[Constants.SP_INSTALL_MODE, 0]) { dialog, which ->
                            activity[Constants.SP_INSTALL_MODE] = which
                            if (which == 1) {
                                CommonUtils.requestPermissionByShizuku(activity)
                            } else if (which == 2) {
                                CommonUtils.requestPermissionByIcebox(activity)
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
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
        holder.switchBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
            onItemSwitchClick!!.onItemClick(compoundButton, position, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnItemClick(onItemSwitchClick: OnItemSwitchClick) {
        this.onItemSwitchClick = onItemSwitchClick
    }

    fun interface OnItemSwitchClick {

        fun onItemClick(view: View, pos: Int, bool: Boolean)

    }

    class SettingsViewHolder(vb: ItemRvSettingsBinding) : RecyclerView.ViewHolder(vb.root) {

        val title: TextView = vb.settingsItemTitle
        val sub: TextView = vb.settingsItemSub
        val icon: BurnRoundView = vb.settingsItemIcon
        val switchBtn: SwitchButton = vb.settingsItemSwitch

    }

}

