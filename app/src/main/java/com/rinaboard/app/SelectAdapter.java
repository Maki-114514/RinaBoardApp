package com.rinaboard.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SelectAdapter extends ArrayAdapter<String> {
    private int selectedPosition = -1; // 记录当前选中的位置

    public SelectAdapter(Context context, List<String> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String item = getItem(position);
        TextView textView = itemView.findViewById(android.R.id.text1);
        textView.setText(item);

        // 设置选中状态的背景颜色
        if (position == selectedPosition) {
            itemView.setBackgroundColor(android.graphics.Color.LTGRAY);
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT); // 默认透明背景
        }

        return itemView;
    }

    // 设置选中位置
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // 刷新列表以更新视图
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
