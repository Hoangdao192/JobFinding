package com.uet.fwork.database.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.post.CommentModel;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository extends Repository {
    private static final String LOG_TAG = "Comment repository";

    private final static String REFERENCE_PATH = "post/comments";

    public CommentRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, REFERENCE_PATH);
    }

    public void insert(CommentModel comment, @Nullable OnQuerySuccessListener<Boolean> listener) {
        if (comment.getPostId() == "") {
            if (listener != null) {
                listener.onSuccess(false);
            }
            return;
        }

        String key = rootDatabaseReference.child(comment.getPostId()).push().getKey();
        comment.setCommentId(key);
        rootDatabaseReference.child(comment.getPostId()).child(key).setValue(comment)
                .addOnSuccessListener(unused -> {
                    Log.d(LOG_TAG, "Upload comment successful " + comment.getCommentId());
                    if (listener != null) listener.onSuccess(true);
                })
                .addOnFailureListener(exception -> {
                    exception.printStackTrace();
                    Log.d(LOG_TAG, "Upload comment failed " + comment.getCommentId());
                    if (listener != null) listener.onSuccess(false);
                })
                .addOnCanceledListener(() -> {
                    Log.d(LOG_TAG, "Upload comment cancelled " + comment.getCommentId());
                    if (listener != null) listener.onSuccess(false);
                });
    }

    public void getAllCommentByPostId(String postId, OnQuerySuccessListener<List<CommentModel>> listener) {
        rootDatabaseReference.child(postId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    System.out.println(dataSnapshot.toString());
                    List<CommentModel> commentList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        System.out.println(snapshot);
                        commentList.add(snapshot.getValue(CommentModel.class));
                    }
                });
    }

    public void getNumberOfComment(String postId, @NonNull OnQuerySuccessListener<Long> listener) {
        rootDatabaseReference.child(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        listener.onSuccess(dataSnapshot.getChildrenCount());
                    }
                });
    }
}
