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
import com.uet.fwork.database.model.post.ReactionModel;
import com.uet.fwork.database.repository.PostApplyRepository;
import com.uet.fwork.database.repository.PostReactionRepository;
import com.uet.fwork.database.repository.Repository;

import java.util.List;

public class ShowPostLikeFragment extends Fragment {

    private RecyclerView recPostApply;
    private PostReactionRepository postReactionRepository;
    private FirebaseUser firebaseUser;

    public ShowPostLikeFragment() {
        super(R.layout.fragment_show_like_post);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postReactionRepository = new PostReactionRepository(getContext(), FirebaseDatabase.getInstance());

        recPostApply = view.findViewById(R.id.recPostApply);

        postReactionRepository.getAllByUserId(firebaseUser.getUid(), postReactionList -> {
            PostLikeRecyclerViewAdapter adapter = new PostLikeRecyclerViewAdapter(
                    getContext(), postReactionList
            );
            recPostApply.setAdapter(adapter);
            recPostApply.setLayoutManager(new LinearLayoutManager(getContext()));
        });
    }
}
