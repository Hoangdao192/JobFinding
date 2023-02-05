package com.vnsoftware.jobfinder.database.repository;

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
import com.google.firebase.database.DatabaseReference;
import com.vnsoftware.jobfinder.Constants;
import com.vnsoftware.jobfinder.database.model.post.ReactionModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostReactionRepository extends Repository {
    private static final String REFERENCE_PATH = "posts/reactions";
    private static final String USER_REACTION_PATH = "/posts/userReactions";
    private static final String LOG_TAG = "PostReaction repository";
    private static PostReactionRepository INSTANCE = null;

    private PostReactionRepository() {
        super(REFERENCE_PATH);
    }

    public static PostReactionRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new PostReactionRepository();
        }

        return INSTANCE;
    }

    public QueryTask<Boolean> insert(ReactionModel reaction) {
        return new QueryTask<Boolean>() {
            @Override
            public void execute() {
                DatabaseReference newReference = rootDatabaseReference
                        .child(reaction.getPostId()).push();

                reaction.setReactionId(newReference.getKey());
                newReference.setValue(reaction)
                        .addOnFailureListener(exception -> {
                            Log.d(LOG_TAG, "Insert reaction failed " + reaction);
                            exception.printStackTrace();
                            onFailed(exception);
                        })
                        .addOnSuccessListener(unused -> {
                            firebaseDatabase.getReference(USER_REACTION_PATH)
                                    .child(reaction.getUserId())
                                    .child(reaction.getReactionId())
                                    .setValue(reaction)
                                    .addOnSuccessListener(unused1 -> {
                                        Log.d(LOG_TAG, "Insert reaction successful " + reaction);
                                        onSuccess(true);
                                    })
                                    .addOnFailureListener(exception -> {
                                        exception.printStackTrace();
                                        Log.d(LOG_TAG, "Insert reaction failed " + reaction);
                                        onFailed(exception);
                                    })
                                    .addOnCanceledListener(() -> {
                                        Log.d(LOG_TAG, "Insert reaction cancelled " + reaction);
                                        onCancelled();
                                    });
                        })
                        .addOnCanceledListener(() -> {
                            Log.d(LOG_TAG, "Insert reaction cancelled " + reaction);
                            onCancelled();
                        });

            }
        };
    }

    public void insert(ReactionModel reactionModel, @Nullable OnQuerySuccessListener<Boolean> listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
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
                    firebaseDatabase.getReference(USER_REACTION_PATH)
                            .child(reactionModel.getUserId())
                            .child(reactionModel.getReactionId())
                            .setValue(reactionModel)
                            .addOnSuccessListener(unused1 -> {
                                Log.d(LOG_TAG, "Insert reaction successful " + reactionModel.toString());
                                if (listener != null) {
                                    listener.onSuccess(true);
                                }
                            })
                            .addOnFailureListener(exception -> {
                                exception.printStackTrace();
                                Log.d(LOG_TAG, "Insert reaction failed " + reactionModel.toString());
                            })
                            .addOnCanceledListener(() -> {
                                Log.d(LOG_TAG, "Insert reaction cancelled " + reactionModel.toString());
                            });
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
                            dataSnapshot.getChildren().forEach(dataSnapshot1 -> {
                                rootDatabaseReference.child(postId).child(dataSnapshot1.getKey()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                firebaseDatabase.getReference(USER_REACTION_PATH)
                                                        .child(userId).child(dataSnapshot1.getKey())
                                                        .removeValue()
                                                        .addOnFailureListener(e -> e.printStackTrace());
                                            }
                                        });
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

    public QueryTask<Boolean> isUserReactionPost(String postId, String userId) {
        return new QueryTask<Boolean>() {
            @Override
            public void execute() {
                rootDatabaseReference.child(postId).orderByChild("userId").equalTo(userId).get()
                        .addOnSuccessListener(dataSnapshot -> {
                            onSuccess(dataSnapshot.exists());
                        }).addOnFailureListener(e -> {
                            e.printStackTrace();
                            onFailed(e);
                        });
            }
        };
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

    public void getAllByUserId(
            String userId, @NonNull OnQuerySuccessListener<List<ReactionModel>> listener) {
        firebaseDatabase.getReference(USER_REACTION_PATH)
                .child(userId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        List<ReactionModel> reactionModelList = new ArrayList<>();
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            reactionModelList.add(snapshot.getValue(ReactionModel.class));
                        });
                        Log.d(LOG_TAG,
                                "Get all by user successful " + reactionModelList.toString());
                        listener.onSuccess(reactionModelList);
                    } else {
                        Log.d(LOG_TAG, "Get all by user: data snapshot not exists " + userId);
                    }
                })
                .addOnFailureListener(exception -> {
                    exception.printStackTrace();
                    Log.d(LOG_TAG, "Get all by user failed");
                })
                .addOnCanceledListener(() -> {
                    Log.d(LOG_TAG, "Get all by user cancelled");
                });
    }

    public void deletePostReaction(
            ReactionModel reactionModel, @Nullable OnQuerySuccessListener<Boolean> listener) {
        rootDatabaseReference.child(reactionModel.getPostId()).child(reactionModel.getReactionId())
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Log.d(LOG_TAG, "Remove snapshot posts/reactions/"
                            + reactionModel.getPostId() + "/"
                            + reactionModel.getReactionId() + " successful");
                    firebaseDatabase.getReference(USER_REACTION_PATH)
                            .child(reactionModel.getUserId())
                            .child(reactionModel.getReactionId()).removeValue()
                            .addOnSuccessListener(unused1 -> {
                                if (listener != null) {
                                    listener.onSuccess(true);
                                }
                                Log.d(LOG_TAG, "Remove snapshot posts/userReactions/"
                                        + reactionModel.getUserId() + "/"
                                        + reactionModel.getReactionId() + " successful");
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                if (listener != null) {
                                    listener.onSuccess(false);
                                }
                                Log.d(LOG_TAG, "Remove snapshot posts/userReactions/"
                                        + reactionModel.getUserId() + "/"
                                        + reactionModel.getReactionId() + " failed");
                            })
                            .addOnCanceledListener(() -> {
                                if (listener != null) {
                                    listener.onSuccess(false);
                                }
                                Log.d(LOG_TAG, "Remove snapshot posts/userReactions/"
                                        + reactionModel.getUserId() + "/"
                                        + reactionModel.getReactionId() + " cancelled");
                            });
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onSuccess(false);
                    }
                    e.printStackTrace();
                    Log.d(LOG_TAG, "Remove snapshot posts/reactions/"
                            + reactionModel.getPostId() + "/"
                            + reactionModel.getReactionId() + " failed");
                })
                .addOnCanceledListener(() -> {
                    if (listener != null) {
                        listener.onSuccess(false);
                    }
                    Log.d(LOG_TAG, "Remove snapshot posts/reactions/"
                            + reactionModel.getPostId() + "/"
                            + reactionModel.getReactionId() + " cancelled");
                });
    }


    public void deletePostReactionByPostId(String postId) {
        rootDatabaseReference.child(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        List<ReactionModel> reactionModelList = new ArrayList<>();
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            reactionModelList.add(dataSnapshot.getValue(ReactionModel.class));
                        });
                        reactionModelList.forEach(reactionModel -> {
                            deletePostReaction(reactionModel, null);
                        });
                    }
                });
    }
    
}
