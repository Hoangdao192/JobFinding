package com.uet.fwork.post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.model.post.ReactionModel;
import com.uet.fwork.database.repository.CommentRepository;
import com.uet.fwork.database.repository.PostReactionRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.util.TimestampToString;

import java.util.Calendar;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    Context context;
    List<PostModel> postModelList;

    private PostReactionRepository reactionRepository;
    private CommentRepository commentRepository;
    private FirebaseUser firebaseUser;

    public PostsAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
        reactionRepository = new PostReactionRepository(context, FirebaseDatabase.getInstance());
        commentRepository = new CommentRepository(context, FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
        PostModel post = postModelList.get(position);

        //get data
        String uid = post.getUserId();
        String uEmail = post.getUserEmail();
        String uName = post.getUserName();
        String uDp = post.getUserDp();
        String pId = post.getPostId();
        String pJobName = post.getPostName();
        String pJobMajor = post.getPostMajor();
        String pJobAddress = post.getPostAddress();
        String pJobExperience = post.getPostExperience();
        String pJobSalary = post.getPostSalary();
        String pJobDescription = post.getPostDescription();
        String pJobImage = post.getPostImage();
        String pTimeStamp = post.getPostTime().toString();

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
        holder.pTimeTv.setText(TimestampToString.convert(post.getPostTime()));

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

        reactionRepository.isUserLikePost(post.getPostId(), firebaseUser.getUid(), new Repository.OnQuerySuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_no_fill));
                    reactionRepository.removeReactionByPostAndUser(post.getPostId(), firebaseUser.getUid());
                } else {
                    holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_fill));
                    reactionRepository.insert(new ReactionModel(
                            firebaseUser.getUid(), post.getPostId(), Calendar.getInstance().getTimeInMillis()/1000
                    ), null);
                }
            }
        });
        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reactionRepository.isUserLikePost(post.getPostId(), firebaseUser.getUid(), new Repository.OnQuerySuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (result) {
                            holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_no_fill));
                            reactionRepository.removeReactionByPostAndUser(post.getPostId(), firebaseUser.getUid());
                        } else {
                            holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_fill));
                            reactionRepository.insert(new ReactionModel(
                                    firebaseUser.getUid(), post.getPostId(), Calendar.getInstance().getTimeInMillis()/1000
                            ), null);
                        }
                    }
                });
            }
        });
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentViewDialog commentViewDialog = new CommentViewDialog(context, post.getPostId());
                commentViewDialog.show();
            }
        });

        reactionRepository.getNumberOfReaction(post.getPostId(), new Repository.OnQuerySuccessListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                holder.txvLikeNumber.setText(result + " Lượt thích");
            }
        });

        commentRepository.getNumberOfComment(post.getPostId(), new Repository.OnQuerySuccessListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                holder.txvCommentNumber.setText(result + " Bình luận");
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
        ImageView uAvatarIv, postImageIv, btnLike;
        TextView uNameTv, pTimeTv, pJobNameTv, pJobMajorTv, pJobAddressTv, pJobExperienceTv, pJobSalaryTv, pJobDescriptionTv;
        ImageButton moreButton;
        AppCompatButton commentButton;

        private TextView txvLikeNumber, txvCommentNumber;

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
            moreButton = itemView.findViewById(R.id.postMoreButton);
            btnLike = itemView.findViewById(R.id.btnLike);
            commentButton = itemView.findViewById(R.id.postCommentButton);

            txvCommentNumber = itemView.findViewById(R.id.txtCommentNumber);
            txvLikeNumber = itemView.findViewById(R.id.txtLikeNumber);
        }

    }
}
