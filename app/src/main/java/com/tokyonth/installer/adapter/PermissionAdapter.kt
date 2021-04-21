package com.tokyonth.installer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.R
import com.tokyonth.installer.bean.permissions.PermFullBean
import com.tokyonth.installer.databinding.ItemRvPermissionBinding
import com.tokyonth.installer.utils.visibleOrGone
import com.tokyonth.installer.view.CustomizeDialog

import java.util.ArrayList

class PermissionAdapter(private val list: ArrayList<PermFullBean>, private val context: Context) :
        RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val vb = ItemRvPermissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermissionViewHolder(vb)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.bind(list[position], context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class PermissionViewHolder(private val vb: ItemRvPermissionBinding) : RecyclerView.ViewHolder(vb.root) {

        fun bind(bean: PermFullBean, context: Context) {
            vb.tvPerm.text = bean.perm
            vb.tvDes.text = bean.des
            vb.tvDesDiv.visibleOrGone(bean.des != "")
            vb.llPerm.setOnClickListener {
                val lab = if (bean.lab == null) context.resources.getString(R.string.text_no_description) else bean.lab
                val des = if (bean.des == null) context.resources.getString(R.string.text_no_description) else bean.des

                CustomizeDialog.getInstance(context)
                        //.setTitle(group)
                        .setMessage(bean.perm + "\n\n" + lab + "\n\n" + des)
                        .setPositiveButton(R.string.dialog_ok, null)
                        .setCancelable(false).create().show()
            }
        }

    }

}
