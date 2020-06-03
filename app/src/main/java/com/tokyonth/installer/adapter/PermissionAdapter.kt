package com.tokyonth.installer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.R
import com.tokyonth.installer.bean.permissions.PermFullBean
import com.tokyonth.installer.widget.CustomizeDialog

import java.util.ArrayList

class PermissionAdapter(private val list: ArrayList<PermFullBean>?, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_permission_item, parent, false)
        return PermissionHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PermissionHolder) {
            val bean = list!![position]
            holder.tvPerm.text = bean.perm
            holder.tvDes.text = bean.des
            if (list[position].des == "") {
                holder.tvDesDiv.visibility = View.GONE
            } else {
                holder.tvDesDiv.visibility = View.VISIBLE
            }
            holder.llPerm.setOnClickListener {
                //val group = if (bean.group == null) context.resources.getString(R.string.text_no_description) else bean.group
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

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    internal class PermissionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvPerm: TextView = itemView.findViewById(R.id.tv_perm)
        val tvDes: TextView = itemView.findViewById(R.id.tv_des)
        val tvDesDiv: TextView = itemView.findViewById(R.id.tv_des_div)
        val llPerm: LinearLayout = itemView.findViewById(R.id.ll_perm)

    }

}
