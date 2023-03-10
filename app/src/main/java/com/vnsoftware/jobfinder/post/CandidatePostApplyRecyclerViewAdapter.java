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
import com.vnsoftware.jobfinder.database.model.post.PostApplyModel;
import com.vnsoftware.jobfinder.database.model.post.PostApplyStatus;
import com.vnsoftware.jobfinder.database.model.post.PostModel;
import com.vnsoftware.jobfinder.database.repository.PostApplyRepository;
import com.vnsoftware.jobfinder.database.repository.PostRepository;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.dialog.ConfirmDialog;

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
                    context, "H???y ???ng tuy???n",
                    "B???n c?? ch???c ch???n mu???n h???y ???ng tuy???n kh??ng",
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
        holder.txvJobDescription.setText("M?? t??? c??ng vi???c: " + postModel.getPostDescription());
        holder.txvJobSalary.setText("M???c l????ng t???i thi???u: " + postModel.getPostSalary() + " tri???u ?????ng");
        holder.txvJobExperience.setText("Kinh nghi???m: " + format.format(postModel.getPostExperience())+ " n??m");
        holder.txvJobAddress.setText("?????a ch???: " + postModel.getPostAddress());
        holder.txvJobName.setText("T??n c??ng vi???c: " + postModel.getPostName());
        holder.txvJobMajor.setText("Chuy??n ng??nh: " + postModel.getPostMajor());


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
