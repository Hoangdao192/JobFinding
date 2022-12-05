package com.uet.fwork.post;

import android.content.Context;
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
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostApplyModel;
import com.uet.fwork.database.model.post.PostApplyStatus;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.PostApplyRepository;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.dialog.ConfirmDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployerPostApplyRecyclerViewAdapter extends RecyclerView.Adapter<EmployerPostApplyRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<PostApplyModel> postApplyList;
    private Map<String, PostModel> postMap;
    private Map<String, CandidateModel> candidateMap = new HashMap<>();
    private PostRepository postRepository;
    private PostApplyRepository postApplyRepository;
    private UserRepository userRepository;

    public EmployerPostApplyRecyclerViewAdapter(
            Context context, List<PostApplyModel> postApplyList, Map<String, PostModel> postMap) {
        this.context = context;
        this.postMap = postMap;
        this.postApplyList = postApplyList;
        this.postRepository = new PostRepository(context, FirebaseDatabase.getInstance());
        this.userRepository = new UserRepository(FirebaseDatabase.getInstance());
        this.postApplyRepository = new PostApplyRepository(context, FirebaseDatabase.getInstance());
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
                postApplyModel.setStatus(PostApplyStatus.REJECTED);
                postApplyRepository.update(postApplyModel, null);
            }
        });
        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postApplyModel.setStatus(PostApplyStatus.ACCEPTED);
                postApplyRepository.update(postApplyModel, null);
            }
        });
    }

    private void loadCandidateApplication(
            ViewHolder holder, PostModel postModel, CandidateModel candidateModel) {
        if (!candidateModel.getAvatar().equals("")) {
            Picasso.get().load(candidateModel.getAvatar()).into(holder.cirImgAvatar);
        }
        holder.txvFullName.setText(candidateModel.getFullName());
        holder.txvExperience.setText(candidateModel.getYearOfExperience() + " năm");
        holder.txvMajor.setText(candidateModel.getMajor());
        holder.txvPhoneNumber.setText(candidateModel.getPhoneNumber());
        holder.txvEmail.setText(candidateModel.getEmail());
        holder.txvJobName.setText(postModel.getPostName());
    }

    @Override
    public int getItemCount() {
        return postApplyList.size();
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
