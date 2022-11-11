package com.uet.fwork;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.uet.fwork.database.model.UserModel;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyHolder> {

    Context context;
    List<UserModel> userModelList;

    //constructor
    public UsersAdapter(Context context, List<UserModel> userModelList){
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_search.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_search,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String userImage = userModelList.get(position).getAvatar();
        String userName = userModelList.get(position).getFullName();

        //set data
        holder.mNameTv.setText(userName);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.user).into(holder.mAvatarIv);
        } catch (Exception e){

        }

        //click item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView mAvatarIv;
        TextView mNameTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
        }
    }

}
