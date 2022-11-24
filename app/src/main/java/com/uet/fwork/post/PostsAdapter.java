package com.uet.fwork.post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.database.model.post.PostModel;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    Context context;
    List<PostModel> postModelList;

    public PostsAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_posts.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String uid = postModelList.get(position).getUserId();
        String uEmail = postModelList.get(position).getUserEmail();
        String uName = postModelList.get(position).getUserName();
        String uDp = postModelList.get(position).getUserDp();
        String pId = postModelList.get(position).getPostId();
        String pJobName = postModelList.get(position).getPostName();
        String pJobMajor = postModelList.get(position).getPostMajor();
        String pJobAddress = postModelList.get(position).getPostAddress();
        String pJobExperience = postModelList.get(position).getPostExperience();
        String pJobSalary = postModelList.get(position).getPostSalary();
        String pJobDescription = postModelList.get(position).getPostDescription();
        String pJobImage = postModelList.get(position).getPostImage();
        String pTimeStamp = postModelList.get(position).getPostTime();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        //Calendar calendar = Calendar.getInstance(Locale.getDefault());
        //calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        //String pTime = DateFormat.format("dd/mm/yyyy hh:mm am/pm", calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        //holder.pTimeTv.setText(pTime);
        holder.pJobNameTv.setText(pJobName);
        holder.pJobMajorTv.setText(pJobMajor);
        holder.pJobDescriptionTv.setText(pJobDescription);
        holder.pJobSalaryTv.setText(pJobSalary);
        holder.pJobAddressTv.setText(pJobAddress);
        holder.pJobExperienceTv.setText(pJobExperience);

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.blank_user_avatar).into(holder.uAvatarIv);
        } catch (Exception e) {

        }

        //set post image
        //if post doesn't have any images, hide imageView
        if (pJobImage.equals("noImage")) {
            //hide imageView
            holder.postImageIv.setVisibility(View.GONE);
        } else {
            try {
                Picasso.get().load(pJobImage).into(holder.postImageIv);
            } catch (Exception e) {

            }
        }

        //Buttons click
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });

    }

    @Override
    public int getItemCount() {
        return postModelList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views from row_posts.xml
        ImageView uAvatarIv, postImageIv;
        TextView uNameTv, pTimeTv, pJobNameTv, pJobMajorTv, pJobAddressTv, pJobExperienceTv, pJobSalaryTv, pJobDescriptionTv, pLikesTv;
        ImageButton moreButton;
        AppCompatButton likeButton, commentButton;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            uAvatarIv = itemView.findViewById(R.id.uPostAvatarIv);
            postImageIv = itemView.findViewById(R.id.jobImagePost);
            uNameTv = itemView.findViewById(R.id.uPostNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pJobNameTv = itemView.findViewById(R.id.jobNamePost);
            pJobMajorTv = itemView.findViewById(R.id.jobMajorPost);
            pJobAddressTv = itemView.findViewById(R.id.jobAddressPost);
            pJobExperienceTv = itemView.findViewById(R.id.jobExperiencePost);
            pJobSalaryTv = itemView.findViewById(R.id.jobSalaryPost);
            pJobDescriptionTv = itemView.findViewById(R.id.jobDescriptionPost);
            pLikesTv = itemView.findViewById(R.id.postLikeTv);
            moreButton = itemView.findViewById(R.id.postMoreButton);
            likeButton = itemView.findViewById(R.id.postLikeButton);
            commentButton = itemView.findViewById(R.id.postCommentButton);
        }

    }
}
