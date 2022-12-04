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
import com.uet.fwork.database.repository.PostApplyRepository;
import com.uet.fwork.database.repository.Repository;

import java.util.List;

public class CandidateShowPostApplyFragment extends Fragment {

    private RecyclerView recPostApply;
    private PostApplyRepository postApplyRepository;
    private FirebaseUser firebaseUser;

    public CandidateShowPostApplyFragment() {
        super(R.layout.fragment_my_apply);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postApplyRepository = new PostApplyRepository(getContext(), FirebaseDatabase.getInstance());

        recPostApply = view.findViewById(R.id.recPostApply);

        postApplyRepository.getAllPostApplyByUserId(firebaseUser.getUid(), postApplyList -> {
            CandidatePostApplyRecyclerViewAdapter adapter = new CandidatePostApplyRecyclerViewAdapter(
                    getContext(), postApplyList
            );
            recPostApply.setAdapter(adapter);
            recPostApply.setLayoutManager(new LinearLayoutManager(getContext()));
        });
    }
}
