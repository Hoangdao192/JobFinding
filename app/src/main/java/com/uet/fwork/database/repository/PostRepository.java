package com.uet.fwork.database.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.database.model.post.PostModel;

public class PostRepository extends Repository {
    private static final String LOG_TAG = "Post repository";

    private final static String databasePath = "posts";

    public PostRepository(FirebaseDatabase firebaseDatabase, FirebaseFirestore firebaseFirestore) {
        super(firebaseDatabase, firebaseFirestore, databasePath);
    }

    public PostRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, databasePath);
    }

    public void insert(PostModel postModel, @Nullable OnQuerySuccessListener<Boolean> listener) {
        String postId = rootDatabaseReference.push().getKey();
        postModel.setPostId(postId);
        rootDatabaseReference.child("list").child(postId).setValue(postModel)
                .addOnFailureListener(exception -> {
                    Log.d(LOG_TAG, "Insert post failed " + postModel.toString());
                    exception.printStackTrace();
                    if (listener != null) {
                        listener.onSuccess(false);
                    }
                })
                .addOnSuccessListener(unused -> {
                    Log.d(LOG_TAG, "Insert post successful " + postModel.toString());
                    if (listener != null) {
                        listener.onSuccess(true);
                    }
                })
                .addOnCanceledListener(() ->
                        Log.d(LOG_TAG, "Insert post cancelled " + postModel.toString()));
    }

    /**
     * Method này sẽ thay thế toàn bộ dữ liệu của postModel cũ thành postModel mới
     */
    public void update(PostModel postModel, @Nullable OnQuerySuccessListener<Boolean> listener) {
        if (!postModel.getPostId().isEmpty()) {
            rootDatabaseReference.child(postModel.getPostId()).setValue(postModel)
                    .addOnSuccessListener(unused -> {
                        if (listener != null) {
                            listener.onSuccess(true);
                        }
                    })
                    .addOnFailureListener(exception -> {
                        exception.printStackTrace();
                        if (listener != null) {
                            listener.onSuccess(false);
                        }
                    });
        }
    }
}
