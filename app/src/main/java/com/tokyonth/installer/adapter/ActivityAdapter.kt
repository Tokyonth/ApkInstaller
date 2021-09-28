package com.tokyonth.installer.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.tokyonth.installer.R
import java.util.ArrayList

class ActivityAdapter(private val list: ArrayList<String>) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        return ActivityViewHolder(activityText(parent.context))
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun activityText(context: Context): TextView {
        return TextView(context).apply {
            setTextColor(ResourcesCompat.getColor(context.resources, R.color.colorTextSub, null))
        }
    }

    class ActivityViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {

        fun bind(string: String) {
            textView.text = string
        }

    }

}
