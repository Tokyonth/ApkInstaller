package com.tokyonth.installer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.tokyonth.installer.R

import java.util.ArrayList

class ActivityAdapter(private val list: ArrayList<String>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_activity_item, parent, false)
        return ActivityHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ActivityHolder) {
            holder.tvAct.text = list!![position]
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    internal class ActivityHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvAct: TextView = itemView.findViewById(R.id.tv_act)

    }

}