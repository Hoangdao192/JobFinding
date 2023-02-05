package com.vnsoftware.jobfinder.post;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.database.repository.PostRepository;

public class ShowMyPostFragment extends Fragment {

    private RecyclerView recPostApply;
    private PostRepository postRepository;
    private FirebaseUser firebaseUser;

    public ShowMyPostFragment() {
        super(R.layout.fragment_show_my_post);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postRepository = PostRepository.getInstance();

        recPostApply = view.findViewById(R.id.recPostApply);

        postRepository.getAllByUserId(firebaseUser.getUid(), postList -> {
            MyPostRecyclerViewAdapter adapter = new MyPostRecyclerViewAdapter(
                    getContext(), postList
            );
            recPostApply.setAdapter(adapter);
            recPostApply.setLayoutManager(new LinearLayoutManager(getContext()));
        });
    }
}
