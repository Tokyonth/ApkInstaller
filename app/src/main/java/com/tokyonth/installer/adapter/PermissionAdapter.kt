package com.tokyonth.installer.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.data.PermissionInfoEntity
import com.tokyonth.installer.view.item.PermissionItemView

class PermissionAdapter(private val list: MutableList<PermissionInfoEntity>) :
    RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    private lateinit var itemClickListener: (PermissionInfoEntity) -> Unit

    fun setItemClickListener(itemClickListener: (PermissionInfoEntity) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        return PermissionViewHolder(PermissionItemView(parent.context))
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.bind(list[position], itemClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class PermissionViewHolder(private val permissionItemView: PermissionItemView) :
        RecyclerView.ViewHolder(permissionItemView) {

        fun bind(entity: PermissionInfoEntity, itemClickListener: (PermissionInfoEntity) -> Unit) {
            permissionItemView.setData(entity)
            permissionItemView.setOnClickListener {
                itemClickListener.invoke(entity)
            }
        }

    }

}
