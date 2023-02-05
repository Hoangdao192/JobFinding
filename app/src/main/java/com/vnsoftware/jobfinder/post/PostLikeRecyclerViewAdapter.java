package com.vnsoftware.jobfinder.post;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.ViewProfileActivityCandidate;
import com.vnsoftware.jobfinder.ViewProfileActivityEmployer;
import com.vnsoftware.jobfinder.database.model.post.PostModel;
import com.vnsoftware.jobfinder.database.model.post.ReactionModel;
import com.vnsoftware.jobfinder.database.repository.PostReactionRepository;
import com.vnsoftware.jobfinder.database.repository.PostRepository;
import com.vnsoftware.jobfinder.database.repository.UserRepository;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostLikeRecyclerViewAdapter extends RecyclerView.Adapter<PostLikeRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<ReactionModel> reactionModels;
    private Map<String, PostModel> postMap = new HashMap<>();
    private PostRepository postRepository;
    private UserRepository userRepository;
    private PostReactionRepository postReactionRepository;
    DecimalFormat format = new DecimalFormat("0.#");

    public PostLikeRecyclerViewAdapter(Context context, List<ReactionModel> reactionModels) {
        this.context = context;
        this.reactionModels = reactionModels;
        this.postRepository = PostRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
        this.postReactionRepository = PostReactionRepository.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recyclerview_my_apply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReactionModel reactionModel = reactionModels.get(position);
        if (!postMap.containsKey(reactionModel.getPostId())) {
            postRepository.getById(reactionModel.getPostId(), post -> {
                postMap.put(reactionModel.getPostId(), post);
                loadPostDataIntoHolder(post, holder);
            });
        } else {
            loadPostDataIntoHolder(postMap.get(reactionModel.getPostId()), holder);
        }

        holder.btnRemove.setOnClickListener(v -> {
            postReactionRepository.deletePostReaction(
                    reactionModel, success -> {
                        if (success) {
                            reactionModels.remove(holder.getBindingAdapterPosition());
                            notifyItemRemoved(holder.getBindingAdapterPosition());
                        }
                    });
        });
    }

    private void loadPostDataIntoHolder(PostModel postModel, ViewHolder holder) {
        userRepository.getUserByUID(postModel.getUserId(), user -> {
            if (!user.getAvatar().equals("")) {
                Picasso.get().load(user.getAvatar()).into(holder.imgPostOwnerAvatar);
            }
            holder.txvPostOwnerName.setText(user.getFullName());
            holder.txvPostOwnerName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(user.getRole().equals("Candidate")) {
                        Intent intent = new Intent(context, ViewProfileActivityCandidate.class);
                        intent.putExtra("id",user.getId());
                        context.startActivity(intent);
                    } else if (user.getRole().equals("Employer")) {
                        Intent intent = new Intent(context, ViewProfileActivityEmployer.class);
                        intent.putExtra("id", user.getId());
                        context.startActivity(intent);
                    }


                }
            });
        });
        holder.txvJobDescription.setText("Mô tả công việc: " + postModel.getPostDescription());
        holder.txvJobSalary.setText("Mức lương tối thiểu: " + postModel.getPostSalary() + " triệu đồng");
        holder.txvJobExperience.setText("Kinh nghiệm: " + format.format(postModel.getPostExperience()) + " năm");
        holder.txvJobAddress.setText("Địa chỉ: " + postModel.getPostAddress());
        holder.txvJobName.setText("Tên công việc: " + postModel.getPostName());
        holder.txvJobMajor.setText("Chuyên ngành: " + postModel.getPostMajor());
    }

    @Override
    public int getItemCount() {
        return reactionModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgPostOwnerAvatar;
        private TextView txvPostOwnerName;
        private TextView txvJobName;
        private TextView txvJobMajor;
        private TextView txvJobAddress;
        private TextView txvJobExperience;
        private TextView txvJobSalary;
        private TextView txvJobDescription;
        private ImageView btnRemove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostOwnerAvatar = itemView.findViewById(R.id.imgPostOwnerAvatar);
            txvPostOwnerName = itemView.findViewById(R.id.txvPostOwnerName);
            txvJobName = itemView.findViewById(R.id.txvJobName);
            txvJobMajor = itemView.findViewById(R.id.txvJobMajor);
            txvJobAddress = itemView.findViewById(R.id.txvJobAddress);
            txvJobExperience = itemView.findViewById(R.id.txvJobExperience);
            txvJobSalary = itemView.findViewById(R.id.txvJobSalary);
            txvJobDescription = itemView.findViewById(R.id.txvJobDescription);
            btnRemove = itemView.findViewById(R.id.btnDelete);
        }
    }
}
