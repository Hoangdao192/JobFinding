package com.uet.fwork.post;

import android.content.Context;
import android.content.Intent;
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
import com.uet.fwork.ViewProfileActivityCandidate;
import com.uet.fwork.ViewProfileActivityEmployer;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostApplyModel;
import com.uet.fwork.database.model.post.PostApplyStatus;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.PostApplyRepository;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.dialog.ConfirmDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CandidatePostApplyRecyclerViewAdapter extends RecyclerView.Adapter<CandidatePostApplyRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<PostApplyModel> postApplyList;
    private Map<String, PostModel> postMap = new HashMap<>();
    private PostRepository postRepository;
    private PostApplyRepository postApplyRepository;
    private UserRepository userRepository;

    private List<PostApplyModel> unCheckApplicationList = new ArrayList<>();
    private List<PostApplyModel> acceptedApplicationList = new ArrayList<>();
    private List<PostApplyModel> rejectedApplicationList = new ArrayList<>();
    DecimalFormat format = new DecimalFormat("0.#");

    public CandidatePostApplyRecyclerViewAdapter(Context context, List<PostApplyModel> postApplyList) {
        this.context = context;
        this.postRepository = PostRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
        this.postApplyRepository = PostApplyRepository.getInstance();

        postApplyList.forEach(postApplyModel -> {
            switch (postApplyModel.getStatus()) {
                case PostApplyStatus.WAITING:
                    unCheckApplicationList.add(postApplyModel);
                    break;
                case PostApplyStatus.ACCEPTED:
                    acceptedApplicationList.add(postApplyModel);
                    break;
                case PostApplyStatus.REJECTED:
                    rejectedApplicationList.add(postApplyModel);
                    break;
            }
        });

        this.postApplyList = unCheckApplicationList;
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

        holder.btnRemove.setOnClickListener(v -> {
            ConfirmDialog confirmDialog = new ConfirmDialog(
                    context, "Hủy ứng tuyển",
                    "Bạn có chắc chắn muốn hủy ứng tuyển không",
                    new ConfirmDialog.OnEventListener() {
                        @Override
                        public void onConfirm() {
                            postApplyRepository.deletePostApplyByPostAndUser(
                                    postApply.getPostId(),
                                    postApply.getUserId(), success -> {
                                        if (success) {
                                            postApplyList.remove(holder.getBindingAdapterPosition());
                                            notifyItemRemoved(holder.getBindingAdapterPosition());
                                        }
                                    });
                        }

                        @Override
                        public void onCancel() {

                        }
                    }
            );
            confirmDialog.show();
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
        holder.txvJobExperience.setText("Kinh nghiệm: " + format.format(postModel.getPostExperience())+ " năm");
        holder.txvJobAddress.setText("Địa chỉ: " + postModel.getPostAddress());
        holder.txvJobName.setText("Tên công việc: " + postModel.getPostName());
        holder.txvJobMajor.setText("Chuyên ngành: " + postModel.getPostMajor());


    }

    public void displayUnReadApplication() {
        this.postApplyList = unCheckApplicationList;
        notifyDataSetChanged();
    }

    public void displayAcceptedApplication() {
        this.postApplyList = acceptedApplicationList;
        notifyDataSetChanged();
    }

    public void displayRejectedApplication() {
        this.postApplyList = rejectedApplicationList;
        notifyDataSetChanged();
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
        private ImageView btnRemove;
        private TextView txvStatus;
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
            txvStatus = itemView.findViewById(R.id.txvStatus);
        }
    }
}
