package com.vnsoftware.jobfinder.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.dialog.LoadingScreenDialog;
import com.vnsoftware.jobfinder.util.ApiAddress;
import com.vnsoftware.jobfinder.util.MapTextSearchAPI;
import com.vnsoftware.jobfinder.util.VietNameAdministrativeDivisionAPI;

import java.util.List;

public class AddressListRecyclerViewAdapter extends RecyclerView.Adapter<AddressListRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ApiAddress> addressList;
    private OnItemClickListener<ApiAddress> listener;

    public AddressListRecyclerViewAdapter(Context context, List<ApiAddress> addressList, OnItemClickListener<ApiAddress> listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recyclerview_address_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiAddress address = addressList.get(position);
        String placeName = address.getPlaceName();
        String placeAddress = address.getFullAddress();
        if (placeName.isEmpty()) {
            placeName = placeAddress.split(",")[0];
        }
        holder.txvPlaceName.setText(placeName);
        holder.txvPlaceAddress.setText(placeAddress);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingScreenDialog dialog = new LoadingScreenDialog(context);
                dialog.show();
                MapTextSearchAPI.getPlace(address.getId(), context, new VietNameAdministrativeDivisionAPI.OnApiResult<ApiAddress>() {
                    @Override
                    public void onResult(ApiAddress result) {
                        dialog.dismiss();
                        listener.onClick(result);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txvPlaceName, txvPlaceAddress;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.wrapper);
            txvPlaceName = itemView.findViewById(R.id.txtPlaceName);
            txvPlaceAddress = itemView.findViewById(R.id.txtPlaceAddress);
        }
    }

    public interface OnItemClickListener<T> {
        void onClick(T t);
    }
}
