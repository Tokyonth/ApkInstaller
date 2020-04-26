package com.tokyonth.installer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tokyonth.installer.R;

import java.util.ArrayList;

public class ActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> list;
    
    public ActivityAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_info_item_act, parent, false);
        return new ActivityHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ActivityHolder) {
            ((ActivityHolder) holder).tv_act.setText(list.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ActivityHolder extends RecyclerView.ViewHolder {

        private TextView tv_act;

        ActivityHolder(@NonNull View itemView) {
            super(itemView);
            tv_act = itemView.findViewById(R.id.tv_act);
        }
    }

}