package com.tokyonth.installer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.installer.R
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.data.SettingsEntity
import com.tokyonth.installer.databinding.ItemRvSettingsBinding

import java.util.ArrayList

class SettingsAdapter(context: Context) :
    RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private val list: ArrayList<SettingsEntity> = ArrayList()

    private val local = SPDataManager.instance

    init {
        list.apply {
            add(
                SettingsEntity(
                    context.getString(R.string.title_show_perm),
                    context.getString(R.string.summary_show_perm),
                    R.drawable.ic_perm,
                    ContextCompat.getColor(context, R.color.color0),
                    local.isShowPermission()
                )
            )
            add(
                SettingsEntity(
                    context.getString(R.string.title_show_activity),
                    context.getString(R.string.summary_show_activity),
                    R.drawable.ic_activity,
                    ContextCompat.getColor(context, R.color.color1),
                    local.isShowActivity()
                )
            )
            add(
                SettingsEntity(
                    context.getString(R.string.default_silent),
                    context.getString(R.string.default_silent_sub),
                    R.drawable.ic_silent,
                    ContextCompat.getColor(context, R.color.color6),
                    local.isDefaultSilent()
                )
            )
            add(
                SettingsEntity(
                    context.getString(R.string.auto_del_apk_title),
                    context.getString(R.string.auto_delete_apk),
                    R.drawable.ic_delete,
                    ContextCompat.getColor(context, R.color.color2),
                    local.isAutoDel()
                )
            )
            add(
                SettingsEntity(
                    context.getString(R.string.follow_system_night_mode),
                    context.getString(R.string.follow_system_night_mode_sub),
                    R.drawable.ic_follow,
                    ContextCompat.getColor(context, R.color.color4),
                    local.isFollowSystem()
                )
            )
            add(
                SettingsEntity(
                    context.getString(R.string.install_mode),
                    local.getInstallName() /*context.getString(R.string.install_mode_sub)*/,
                    R.drawable.ic_mode,
                    ContextCompat.getColor(context, R.color.color5),
                    false
                )
            )
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
        list[list.size - 1].sub = local.getInstallName()
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

    class SettingsViewHolder(private val vb: ItemRvSettingsBinding) :
        RecyclerView.ViewHolder(vb.root) {

        fun bind(entity: SettingsEntity, onItemClickListener: OnItemClickListener) {
            vb.settingsItemTitle.text = entity.title
            vb.settingsItemSub.text = entity.sub
            vb.settingsItemIcon.setBurnSrc(entity.icon, entity.color, true)

            vb.settingsItemSwitch.run {
                isChecked = entity.selected
                if (bindingAdapterPosition == 5) {
                    visibility = View.GONE
                }

                setOnCheckedChangeListener { _, isChecked ->
                    onItemClickListener.onSwitch(bindingAdapterPosition, isChecked)
                }
            }

            vb.root.setOnClickListener {
                onItemClickListener.onClick(bindingAdapterPosition)
            }
        }
    }

}
