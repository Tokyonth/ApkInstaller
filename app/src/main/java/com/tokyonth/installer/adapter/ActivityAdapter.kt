package com.tokyonth.installer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.R

import java.util.ArrayList

class ActivityAdapter(private val list: ArrayList<String>?) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.tvAct.text = list!![position]
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvAct: TextView = itemView.findViewById(R.id.tv_act)

    }

}