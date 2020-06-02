package com.tokyonth.installer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tokyonth.installer.R;
import com.tokyonth.installer.bean.permissions.PermFullBean;
import com.tokyonth.installer.widget.CustomizeDialog;

import java.util.ArrayList;

public class PermissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<PermFullBean> list;
    private Context context;

    public PermissionAdapter(ArrayList<PermFullBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_permission_item, parent, false);
        return new PermissionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PermissionHolder) {
            PermFullBean bean = list.get(position);
            ((PermissionHolder) holder).tv_perm.setText(bean.getPerm());
            ((PermissionHolder) holder).tv_des.setText(bean.getDes());
            if (list.get(position).getDes() == null) {
                ((PermissionHolder) holder).tv_des_div.setVisibility(View.GONE);
            } else {
                ((PermissionHolder) holder).tv_des_div.setVisibility(View.VISIBLE);
            }
            ((PermissionHolder) holder).ll_perm.setOnClickListener(v -> {
                String group = bean.getGroup() == null ? context.getResources().getString(R.string.text_no_description) : bean.getGroup();
                String lab = bean.getLab() == null ? context.getResources().getString(R.string.text_no_description) : bean.getLab();
                String des = bean.getDes() == null ? context.getResources().getString(R.string.text_no_description) : bean.getDes();

                CustomizeDialog.getInstance(context)
                        .setTitle(group)
                        .setMessage(bean.getPerm() + "\n" + lab + "\n" + des)
                        .setPositiveButton(R.string.dialog_ok, null)
                        .setCancelable(false).create().show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class PermissionHolder extends RecyclerView.ViewHolder {

        private TextView tv_perm;
        private TextView tv_des;
        private TextView tv_des_div;
        private LinearLayout ll_perm;

        PermissionHolder(@NonNull View itemView) {
            super(itemView);
            tv_perm = itemView.findViewById(R.id.tv_perm);
            tv_des = itemView.findViewById(R.id.tv_des);
            tv_des_div = itemView.findViewById(R.id.tv_des_div);
            ll_perm = itemView.findViewById(R.id.ll_perm);
        }
    }

}
