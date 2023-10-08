package com.uet.fwork.post;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uet.fwork.R;
import com.uet.fwork.database.repository.PostApplyRepository;

public class CandidateShowPostApplyFragment extends Fragment {

    private RecyclerView recPostApply;
    private PostApplyRepository postApplyRepository;
    private FirebaseUser firebaseUser;
    private RadioGroup radGrpApplication;
    private  CandidatePostApplyRecyclerViewAdapter adapter;

    public CandidateShowPostApplyFragment() {
        super(R.layout.fragment_my_apply);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postApplyRepository = PostApplyRepository.getInstance();

        recPostApply = view.findViewById(R.id.recPostApply);
        radGrpApplication = view.findViewById(R.id.radGrpApplication);

        postApplyRepository.getAllPostApplyByUserId(firebaseUser.getUid(), postApplyList -> {
            adapter = new CandidatePostApplyRecyclerViewAdapter(
                    getContext(), postApplyList
            );
            recPostApply.setAdapter(adapter);
            recPostApply.setLayoutManager(new LinearLayoutManager(getContext()));

            radGrpApplication.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.radNotRead:
                            adapter.displayUnReadApplication();
                            break;
                        case R.id.radAccepted:
                            adapter.displayAcceptedApplication();
                            break;
                        case R.id.radRejected:
                            adapter.displayRejectedApplication();
                            break;
                    }
                }
            });
        });
    }
}
