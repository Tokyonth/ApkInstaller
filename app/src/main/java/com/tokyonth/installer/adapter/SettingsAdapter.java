package com.tokyonth.installer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kyleduo.switchbutton.SwitchButton;
import com.tokyonth.installer.Constants;
import com.tokyonth.installer.R;
import com.tokyonth.installer.bean.SettingsBean;
import com.tokyonth.installer.utils.SPUtils;
import com.tokyonth.installer.widget.BurnRoundView;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SettingsBean> list;
    private OnItemSwitchClick onItemSwitchClick;

    public SettingsAdapter(ArrayList<SettingsBean> list) {
        this.list = list;
    }

    public void setOnItemClick(OnItemSwitchClick onItemSwitchClick) {
        this.onItemSwitchClick = onItemSwitchClick;
    }

    public interface OnItemSwitchClick {
        void onItemClick(View view, int pos, boolean bool);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_settings_item, parent, false);
        return new CommonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof CommonViewHolder) {
            SettingsBean bean = list.get(position);
            ((CommonViewHolder) holder).title.setText(bean.getTitle());
            ((CommonViewHolder) holder).sub.setText(bean.getSub());
            ((CommonViewHolder) holder).icon.setBurnSrc(bean.getIcon(), bean.getColor());
            ((CommonViewHolder) holder).switchBtn.setOnCheckedChangeListener((compoundButton, isChecked) -> onItemSwitchClick.onItemClick(compoundButton, position, isChecked));
            switch (position) {
                case 0:
                    ((CommonViewHolder) holder).switchBtn.setChecked((boolean)SPUtils.getData(Constants.SP_SHOW_PERM, true));
                    break;
                case 1:
                    ((CommonViewHolder) holder).switchBtn.setChecked((boolean)SPUtils.getData(Constants.SP_SHOW_ACT, true));
                    break;
                case 2:
                    ((CommonViewHolder) holder).switchBtn.setChecked((boolean)SPUtils.getData(Constants.SP_VIBRATE, false));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    static class CommonViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView sub;
        private BurnRoundView icon;
        private SwitchButton switchBtn;

        CommonViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.settings_item_title);
            sub = itemView.findViewById(R.id.settings_item_sub);
            icon = itemView.findViewById(R.id.settings_item_icon);
            switchBtn = itemView.findViewById(R.id.settings_item_switch);
        }
    }

}

