package com.tokyonth.installer.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.installer.databinding.ItemRvFreezeAppBinding
import com.tokyonth.installer.utils.AppPackageUtils
import java.util.ArrayList

class FreezeAdapter(private val context: Context, private val list: ArrayList<String>) : RecyclerView.Adapter<FreezeAdapter.FreezeViewHolder>() {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FreezeViewHolder {
        val vb = ItemRvFreezeAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FreezeViewHolder(vb)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FreezeViewHolder, position: Int) {
        val appName = AppPackageUtils.getAppNameByPackageName(context, list[position])
        val appIcon = AppPackageUtils.getAppIconByPackageName(context, list[position])

        holder.bind(appName, list[position], appIcon)
        holder.itemView.setOnClickListener {
            listener!!.onClick(position, list[position])
        }
    }

    fun setItemListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun interface OnItemClickListener {

        fun onClick(position: Int, pkgName: String)

    }

    class FreezeViewHolder(private val vb: ItemRvFreezeAppBinding) : RecyclerView.ViewHolder(vb.root) {

        fun bind(appName: String, appSub: String, appIcon: Drawable) {
            vb.tvFreezeAppName.text = appName
            vb.tvFreezeSub.text = appSub
            vb.ivFreezeIcon.setImageDrawable(appIcon)
        }

    }

}
