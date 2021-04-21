package com.tokyonth.installer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.databinding.ItemRvActivityBinding

import java.util.ArrayList

class ActivityAdapter(private val list: ArrayList<String>) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val vb = ItemRvActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(vb)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ActivityViewHolder(private val vb: ItemRvActivityBinding) : RecyclerView.ViewHolder(vb.root) {

        fun bind(string: String) {
            vb.tvAct.text = string
        }

    }

}