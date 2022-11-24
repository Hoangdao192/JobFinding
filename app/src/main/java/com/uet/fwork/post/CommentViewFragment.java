package com.uet.fwork.post;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.R;
import com.uet.fwork.database.model.post.CommentModel;
import com.uet.fwork.database.repository.CommentRepository;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.Repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommentViewFragment extends Fragment {

    private static final String LOG_TAG = "Comment fragment";

    private RecyclerView recCommentList;
    private EditText edtComment;
    private Button btnSend;
    private String postId;

    private CommentRecyclerViewAdapter commentAdapter;
    private List<CommentModel> commentList = new ArrayList<>();

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private FirebaseUser firebaseUser;

    public CommentViewFragment() {
        super(R.layout.fragment_comment_view);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postId = getArguments().getString("postId");
        if (postId == null) {
            Log.e(LOG_TAG, "Not receive argument postId");
        }

        postRepository = new PostRepository(FirebaseDatabase.getInstance());
        commentRepository = new CommentRepository(FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recCommentList = view.findViewById(R.id.recCommentList);
        edtComment = view.findViewById(R.id.edtComment);
        btnSend = view.findViewById(R.id.btnSend);

        commentAdapter = new CommentRecyclerViewAdapter(getContext(), commentList);
        recCommentList.setAdapter(commentAdapter);
        recCommentList.setLayoutManager(new LinearLayoutManager(getContext()));

        commentRepository.getAllCommentByPostId(postId, commentList -> {
            if (commentList != null) {
                commentAdapter.setCommentList(commentList);
            }
        });

        postRepository.getRootDatabaseReference().child(postId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                CommentModel comment = snapshot.getValue(CommentModel.class);
                commentList.add(comment);
                commentAdapter.notifyItemInserted(commentList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = edtComment.getText().toString();
                CommentModel commentModel = new CommentModel(
                        Calendar.getInstance().getTimeInMillis() / 1000,
                        comment, postId, firebaseUser.getUid(), ""
                );
                commentRepository.insert(commentModel, null);
            }
        });
    }
}
