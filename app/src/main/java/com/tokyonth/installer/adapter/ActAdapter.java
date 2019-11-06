package com.tokyonth.installer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tokyonth.installer.R;

import java.util.List;

public class ActAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> list;

    public void setList(List<String> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_item_act, parent, false);
        return new ActHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ActHolder) {
            ((ActHolder) holder).tv_act.setText(list.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ActHolder extends RecyclerView.ViewHolder {

        private TextView tv_act;

        ActHolder(@NonNull View itemView) {
            super(itemView);
            tv_act = itemView.findViewById(R.id.tv_act);
        }
    }

}
