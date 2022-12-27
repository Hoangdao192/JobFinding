package com.uet.fwork.post;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.ViewProfileActivityCandidate;
import com.uet.fwork.ViewProfileActivityEmployer;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostApplyModel;
import com.uet.fwork.database.model.post.PostApplyStatus;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.PostApplyRepository;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.dialog.ConfirmDialog;
import com.uet.fwork.firebasehelper.CloudMessagingHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployerPostApplyRecyclerViewAdapter extends RecyclerView.Adapter<EmployerPostApplyRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private Map<String, PostModel> postMap;
    private Map<String, CandidateModel> candidateMap = new HashMap<>();
    private PostRepository postRepository;
    private PostApplyRepository postApplyRepository;
    private UserRepository userRepository;
    private CloudMessagingHelper cloudMessagingHelper;

    private List<PostApplyModel> postApplyList = new ArrayList<>();
    private List<PostApplyModel> unReadPostApplyList;
    private List<PostApplyModel> acceptedPostApplyList;
    private List<PostApplyModel> rejectedPostApplyList;
    DecimalFormat format = new DecimalFormat("0.#");

    public EmployerPostApplyRecyclerViewAdapter(
            Context context, Map<String, PostModel> postMap,
            List<PostApplyModel> unReadPostApplyList,
            List<PostApplyModel> acceptedPostApplyList,
            List<PostApplyModel> rejectedPostApplyList) {
        this.context = context;
        this.postMap = postMap;
        this.postRepository = PostRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
        this.postApplyRepository = PostApplyRepository.getInstance();
        this.postApplyList = unReadPostApplyList;
        this.unReadPostApplyList = unReadPostApplyList;
        this.acceptedPostApplyList = acceptedPostApplyList;
        this.rejectedPostApplyList = rejectedPostApplyList;
        this.cloudMessagingHelper = CloudMessagingHelper.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recyclerview_job_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostApplyModel postApplyModel = postApplyList.get(position);
        if (!candidateMap.containsKey(postApplyModel.getUserId())) {
            userRepository.getUserByUID(postApplyModel.getUserId(), userModel -> {
                candidateMap.put(userModel.getId(), (CandidateModel) userModel);
                loadCandidateApplication(holder, postMap.get(postApplyModel.getPostId()), (CandidateModel) userModel);
            });
        } else {
            loadCandidateApplication(
                    holder,
                    postMap.get(postApplyModel.getPostId()),
                    candidateMap.get(postApplyModel.getUserId()));
        }
        holder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postApplyModel.getStatus().equals(PostApplyStatus.WAITING)) {
                    postApplyModel.setStatus(PostApplyStatus.REJECTED);
                    postApplyRepository.update(postApplyModel, result -> {
                        cloudMessagingHelper.sendPostApplicationRejectNotify(postApplyModel);
                        unReadPostApplyList.remove(postApplyModel);
                        rejectedPostApplyList.add(postApplyModel);
                        notifyItemRemoved(holder.getBindingAdapterPosition());
                    });
                }
            }
        });
        holder.btnShowCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRepository = UserRepository.getInstance();
                userRepository.getUserRole(postApplyModel.getUserId(), g -> {
                    if(g.equals("Candidate")) {
                        Intent intent = new Intent(context, ViewProfileActivityCandidate.class);
                        intent.putExtra("id",postApplyModel.getUserId());
                        context.startActivity(intent);
                    } else if (g.equals("Employer")) {
                        Intent intent = new Intent(context, ViewProfileActivityEmployer.class);
                        intent.putExtra("id",postApplyModel.getUserId());
                        context.startActivity(intent);
                    }
                });


            }
        });
        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postApplyModel.setStatus(PostApplyStatus.ACCEPTED);
                postApplyRepository.update(postApplyModel, result -> {
                    cloudMessagingHelper.sendPostApplicationAcceptNotify(postApplyModel);
                    unReadPostApplyList.remove(postApplyModel);
                    acceptedPostApplyList.add(postApplyModel);
                    notifyItemRemoved(holder.getBindingAdapterPosition());
                });

            }
        });

        if (!postApplyModel.getStatus().equals(PostApplyStatus.WAITING)) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        } else {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        }
    }

    private void loadCandidateApplication(
            ViewHolder holder, PostModel postModel, CandidateModel candidateModel) {
        if (!candidateModel.getAvatar().equals("")) {
            Picasso.get().load(candidateModel.getAvatar()).into(holder.cirImgAvatar);
        }
        holder.txvFullName.setText(candidateModel.getFullName());
        holder.txvExperience.setText(format.format(candidateModel.getYearOfExperience()) + " năm");
        holder.txvMajor.setText(candidateModel.getMajor());
        holder.txvPhoneNumber.setText(candidateModel.getPhoneNumber());
        holder.txvEmail.setText(candidateModel.getEmail());
        holder.txvJobName.setText(postModel.getPostName());
    }

    @Override
    public int getItemCount() {
        return postApplyList.size();
    }

    public void setPostApplyList(List<PostApplyModel> postApplyList) {
        this.postApplyList = postApplyList;
        notifyDataSetChanged();
    }

    public void displayUnReadApplication() {
        this.postApplyList = unReadPostApplyList;
        notifyDataSetChanged();
    }

    public void displayAcceptedApplication() {
        this.postApplyList = acceptedPostApplyList;
        notifyDataSetChanged();
    }

    public void displayRejectedApplication() {
        this.postApplyList = rejectedPostApplyList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView cirImgAvatar;
        private TextView txvFullName;
        private TextView txvExperience;
        private TextView txvMajor;
        private TextView txvPhoneNumber;
        private TextView txvEmail;
        private TextView txvJobName;
        private Button btnShowCandidate, btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cirImgAvatar = itemView.findViewById(R.id.cirImgAvatar);
            txvFullName = itemView.findViewById(R.id.txvFullName);
            txvExperience = itemView.findViewById(R.id.txvExperience);
            txvMajor = itemView.findViewById(R.id.txvMajor);
            txvPhoneNumber = itemView.findViewById(R.id.txvPhoneNumber);
            txvEmail = itemView.findViewById(R.id.txvEmail);
            txvJobName = itemView.findViewById(R.id.txvJobName);
            btnShowCandidate = itemView.findViewById(R.id.btnShowCandidate);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
