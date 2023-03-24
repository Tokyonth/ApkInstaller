package com.tokyonth.installer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.R
import com.tokyonth.installer.data.SPDataManager
import com.tokyonth.installer.data.SettingsEntity
import com.tokyonth.installer.databinding.ItemSettingPerfBinding
import com.tokyonth.installer.utils.ktx.color
import com.tokyonth.installer.utils.ktx.string

import java.util.ArrayList

class SettingsAdapter : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private val list: MutableList<SettingsEntity> = ArrayList()

    init {
        list.apply {
            add(
                SettingsEntity(
                    string(R.string.title_show_perm),
                    string(R.string.summary_show_perm),
                    R.drawable.round_security_24,
                    color(R.color.color0),
                    SPDataManager.instance.isShowPermission()
                )
            )
            add(
                SettingsEntity(
                    string(R.string.title_show_activity),
                    string(R.string.summary_show_activity),
                    R.drawable.round_layers_24,
                    color(R.color.color1),
                    SPDataManager.instance.isShowActivity()
                )
            )
            add(
                SettingsEntity(
                    string(R.string.default_silent),
                    string(R.string.default_silent_sub),
                    R.drawable.round_silence_24,
                    color(R.color.color6),
                    SPDataManager.instance.isDefaultSilent()
                )
            )
            add(
                SettingsEntity(
                    string(R.string.auto_del_apk_title),
                    string(R.string.auto_delete_apk),
                    R.drawable.round_delete_forever_24,
                    color(R.color.color2),
                    SPDataManager.instance.isAutoDel()
                )
            )
            add(
                SettingsEntity(
                    string(R.string.follow_system_night_mode),
                    string(R.string.follow_system_night_mode_sub),
                    R.drawable.round_auto_mode_24,
                    color(R.color.color4),
                    SPDataManager.instance.isFollowSystem()
                )
            )
            add(
                SettingsEntity(
                    string(R.string.install_mode),
                    SPDataManager.instance.getInstallName(),
                    R.drawable.round_play_for_work_24,
                    color(R.color.color5),
                    false
                )
            )
        }
    }

    interface OnItemActionListener {

        fun onSwitch(pos: Int, bool: Boolean)

        fun onClick(pos: Int)

    }

    private var onItemClickListener: OnItemActionListener? = null

    fun setOnItemActionListener(onItemActionListener: OnItemActionListener) {
        this.onItemClickListener = onItemActionListener
    }

    fun updateInstallMode() {
        list[list.size - 1].sub = SPDataManager.instance.getInstallName()
        notifyItemChanged(list.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val binding =
            ItemSettingPerfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(list[position], onItemClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class SettingsViewHolder(private val binding: ItemSettingPerfBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: SettingsEntity,
            action: OnItemActionListener?
        ) {
            binding.itemSettingTitle.text = data.title
            binding.itemSettingSub.text = data.sub
            binding.itemSettingIcon.setBurnSrc(data.icon, data.color, true)

            binding.itemSettingSwitch.run {
                isChecked = data.selected
                if (bindingAdapterPosition == 5) {
                    visibility = View.GONE
                }

                setOnCheckedChangeListener { _, isChecked ->
                    action?.onSwitch(bindingAdapterPosition, isChecked)
                }
            }

            binding.root.setOnClickListener {
                action?.onClick(bindingAdapterPosition)
            }
        }
    }

}
