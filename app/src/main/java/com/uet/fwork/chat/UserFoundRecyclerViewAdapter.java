package com.uet.fwork.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFoundRecyclerViewAdapter extends RecyclerView.Adapter<UserFoundRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<UserModel> userList;
    private OnItemClickListener clickListener = null;
    private List<Target> targetList = new ArrayList<>();

    public UserFoundRecyclerViewAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    public UserFoundRecyclerViewAdapter(Context context, List<UserModel> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.clickListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recyclerview_chat_search, parent, false);
        return new ViewHolder(view, this.clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userAvatar = userList.get(position).getAvatar();
        holder.txtFullName.setText(userList.get(position).getFullName());
        if (!userAvatar.isEmpty()) {
            System.out.println(userList.get(position).getFullName());
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    holder.imgAvatar.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            targetList.add(target);
            Picasso.get().load(userAvatar)
                    .placeholder(R.drawable.wlop_33se)
                    .into(target);
        } else {
            Picasso.get().load(R.drawable.wlop_33se)
                    .placeholder(R.drawable.wlop_33se)
                    .into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<UserModel> userList) {
        this.userList.clear();
        this.targetList.clear();
        this.userList.addAll(userList);
        notifyDataSetChanged();
    }

    public void clearUserList() {
        userList.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private CircleImageView imgAvatar;
        private TextView txtFullName;
        public ViewHolder(@NonNull View itemView, @Nullable OnItemClickListener listener) {
            super(itemView);
            view = itemView;
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtFullName = itemView.findViewById(R.id.txtFullName);

            if (listener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClick(userList.get(getAdapterPosition()));
                    }
                });
            }
        }

        public View getView() {
            return view;
        }
    }

    public interface OnItemClickListener {
        void onClick(UserModel user);
    }
}
