package com.uet.fwork.database.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.database.model.post.PostModel;

import java.util.ArrayList;
import java.util.List;

public class PostRepository extends Repository {
    private static final String LOG_TAG = "Post repository";

    public final static String DATABASE_PATH = "posts/list";

    private PostApplyRepository postApplyRepository;
    private CommentRepository commentRepository;
    private PostReactionRepository postReactionRepository;
    private Context context;

    public PostRepository(Context context, FirebaseDatabase firebaseDatabase) {
        super(DATABASE_PATH);
        this.context = context;
        postApplyRepository = new PostApplyRepository(context, FirebaseDatabase.getInstance());
        postReactionRepository = new PostReactionRepository(context, firebaseDatabase);
        commentRepository = new CommentRepository(context, firebaseDatabase);
    }

    public void insert(PostModel postModel, @Nullable OnQuerySuccessListener<Boolean> listener) {
        String postId = rootDatabaseReference.push().getKey();
        postModel.setPostId(postId);
        rootDatabaseReference.child(postId).setValue(postModel)
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

    public void getById(String postId, @NonNull OnQuerySuccessListener<PostModel> listener) {
        rootDatabaseReference.child(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        PostModel postModel = dataSnapshot.getValue(PostModel.class);
                        Log.d(LOG_TAG, "Get post by id " + postId + " successful " + postModel.toString());
                        listener.onSuccess(postModel);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "Get post by id " + postId + " failed");
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.d(LOG_TAG, "Get post by id " + postId + " cancelled");
                    }
                });
    }

    public void getAllByUserId(
            String userId, @NonNull OnQuerySuccessListener<List<PostModel>> listener) {
        rootDatabaseReference.orderByChild("userId").equalTo(userId).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        List<PostModel> postModels = new ArrayList<>();
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            postModels.add(snapshot.getValue(PostModel.class));
                        });
                        Log.d(LOG_TAG,
                                "Get all post by user id successful "
                                        + userId + " " + postModels.toString());
                        listener.onSuccess(postModels);
                    } else {
                        Log.d(LOG_TAG, "Get all by user id: data snapshot not exists");
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "Get all by user id failed " + userId);
                })
                .addOnCanceledListener(() -> {
                    Log.d(LOG_TAG, "Get all by user id cancelled " + userId);
                });
    }

    public void deletePost(PostModel postModel) {
        postApplyRepository.deletePostApplyByPostId(postModel.getPostId());
        commentRepository.deleteCommentByPostId(postModel.getPostId());
        postReactionRepository.deletePostReactionByPostId(postModel.getPostId());
        rootDatabaseReference.child(postModel.getPostId()).removeValue();
    }
}
