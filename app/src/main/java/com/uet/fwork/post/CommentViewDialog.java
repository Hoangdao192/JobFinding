package com.uet.fwork.post;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
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
import com.uet.fwork.database.repository.CommentRepository;
import com.uet.fwork.database.repository.PostRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommentViewDialog extends BottomSheetDialog {

    private static final String LOG_TAG = "Comment fragment";

    private RecyclerView recCommentList;
    private EditText edtComment;
    private Button btnSend;
    private String postId;

    private CommentRecyclerViewAdapter commentAdapter;
    private List<CommentModel> commentList = new ArrayList<>();

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private FirebaseUser firebaseUser;

    public CommentViewDialog(Context context, String postId) {
        super(context, R.style.Theme_FWork_BottomSheetDialog_Fullscreen);
        this.postId = postId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_comment_view);

        setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                // In a previous life I used this method to get handles to the positive and negative buttons
                // of a dialog in order to change their Typeface. Good ol' days.

                BottomSheetDialog d = (BottomSheetDialog) dialog;

                // This is gotten directly from the source of BottomSheetDialog
                // in the wrapInBottomSheet() method
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                // Right here!
                BottomSheetBehavior.from(bottomSheet)
                        .setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        postRepository = new PostRepository(FirebaseDatabase.getInstance());
        commentRepository = new CommentRepository(FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        recCommentList = findViewById(R.id.recCommentList);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);

        commentAdapter = new CommentRecyclerViewAdapter(getContext(), commentList);
        recCommentList.setAdapter(commentAdapter);
        recCommentList.setLayoutManager(new LinearLayoutManager(getContext()));

        commentRepository.getAllCommentByPostId(postId, commentList -> {
            if (commentList != null) {
                commentAdapter.setCommentList(commentList);
            }
        });

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
                CommentModel commentModel = new CommentModel(
                        Calendar.getInstance().getTimeInMillis() / 1000,
                        comment, postId, firebaseUser.getUid(), ""
                );
                commentRepository.insert(commentModel, null);
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
}
