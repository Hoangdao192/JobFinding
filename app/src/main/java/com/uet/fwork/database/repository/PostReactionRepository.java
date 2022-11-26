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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.Constants;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.model.post.ReactionModel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PostReactionRepository extends Repository {
    private static final String REFERENCE_PATH = "postReactions";
    private static final String LOG_TAG = "PostReaction repository";

    private Context context;

    public PostReactionRepository(Context context, FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, REFERENCE_PATH);
        this.context = context;
    }

    public void insert(ReactionModel reactionModel, @Nullable OnQuerySuccessListener<Boolean> listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String apiUrl = Constants.SERVER_URL + "post/reaction/notify";
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
                params.put("userId", reactionModel.getUserId());
                params.put("postId", reactionModel.getPostId());
                return params;
            }
        };
        Log.d(LOG_TAG, "Volley: Add request to queue");
        requestQueue.add(stringRequest);

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
