package com.tokyonth.installer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.installer.R
import com.tokyonth.installer.utils.AppPackageUtils
import java.util.ArrayList

class FreezeAdapter(private val context: Context?, private val list: ArrayList<String>?) : RecyclerView.Adapter<FreezeAdapter.FreezeViewHolder>() {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FreezeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_freeze_app, parent, false)
        return FreezeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: FreezeViewHolder, position: Int) {
        val appName = AppPackageUtils.getAppNameByPackageName(context, list!![position])
        val appIcon = AppPackageUtils.getAppIconByPackageName(context, list[position])
        holder.tvFreezeAppName.text = appName
        holder.tvFreezeSub.text = list[position]
        holder.ivFreezeIcon.setImageDrawable(appIcon)
        holder.itemView.setOnClickListener {
            listener!!.onClick(position, list[position])
        }
    }

    fun setItemListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {

        fun onClick(position: Int, pkgName: String)

    }

    class FreezeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvFreezeAppName: TextView = itemView.findViewById(R.id.tv_freeze_app_name)
        var tvFreezeSub: TextView = itemView.findViewById(R.id.tv_freeze_sub)
        var ivFreezeIcon: ImageView = itemView.findViewById(R.id.iv_freeze_icon)

    }

}
