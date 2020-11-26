package com.lihui.android.liver.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lihui.android.liver.R;

import java.util.ArrayList;

public class FilterAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<FilterBean> filterBeans;

    private int lastSelect = 0;

    public FilterAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = View.inflate(context, R.layout.item_filter, null);
        return new FilterHolder(inflate,context,onItemSelectListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        FilterHolder h = (FilterHolder) holder;
        h.setData(filterBeans.get(position),position);
    }

    @Override
    public int getItemCount() {
        return filterBeans == null? 0:filterBeans.size();
    }

    public void setData(ArrayList<FilterBean> filterBeans) {
        this.filterBeans = filterBeans;
    }

    class FilterHolder extends RecyclerView.ViewHolder {

        private Context context;
        private ImageView icon;
        private TextView textView;
        private OnItemSelectListener onItemSelectListener;
        public FilterHolder(@NonNull View itemView,Context context,OnItemSelectListener onItemSelectListener) {

            super(itemView);
            this.context = context ;
            icon = itemView.findViewById(R.id.iv_icon);
            textView = itemView.findViewById(R.id.tv_name);

            this.onItemSelectListener = onItemSelectListener;
        }

        public void setData(FilterBean filterBean,int pos) {

            if (filterBean.isSelect()) {
                icon.setBackgroundColor(context.getResources().getColor(R.color.purple_200));
            } else {
                icon.setBackgroundColor(Color.TRANSPARENT);
            }
            textView.setText(filterBean.getName());

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemSelectListener != null) {
                        onItemSelectListener.onSelect(pos);
                    }
                    filterBeans.get(lastSelect).setSelect(false);
                    filterBean.setSelect(true);
                    notifyDataSetChanged();
                    lastSelect = pos;
                }
            });

        }
    }


    public interface OnItemSelectListener {
        void onSelect(int pos);
    }

    private OnItemSelectListener onItemSelectListener;

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }
}
