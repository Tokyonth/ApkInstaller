package com.tokyonth.installer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.installer.App
import com.tokyonth.installer.R

import com.tokyonth.installer.data.SettingsEntity
import com.tokyonth.installer.databinding.ItemRvSettingsBinding
import com.tokyonth.installer.view.BurnRoundView
import com.tokyonth.installer.view.SwitchButton

import java.util.ArrayList

class SettingsAdapter(context: Context) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private val list: ArrayList<SettingsEntity> = ArrayList()

    init {
        list.apply {
            add(SettingsEntity(context.getString(R.string.title_show_perm), context.getString(R.string.summary_show_perm),
                    R.drawable.ic_perm, ContextCompat.getColor(context, R.color.color0),
                    App.localData.isShowPermission()))
            add(SettingsEntity(context.getString(R.string.title_show_activity), context.getString(R.string.summary_show_activity),
                    R.drawable.ic_activity, ContextCompat.getColor(context, R.color.color1),
                    App.localData.isShowActivity()))
            add(SettingsEntity(context.getString(R.string.default_silent), context.getString(R.string.default_silent_sub),
                    R.drawable.ic_silent, ContextCompat.getColor(context, R.color.color6),
                    App.localData.isDefaultSilent()))
            add(SettingsEntity(context.getString(R.string.auto_del_apk_title), context.getString(R.string.auto_delete_apk),
                    R.drawable.ic_delete, ContextCompat.getColor(context, R.color.color2),
                    App.localData.isAutoDel()))
            add(SettingsEntity(context.getString(R.string.follow_system_night_mode), context.getString(R.string.follow_system_night_mode_sub),
                    R.drawable.ic_follow, ContextCompat.getColor(context, R.color.color4),
                    App.localData.isFollowSystem()))
            add(SettingsEntity(context.getString(R.string.install_mode), App.localData.getInstallName() /*context.getString(R.string.install_mode_sub)*/,
                    R.drawable.ic_mode, ContextCompat.getColor(context, R.color.color5), false))
        }
    }

    interface OnItemClickListener {

        fun onSwitch(pos: Int, bool: Boolean)

        fun onClick(pos: Int)

    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    fun updateInstallMode() {
        list[list.size - 1].sub = App.localData.getInstallName()
        notifyItemChanged(list.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val vb = ItemRvSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingsViewHolder(vb)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(list[position], onItemClickListener!!)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class SettingsViewHolder(private val vb: ItemRvSettingsBinding) : RecyclerView.ViewHolder(vb.root) {

        private val title: TextView = vb.settingsItemTitle
        private val sub: TextView = vb.settingsItemSub
        private val icon: BurnRoundView = vb.settingsItemIcon
        private val switchBtn: SwitchButton = vb.settingsItemSwitch

        fun bind(entity: SettingsEntity, onItemClickListener: OnItemClickListener) {
            title.text = entity.title
            sub.text = entity.sub
            icon.setBurnSrc(entity.icon, entity.color, true)
            switchBtn.isChecked = entity.selected
            if (bindingAdapterPosition == 5) {
                switchBtn.visibility = View.GONE
            }

            switchBtn.setOnCheckedChangeListener { _, isChecked ->
                onItemClickListener.onSwitch(bindingAdapterPosition, isChecked)
            }
            vb.root.setOnClickListener {
                onItemClickListener.onClick(bindingAdapterPosition)
            }
        }
    }

}

