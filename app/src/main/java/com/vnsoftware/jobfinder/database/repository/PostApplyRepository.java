package com.vnsoftware.jobfinder.database.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.vnsoftware.jobfinder.Constants;
import com.vnsoftware.jobfinder.database.model.post.PostApplyModel;
import com.vnsoftware.jobfinder.database.model.post.PostModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostApplyRepository extends Repository {
    private static final String REFERENCE_PATH = "posts/apply";
    private static final String LOG_TAG = "Post apply repository";
    private static PostApplyRepository INSTANCE = null;

    private PostApplyRepository() {
        super(REFERENCE_PATH);
    }

    public static PostApplyRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new PostApplyRepository();
        }

        return INSTANCE;
    }

    public void insert(
            PostApplyModel postApplyModel,
            @Nullable OnQuerySuccessListener<PostApplyModel> listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
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

    public void update(
            PostApplyModel postApplyModel,
            @Nullable OnQuerySuccessListener<PostApplyModel> listener) {
        isPostApplyExists(postApplyModel, new OnQuerySuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    rootDatabaseReference
                            .child(postApplyModel.getPostId())
                            .child(postApplyModel.getApplyId())
                            .setValue(postApplyModel)
                            .addOnSuccessListener(unused -> {
                                Log.d(LOG_TAG, "Update post apply model success " + postApplyModel.toString());
                                DatabaseReference databaseReference = firebaseDatabase.getReference("/posts/userApply");
                                databaseReference.child(postApplyModel.getUserId())
                                        .child(postApplyModel.getApplyId()).setValue(postApplyModel);

                                if (listener != null) {
                                    listener.onSuccess(postApplyModel);
                                }
                            })
                            .addOnFailureListener(exception -> {
                                Log.d(LOG_TAG, "Update post apply model failed " + postApplyModel.toString());
                                if (listener != null) {
                                    listener.onSuccess(null);
                                }
                            })
                            .addOnCanceledListener(() -> {
                                Log.d(LOG_TAG, "Update post apply model cancelled " + postApplyModel.toString());
                                if (listener != null) {
                                    listener.onSuccess(null);
                                }
                            });
                }
            }
        });
    }

    public void isPostApplyExists(
            PostApplyModel postApplyModel, @NonNull OnQuerySuccessListener<Boolean> listener) {
        rootDatabaseReference.child(postApplyModel.getPostId()).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            listener.onSuccess(true);
                        } else {
                            listener.onSuccess(false);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    listener.onSuccess(false);
                })
                .addOnCanceledListener(() -> {
                    listener.onSuccess(false);
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

    public void deletePostApplyByPostAndUser(
            String postId, String userId, @Nullable OnQuerySuccessListener<Boolean> listener) {
        rootDatabaseReference.child(postId)
                .orderByChild("userId").equalTo(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
                            rootDatabaseReference.child(postId).child(dataSnapshot1.getKey()).removeValue()
                                    .addOnSuccessListener(unused -> {
                                        Log.d(LOG_TAG,
                                                "Remove snapshot posts/apply/"
                                                        + dataSnapshot1.getKey()
                                                        + " successful");
                                        firebaseDatabase.getReference("posts/userApply")
                                                .child(userId).child(dataSnapshot1.getKey())
                                                .removeValue()
                                                .addOnSuccessListener(unused1 -> {
                                                    Log.d(LOG_TAG,
                                                            "Remove snapshot posts/userApply/"
                                                                    + userId + " "
                                                                    + dataSnapshot1.getKey()
                                                                    + " successful");
                                                    if (listener != null) {
                                                        listener.onSuccess(true);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    e.printStackTrace();
                                                    Log.d(LOG_TAG,
                                                            "Remove snapshot posts/userApply/"
                                                                    + userId + " "
                                                                    + dataSnapshot1.getKey()
                                                                    + " failed");
                                                    if (listener != null) {
                                                        listener.onSuccess(false);
                                                    }
                                                })
                                                .addOnCanceledListener(() -> {
                                                    Log.d(LOG_TAG,
                                                            "Remove snapshot posts/userApply/"
                                                                    + userId + " "
                                                                    + dataSnapshot1.getKey()
                                                                    + " cancelled");
                                                    if (listener != null) {
                                                        listener.onSuccess(false);
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        e.printStackTrace();
                                        Log.d(LOG_TAG, "Remove snapshot posts/apply/"
                                                + dataSnapshot1.getKey()
                                                + " failed");
                                        if (listener != null) {
                                            listener.onSuccess(false);
                                        }
                                    })
                                    .addOnCanceledListener(() -> {
                                        Log.d(LOG_TAG, "Remove snapshot posts/apply/"
                                                + dataSnapshot1.getKey()
                                                + " cancelled");
                                        if (listener != null) {
                                            listener.onSuccess(false);
                                        }
                                    });
                        });
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    public void deletePostApply(PostApplyModel postApplyModel) {
        String userId = postApplyModel.getUserId();
        rootDatabaseReference.child(postApplyModel.getPostId())
                .orderByChild("userId").equalTo(postApplyModel.getUserId()).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
                            rootDatabaseReference.child(postApplyModel.getPostId()).child(dataSnapshot1.getKey()).removeValue()
                                    .addOnSuccessListener(unused -> {
                                        Log.d(LOG_TAG,
                                                "Remove snapshot posts/apply/"
                                                        + dataSnapshot1.getKey()
                                                        + " successful");
                                        firebaseDatabase.getReference("posts/userApply")
                                                .child(postApplyModel.getUserId()).child(dataSnapshot1.getKey())
                                                .removeValue()
                                                .addOnSuccessListener(unused1 -> {
                                                    Log.d(LOG_TAG,
                                                            "Remove snapshot posts/userApply/"
                                                                    + userId + " "
                                                                    + dataSnapshot1.getKey()
                                                                    + " successful");
                                                })
                                                .addOnFailureListener(e -> {
                                                    e.printStackTrace();
                                                    Log.d(LOG_TAG,
                                                            "Remove snapshot posts/userApply/"
                                                                    + userId + " "
                                                                    + dataSnapshot1.getKey()
                                                                    + " failed");
                                                })
                                                .addOnCanceledListener(() -> {
                                                    Log.d(LOG_TAG,
                                                            "Remove snapshot posts/userApply/"
                                                                    + userId + " "
                                                                    + dataSnapshot1.getKey()
                                                                    + " cancelled");
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        e.printStackTrace();
                                        Log.d(LOG_TAG, "Remove snapshot posts/apply/"
                                                + dataSnapshot1.getKey()
                                                + " failed");
                                    })
                                    .addOnCanceledListener(() -> {
                                        Log.d(LOG_TAG, "Remove snapshot posts/apply/"
                                                + dataSnapshot1.getKey()
                                                + " cancelled");
                                    });
                        });
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    public void deletePostApplyByPostId(String postId) {
        rootDatabaseReference.child(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        List<PostApplyModel> postApplyModels  = new ArrayList<>();
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            postApplyModels.add(snapshot.getValue(PostApplyModel.class));
                        });
                        postApplyModels.forEach(postApplyModel -> {
                            deletePostApply(postApplyModel);
                        });
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    public void getAllPostApplyByPostOwner(
            String postOwnerId, OnQuerySuccessListener<List<PostApplyModel>> listener) {

    }

    public void getAllByPost(
            PostModel postModel, @NonNull OnQuerySuccessListener<List<PostApplyModel>> listener) {
        rootDatabaseReference.child(postModel.getPostId()).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        List<PostApplyModel> postApplyList = new ArrayList<>();
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            postApplyList.add(snapshot.getValue(PostApplyModel.class));
                        });
                        listener.onSuccess(postApplyList);
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
}
