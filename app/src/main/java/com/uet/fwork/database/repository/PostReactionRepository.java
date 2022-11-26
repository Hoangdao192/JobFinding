package com.uet.fwork.database.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.model.post.ReactionModel;

public class PostReactionRepository extends Repository {
    private static final String REFERENCE_PATH = "posts/reactions";
    private static final String LOG_TAG = "PostReaction repository";

    public PostReactionRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, REFERENCE_PATH);
    }

    public void insert(ReactionModel reactionModel, @Nullable OnQuerySuccessListener<Boolean> listener) {
        String reactionId = rootDatabaseReference.child(reactionModel.getPostId()).push().getKey();
        reactionModel.setReactionId(reactionId);
        rootDatabaseReference.child(reactionModel.getPostId()).child(reactionId).setValue(reactionModel)
                .addOnFailureListener(exception -> {
                    Log.d(LOG_TAG, "Insert reaction failed " + reactionModel.toString());
                    exception.printStackTrace();
                    if (listener != null) {
                        listener.onSuccess(false);
                    }
                })
                .addOnSuccessListener(unused -> {
                    Log.d(LOG_TAG, "Insert reaction successful " + reactionModel.toString());
                    if (listener != null) {
                        listener.onSuccess(true);
                    }
                })
                .addOnCanceledListener(() ->
                        Log.d(LOG_TAG, "Insert reaction cancelled " + reactionModel.toString()));
    }

    public void removeReactionByPostAndUser(String postId, String userId) {
        rootDatabaseReference.child(postId).orderByChild("userId").equalTo(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            System.out.println(dataSnapshot);
                            dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
                                rootDatabaseReference.child(postId).child(dataSnapshot1.getKey()).removeValue();
                            });
                            String key = dataSnapshot.getKey();
                        }
                    }
                }).addOnFailureListener(e -> e.printStackTrace());
    }

    public void getNumberOfReaction(String postId, @NonNull OnQuerySuccessListener<Long> listener) {
        rootDatabaseReference.child(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        listener.onSuccess(dataSnapshot.getChildrenCount());
                    }
                });
    }

    public void isUserLikePost(String postId, String userId, @NonNull OnQuerySuccessListener<Boolean> listener) {
        rootDatabaseReference.child(postId).orderByChild("userId").equalTo(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        listener.onSuccess(dataSnapshot.exists());
                    }
                }).addOnFailureListener(e -> e.printStackTrace());
    }
}
