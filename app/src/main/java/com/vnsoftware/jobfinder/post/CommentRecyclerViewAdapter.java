package com.vnsoftware.jobfinder.post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.database.model.UserModel;
import com.vnsoftware.jobfinder.database.model.post.CommentModel;
import com.vnsoftware.jobfinder.database.repository.Repository;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.util.TimestampToString;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<CommentModel> commentList;

    private UserRepository userRepository;

    public CommentRecyclerViewAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
        this.userRepository = UserRepository.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_recyclerview_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);
        userRepository.getUserByUID(comment.getUserId(), new Repository.OnQuerySuccessListener<UserModel>() {
            @Override
            public void onSuccess(UserModel user) {
                holder.txvUsername.setText(user.getFullName());
                holder.setUserAvatar(user.getAvatar());
                holder.txvComment.setText(comment.getContent());
                holder.txvTime.setText(TimestampToString.convert(comment.getCommentTime()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void setCommentList(List<CommentModel> commentList) {
        this.commentList.clear();
        this.commentList.addAll(commentList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView cirImgAvatar;
        private TextView txvUsername;
        private TextView txvComment, txvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cirImgAvatar = itemView.findViewById(R.id.imgAvatar);
            txvUsername = itemView.findViewById(R.id.txtUsername);
            txvComment = itemView.findViewById(R.id.txtComment);
            txvTime = itemView.findViewById(R.id.txtTime);
        }

        public void setUserAvatar(String url) {
            if (!url.isEmpty()) {
                Picasso.get().load(url).into(cirImgAvatar);
            }
        }
    }
}
