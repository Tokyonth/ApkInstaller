package com.tokyonth.installer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.installer.databinding.ItemActivityDetailBinding

class ActivityAdapter(private val list: MutableList<String>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding =
            ItemActivityDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ActivityViewHolder(private val binding: ItemActivityDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String) {
            binding.tvItemActivityName.text = name
        }

    }

}
