package com.uet.fwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.post.PostsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    RecyclerView postsRecyclerView;

    List<PostModel> postModelList;
    PostsAdapter postsAdapter;
    String uid ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_profile);

        postsRecyclerView = findViewById(R.id.recyclerviewPosts);
        firebaseAuth = FirebaseAuth.getInstance();

        //get uid of the clicked user
        Intent intent = getIntent();
        uid = intent.getStringExtra("id");

        postModelList = new ArrayList<>();
        loadPosts();
    }

    private void loadPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first (load data from last)
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts/list");
        Query query = databaseReference.orderByChild("userId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postModelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                   PostModel myPosts = ds.getValue(PostModel.class);

                   //add to list
                    postModelList.add(myPosts);

                    //adapter
                    postsAdapter = new PostsAdapter(ViewProfileActivity.this, postModelList);
                    postsRecyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileActivity.this, ""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}