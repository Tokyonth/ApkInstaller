package com.tokyonth.installer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tokyonth.installer.R;
import com.tokyonth.installer.bean.InfoBean;

import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<InfoBean> list;

    public void setList(List<InfoBean> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_item_permission, parent, false);
        return new PermissionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PermissionHolder) {
            ((PermissionHolder) holder).tv_perm.setText(list.get(position).getPerm());

            ((PermissionHolder) holder).tv_des.setText(list.get(position).getDes());
            if (((PermissionHolder) holder).tv_des.getText().equals("")) {
                ((PermissionHolder) holder).tv_des_div.setVisibility(View.GONE);
            } else {
                ((PermissionHolder) holder).tv_des_div.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PermissionHolder extends RecyclerView.ViewHolder {

        private TextView tv_perm;
        private TextView tv_des;
        private TextView tv_des_div;

        PermissionHolder(@NonNull View itemView) {
            super(itemView);
            tv_perm = itemView.findViewById(R.id.tv_perm);
            tv_des = itemView.findViewById(R.id.tv_des);
            tv_des_div = itemView.findViewById(R.id.tv_des_div);
        }
    }

}
