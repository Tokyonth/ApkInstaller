package com.tokyonth.installer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.kyleduo.switchbutton.SwitchButton
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.activity.FreezeActivity
import com.tokyonth.installer.bean.SettingsBean
import com.tokyonth.installer.utils.SPUtils
import com.tokyonth.installer.widget.BurnRoundView

import java.util.ArrayList

class SettingsAdapter(private val context: Context, private val list: ArrayList<SettingsBean>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            holder.icon.setBurnSrc(bean.icon, bean.color)
            holder.switchBtn.setOnCheckedChangeListener { compoundButton, isChecked -> onItemSwitchClick!!.onItemClick(compoundButton, position, isChecked) }
            when (position) {
                0 -> holder.switchBtn.isChecked = SPUtils.getData(Constants.SP_SHOW_PERM, true) as Boolean
                1 -> holder.switchBtn.isChecked = SPUtils.getData(Constants.SP_SHOW_ACT, true) as Boolean
                2 -> holder.switchBtn.isChecked = SPUtils.getData(Constants.SP_VIBRATE, false) as Boolean
                3 -> holder.switchBtn.isChecked = SPUtils.getData(Constants.SP_IS_SHIZUKU_MODE, false) as Boolean
            }
            if (position == 4) {
                holder.switchBtn.visibility = View.GONE
                holder.itemView.setOnClickListener { context.startActivity(Intent(context, FreezeActivity::class.java)) }
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

