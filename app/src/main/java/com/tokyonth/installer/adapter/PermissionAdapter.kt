package com.tokyonth.installer.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.data.PermFullEntity
import com.tokyonth.installer.view.item.PermissionItemView
import java.util.ArrayList

class PermissionAdapter(private val list: ArrayList<PermFullEntity>) : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    private lateinit var itemClickListener: (PermFullEntity) -> Unit

    fun setItemClickListener(itemClickListener: (PermFullEntity) -> Unit) {
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

    class PermissionViewHolder(private val permissionItemView: PermissionItemView) : RecyclerView.ViewHolder(permissionItemView) {

        fun bind(entity: PermFullEntity, itemClickListener: (PermFullEntity) -> Unit) {
            permissionItemView.setData(entity)
            permissionItemView.setOnClickListener {
                itemClickListener.invoke(entity)
            }
        }

    }

}
