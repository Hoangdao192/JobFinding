package com.uet.fwork.database.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.Constants;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostApplyModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostApplyRepository extends Repository {
    private static final String REFERENCE_PATH = "posts/apply";
    private static final String LOG_TAG = "Post apply repository";
    private Context context;

    public PostApplyRepository(Context context, FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, REFERENCE_PATH);
        this.context = context;
    }

    public void insert(
            PostApplyModel postApplyModel,
            @Nullable OnQuerySuccessListener<PostApplyModel> listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String apiUrl = Constants.SERVER_URL + "post/apply/notify";
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
                params.put("userId", postApplyModel.getUserId());
                params.put("postId", postApplyModel.getPostId());
                return params;
            }
        };

        isUserApplyPost(postApplyModel.getPostId(), postApplyModel.getUserId(), new OnQuerySuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    String key = rootDatabaseReference.child(postApplyModel.getPostId()).push().getKey();
                    postApplyModel.setApplyId(key);
                    Log.d(LOG_TAG, "Insert new post apply model " + postApplyModel.toString());
                    rootDatabaseReference.child(postApplyModel.getPostId()).child(key).setValue(postApplyModel)
                            .addOnSuccessListener(unused -> {
                                Log.d(LOG_TAG, "Insert new post apply model success " + postApplyModel.toString());
                                DatabaseReference databaseReference = firebaseDatabase.getReference("/posts/userApply");
                                databaseReference.child(postApplyModel.getUserId())
                                        .child(postApplyModel.getApplyId()).setValue(postApplyModel);

                                Log.d(LOG_TAG, "Volley: Add request to queue");
                                requestQueue.add(stringRequest);
                                if (listener != null) {
                                    listener.onSuccess(postApplyModel);
                                }
                            })
                            .addOnFailureListener(exception -> {
                                Log.d(LOG_TAG, "Insert new post apply model failed " + postApplyModel.toString());
                                if (listener != null) {
                                    listener.onSuccess(null);
                                }
                            })
                            .addOnCanceledListener(() -> {
                                Log.d(LOG_TAG, "Insert new post apply model cancelled " + postApplyModel.toString());
                                if (listener != null) {
                                    listener.onSuccess(null);
                                }
                            });
                } else  {
                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                }
            }
        });
    }

    public void isUserApplyPost(String postId, String userId, OnQuerySuccessListener<Boolean> listener) {
        DatabaseReference databaseReference = firebaseDatabase.getReference("/posts/userApply");
        databaseReference.child(userId).orderByChild("postId").equalTo(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d(LOG_TAG, "User has apply post " + postId + " " + userId);
                            listener.onSuccess(true);
                        } else {
                            Log.d(LOG_TAG, "User not apply post " + postId + " " + userId);
                            listener.onSuccess(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void getAllPostApplyByUserId(
            String userId, @NonNull OnQuerySuccessListener<List<PostApplyModel>> listener) {
        firebaseDatabase.getReference("/posts/userApply").child(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        List<PostApplyModel> postApplyModels = new ArrayList<>();
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            postApplyModels.add(snapshot.getValue(PostApplyModel.class));
                        });
                        listener.onSuccess(postApplyModels);
                    }
                });
    }
}
