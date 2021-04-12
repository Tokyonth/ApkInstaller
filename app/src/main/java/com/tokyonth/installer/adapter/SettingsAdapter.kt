package com.tokyonth.installer.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.kyleduo.switchbutton.SwitchButton
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.bean.SettingsBean
import com.tokyonth.installer.utils.HelperTools
import com.tokyonth.installer.utils.SPUtils.get
import com.tokyonth.installer.utils.SPUtils.set
import com.tokyonth.installer.view.BurnRoundView
import com.tokyonth.installer.view.CustomizeDialog

import java.util.ArrayList

class SettingsAdapter(private val activity: Activity, private val list: ArrayList<SettingsBean>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var context: Context
    private var onItemSwitchClick: OnItemSwitchClick? = null

    fun setOnItemClick(onItemSwitchClick: OnItemSwitchClick) {
        this.onItemSwitchClick = onItemSwitchClick
    }

    interface OnItemSwitchClick {
        fun onItemClick(view: View, pos: Int, bool: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
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
                            .setSingleChoiceItems(R.array.install_mode_arr, context[Constants.SP_INSTALL_MODE, 0]) { dialog, which ->
                                context[Constants.SP_INSTALL_MODE] = which
                                if (which == 1) {
                                    HelperTools.requestPermissionByShizuku(activity)
                                } else if (which == 2) {
                                    HelperTools.requestPermissionByIcebox(activity)
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
            holder.switchBtn.setOnCheckedChangeListener { compoundButton, isChecked
                ->
                onItemSwitchClick!!.onItemClick(compoundButton, position, isChecked)
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

