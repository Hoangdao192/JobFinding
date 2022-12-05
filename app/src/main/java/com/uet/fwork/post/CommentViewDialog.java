package com.uet.fwork.post;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.R;
import com.uet.fwork.database.model.post.CommentModel;
import com.uet.fwork.database.model.post.ReactionModel;
import com.uet.fwork.database.repository.CommentRepository;
import com.uet.fwork.database.repository.PostReactionRepository;
import com.uet.fwork.database.repository.PostRepository;
import com.uet.fwork.database.repository.Repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommentViewDialog extends BottomSheetDialog {

    private static final String LOG_TAG = "Comment fragment";

    private RecyclerView recCommentList;
    private EditText edtComment;
    private ImageView btnSend, btnLike;
    private TextView txvReactionNumber, txvCommentNumber;

    private String postId;

    private CommentRecyclerViewAdapter commentAdapter;
    private List<CommentModel> commentList = new ArrayList<>();

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private PostReactionRepository reactionRepository;
    private FirebaseUser firebaseUser;

    public CommentViewDialog(Context context, String postId) {
        super(context, R.style.Theme_FWork_BottomSheetDialog);
        this.postId = postId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_comment_view);

        setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet)
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        postRepository = new PostRepository(getContext(), FirebaseDatabase.getInstance());
        commentRepository = new CommentRepository(getContext(), FirebaseDatabase.getInstance());
        reactionRepository = new PostReactionRepository(getContext(), FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        recCommentList = findViewById(R.id.recCommentList);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);
        btnLike = (ImageView) findViewById(R.id.btnLike);
        txvReactionNumber = (TextView) findViewById(R.id.txtLikeNumber);
        txvCommentNumber = (TextView) findViewById(R.id.txtCommentNumber);

        commentAdapter = new CommentRecyclerViewAdapter(getContext(), commentList);
        recCommentList.setAdapter(commentAdapter);
        recCommentList.setLayoutManager(new LinearLayoutManager(getContext()));

//        commentRepository.getAllCommentByPostId(postId, commentList -> {
//            if (commentList != null) {
//                commentAdapter.setCommentList(commentList);
//            }
//        });

        loadCommentNumber();
        loadLikeNumber();

        commentRepository.getRootDatabaseReference().child(postId).addChildEventListener(new ChildEventListener() {
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
                edtComment.getText().clear();
                CommentModel commentModel = new CommentModel(
                        Calendar.getInstance().getTimeInMillis() / 1000,
                        comment, postId, firebaseUser.getUid(), ""
                );
                commentRepository.insert(commentModel, null);
            }
        });

        reactionRepository.isUserLikePost(postId, firebaseUser.getUid(), new Repository.OnQuerySuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    btnLike.setImageDrawable(getContext().getDrawable(R.drawable.ic_heart_no_fill));
                } else {
                    btnLike.setImageDrawable(getContext().getDrawable(R.drawable.ic_heart_fill));
                }
            }
        });
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reactionRepository.isUserLikePost(postId, firebaseUser.getUid(), new Repository.OnQuerySuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        if (result) {
                            btnLike.setImageDrawable(getContext().getDrawable(R.drawable.ic_heart_no_fill));
                            reactionRepository.removeReactionByPostAndUser(postId, firebaseUser.getUid());
                        } else {
                            btnLike.setImageDrawable(getContext().getDrawable(R.drawable.ic_heart_fill));
                            reactionRepository.insert(new ReactionModel(
                                    firebaseUser.getUid(), postId, Calendar.getInstance().getTimeInMillis()/1000
                            ), null);
                        }
                    }
                });
            }
        });

        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    dialog.dismiss();
                }
                return true;
            }
        });
    }

    private void loadLikeNumber() {
        reactionRepository.getNumberOfReaction(postId, new Repository.OnQuerySuccessListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                txvReactionNumber.setText(result + " Lượt thích");
            }
        });
    }

    private void loadCommentNumber() {
        commentRepository.getNumberOfComment(postId, new Repository.OnQuerySuccessListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                txvCommentNumber.setText(result + " Bình luận");
            }
        });
    }
}
