package com.uet.fwork.post;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.R;
import com.uet.fwork.database.model.post.PostApplyModel;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.PostApplyRepository;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployerShowPostApplyFragment extends Fragment {

    private RecyclerView recPostApply;
    private PostApplyRepository postApplyRepository;
    private PostRepository postRepository;
    private FirebaseUser firebaseUser;
    private List<PostApplyModel> postApplyModels = new ArrayList<>();

    public EmployerShowPostApplyFragment() {
        super(R.layout.fragment_show_application);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postRepository = new PostRepository(getContext(), FirebaseDatabase.getInstance());
        postApplyRepository = new PostApplyRepository(getContext(), FirebaseDatabase.getInstance());

        recPostApply = view.findViewById(R.id.recPostApply);


        postRepository.getAllByUserId(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<List<PostModel>>() {
            @Override
            public void onSuccess(List<PostModel> postModelList) {
                Map<String, PostModel> postMap = new HashMap<>();
                postModelList.forEach(post -> {
                    postMap.put(post.getPostId(), post);
                });
                EmployerPostApplyRecyclerViewAdapter adapter = new EmployerPostApplyRecyclerViewAdapter(
                    getContext(), postApplyModels, postMap
                );
                recPostApply.setLayoutManager(new LinearLayoutManager(getContext()));
                recPostApply.setAdapter(adapter);

                postModelList.forEach(post -> {
                    postApplyRepository.getAllByPost(post, new Repository.OnQuerySuccessListener<List<PostApplyModel>>() {
                        @Override
                        public void onSuccess(List<PostApplyModel> result) {
                            int oldSize = postApplyModels.size();
                            postApplyModels.addAll(result);
                            adapter.notifyItemRangeInserted(oldSize, result.size());
                        }
                    });
                });
            }
        });

    }
}
