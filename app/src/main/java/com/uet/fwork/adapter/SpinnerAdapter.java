package com.uet.fwork.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.LayoutRes;

import java.util.List;

public class SpinnerAdapter<T> extends BaseAdapter {
    private Context context;
    private List<T> objectList;
    private int itemLayoutId;
    private OnViewCreatedListener onViewCreatedListener;

    public SpinnerAdapter(
            Context context, List<T> objectList, @LayoutRes int itemLayoutId,
            OnViewCreatedListener onViewCreatedListener
    ) {
        this.context = context;
        this.objectList = objectList;
        this.itemLayoutId = itemLayoutId;
        this.onViewCreatedListener = onViewCreatedListener;
    }

    @Override
    public int getCount() {
        return objectList.size();
    }

    @Override
    public Object getItem(int position) {
        return objectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(itemLayoutId, null);
        onViewCreatedListener.onViewCreated(convertView, position);
        return convertView;
    }

    public List<T> getList() {
        return this.objectList;
    }

    public interface OnViewCreatedListener {
        void onViewCreated(View view, int position);
    }
}
