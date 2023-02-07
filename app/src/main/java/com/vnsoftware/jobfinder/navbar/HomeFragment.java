package com.vnsoftware.jobfinder.navbar;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.vnsoftware.jobfinder.JobSearchFragment;
import com.vnsoftware.jobfinder.database.repository.PostRepository;
import com.vnsoftware.jobfinder.notification.NotificationActivity;
import com.vnsoftware.jobfinder.post.AddPostActivity;
import com.vnsoftware.jobfinder.database.model.post.PostModel;
import com.vnsoftware.jobfinder.post.PostsAdapter;
import com.vnsoftware.jobfinder.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageView imgNotification;

    RecyclerView recyclerView;
    List<PostModel> postModelList;
    PostsAdapter postsAdapter;

    private PostRepository postRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postRepository = PostRepository.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //recycler view and properties
        recyclerView = view.findViewById(R.id.postsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postModelList = new ArrayList<>();
        loadPosts();

        return view;
    }

    private void loadPosts() {
        //path of all posts
        DatabaseReference ref = postRepository.getRootDatabaseReference();
        //get all data from reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postModelList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PostModel postModel = ds.getValue(PostModel.class);

                    postModelList.add(postModel);

                    //adapter
                    postsAdapter = new PostsAdapter(getActivity(), postModelList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(String searchQuery) {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgNotification = view.findViewById(R.id.imgNotification);
        imgNotification.setOnClickListener(imgView -> {
            startActivity(new Intent(getContext(), NotificationActivity.class));
        });
    }

}