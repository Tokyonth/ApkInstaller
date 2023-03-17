package com.tokyonth.installer.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ActivityAdapter(private val list: ArrayList<String>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        return ActivityViewHolder(TextView(parent.context))
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ActivityViewHolder(private val textView: TextView) :
        RecyclerView.ViewHolder(textView) {

        fun bind(string: String) {
            textView.text = string
        }

    }

}
