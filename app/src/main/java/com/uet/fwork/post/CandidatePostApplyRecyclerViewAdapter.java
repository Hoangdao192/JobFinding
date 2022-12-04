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

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostApplyModel;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CandidatePostApplyRecyclerViewAdapter extends RecyclerView.Adapter<CandidatePostApplyRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<PostApplyModel> postApplyList;
    private Map<String, PostModel> postMap = new HashMap<>();
    private PostRepository postRepository;
    private UserRepository userRepository;

    public CandidatePostApplyRecyclerViewAdapter(Context context, List<PostApplyModel> postApplyList) {
        this.context = context;
        this.postApplyList = postApplyList;
        this.postRepository = new PostRepository(FirebaseDatabase.getInstance());
        this.userRepository = new UserRepository(FirebaseDatabase.getInstance());
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
        PostApplyModel postApply = postApplyList.get(position);
        if (!postMap.containsKey(postApply.getPostId())) {
            postRepository.getById(postApply.getPostId(), post -> {
                postMap.put(postApply.getPostId(), post);
                loadPostDataIntoHolder(post, holder);
            });
        } else {
            loadPostDataIntoHolder(postMap.get(postApply.getPostId()), holder);
        }
    }

    private void loadPostDataIntoHolder(PostModel postModel, ViewHolder holder) {
        userRepository.getUserByUID(postModel.getUserId(), user -> {
            if (!user.getAvatar().equals("")) {
                Picasso.get().load(user.getAvatar()).into(holder.imgPostOwnerAvatar);
            }
            holder.txvPostOwnerName.setText(user.getFullName());
        });
        holder.txvJobDescription.setText("Mô tả công việc: " + postModel.getPostDescription());
        holder.txvJobSalary.setText("Mức lương tối thiểu: " + postModel.getPostSalary());
        holder.txvJobExperience.setText("Kinh nghiệm: " + postModel.getPostExperience() + " năm");
        holder.txvJobAddress.setText("Địa chỉ: " + postModel.getPostAddress());
        holder.txvJobName.setText("Tên công việc: " + postModel.getPostName());
        holder.txvJobMajor.setText("Chuyên ngành: " + postModel.getPostMajor());
    }

    @Override
    public int getItemCount() {
        return postApplyList.size();
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
        }
    }
}
