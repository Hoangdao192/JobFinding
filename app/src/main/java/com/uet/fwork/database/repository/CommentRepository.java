package com.uet.fwork.database.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.uet.fwork.Constants;
import com.uet.fwork.database.model.post.CommentModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentRepository extends Repository {
    private static final String LOG_TAG = "Comment repository";
    private final static String REFERENCE_PATH = "posts/comments";

    private static CommentRepository INSTANCE = null;


    private CommentRepository() {
        super(REFERENCE_PATH);
    }

    public static CommentRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new CommentRepository();
        }

        return INSTANCE;
    }

    public void insert(CommentModel comment, @Nullable OnQuerySuccessListener<Boolean> listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
        String apiUrl = Constants.SERVER_URL + "post/comment/notify";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl, response -> {
            response = new String(
                    response.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
            Log.d(LOG_TAG, "Volley: Request response " + response);
        }, error -> {
            error.printStackTrace();
            Log.d(LOG_TAG, "Volley: Send request failed " + apiUrl);
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userId", comment.getUserId());
                params.put("postId", comment.getPostId());
                return params;
            }
        };
        Log.d(LOG_TAG, "Volley: Add request to queue");
        requestQueue.add(stringRequest);

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

    public void deleteCommentByPostId(String postId) {
        rootDatabaseReference.child(postId).removeValue();
    }
}
