package com.tokyonth.installer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.data.PermissionInfoEntity
import com.tokyonth.installer.databinding.ItemPermissionDetailBinding
import com.tokyonth.installer.utils.ktx.visibleOrGone

class PermissionAdapter(private val list: MutableList<PermissionInfoEntity>) :
    RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    private var itemClickListener: ((PermissionInfoEntity) -> Unit)? = null

    fun setItemClickListener(itemClickListener: (PermissionInfoEntity) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val binding =
            ItemPermissionDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.bind(list[position], itemClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class PermissionViewHolder(private val binding: ItemPermissionDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: PermissionInfoEntity,
            click: ((PermissionInfoEntity) -> Unit)? = null
        ) {
            binding.tvItemPermissionName.text = data.permissionName
            binding.tvItemPermissionDesc.text = data.permissionDesc
            binding.tvItemPermissionDesc.visibleOrGone(data.permissionDesc.isNotEmpty())
            binding.root.setOnClickListener {
                click?.invoke(data)
            }
        }

    }

}
