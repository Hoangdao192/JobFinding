package com.uet.fwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.uet.fwork.chat.ChatActivity;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.post.PostModel;
import com.uet.fwork.database.repository.ChatRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;
import com.uet.fwork.dialog.ConfirmDialog;
import com.uet.fwork.dialog.ErrorDialog;
import com.uet.fwork.firebasehelper.FirebaseAuthHelper;
import com.uet.fwork.post.PostsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewProfileActivityCandidate extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private RecyclerView postsRecyclerView;
    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private FirebaseAuthHelper firebaseAuthHelper;

    List<PostModel> postModelList;
    PostsAdapter postsAdapter;
    String userRole = "";
    String uid ="";
    private ImageView imgAvatar, imgOpenChat;
    private TextView txvName, txvEmail, txvPhone, txvSex, txvBirth, txvYearOfExperience, txvMajor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get uid of the clicked user
        setContentView(R.layout.activity_view_profile_candidate);

        Intent intent = getIntent();
        uid = intent.getStringExtra("id");

        chatRepository = ChatRepository.getInstance();
        firebaseAuthHelper = FirebaseAuthHelper.getInstance();

        imgOpenChat = (ImageView) findViewById(R.id.btnOpenChat);
        imgAvatar = findViewById(R.id.avatarIv);
        txvName = findViewById(R.id.nameTv);
        txvBirth = findViewById(R.id.birthTv);
        txvSex = findViewById(R.id.sexTv);
        txvMajor = findViewById(R.id.jobTv);
        txvYearOfExperience = findViewById(R.id.workYearTv);
        txvPhone = findViewById(R.id.phoneTv);
        txvEmail = findViewById(R.id.emailTv);
        Button btnBack = (Button) findViewById(R.id.return_button);

        userRepository = UserRepository.getInstance();
        userRepository.getUserByUID(uid, model -> {
            String avatarImagePath = model.getAvatar();
            if (!avatarImagePath.isEmpty()) {
                Picasso.get().load(avatarImagePath)
                        .placeholder(R.drawable.wlop_33se)
                        .into(imgAvatar);
            }
            CandidateModel candidate = (CandidateModel) model;
            txvName.setText(candidate.getFullName());
            txvBirth.setText(candidate.getDateOfBirth());
            txvSex.setText(candidate.getSex());
            txvMajor.setText(candidate.getMajor());
            txvYearOfExperience.setText(String.valueOf(candidate.getYearOfExperience()));
            txvPhone.setText(candidate.getPhoneNumber());
            txvEmail.setText(candidate.getEmail());
        });
        postsRecyclerView = findViewById(R.id.recyclerviewPosts);
        firebaseAuth = FirebaseAuth.getInstance();

        userRepository = UserRepository.getInstance();
        TextView txtEditProfile = findViewById(R.id.report_Btn);
        txtEditProfile.setOnClickListener(editText -> {
            ConfirmDialog confirmDialog = new ConfirmDialog(
                    ViewProfileActivityCandidate.this, "Báo cáo người dùng",
                    "Bạn có chắc chắn muốn báo cáo người dùng này?",
                    new ConfirmDialog.OnEventListener() {
                        @Override
                        public void onConfirm() {
                            ErrorDialog errorDialog = new ErrorDialog(
                                    ViewProfileActivityCandidate.this, "Báo cáo thành công",
                                    "Cảm ơn bạn đã báo cáo. \nChúng tôi sẽ xem xét người dùng này."
                            );
                            errorDialog.show();
                        }

                        @Override
                        public void onCancel() {

                        }
                    }
            );
            confirmDialog.show();
        });


        imgOpenChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRepository.getUserByUID(uid, userModel ->
                        chatRepository.isChatChanelExists(uid, firebaseAuthHelper.getUser().getId(),
                                chanelId -> {
                                    if (chanelId != null) {
                                        startChatActivity(userModel, chanelId);
                                    } else {
                                        List<String> chatMembers = new ArrayList<>();
                                        chatMembers.add(uid);
                                        chatMembers.add(firebaseAuthHelper.getUser().getId());
                                        chatRepository.createNewChat(chatMembers, chanelModel -> {
                                            startChatActivity(userModel, chanelModel.getId());
                                        });
                                    }
                                }));
            }
        });

        postModelList = new ArrayList<>();
        loadPosts();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void startChatActivity(UserModel partner, String chanelId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("partner", partner);
        intent.putExtra("chatChanelId", chanelId);
        startActivity(intent);
    }

    private void loadPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first (load data from last)
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts/list");
        Query query = databaseReference.orderByChild("userId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postModelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                   PostModel myPosts = ds.getValue(PostModel.class);

                   //add to list
                    postModelList.add(myPosts);

                    //adapter
                    postsAdapter = new PostsAdapter(ViewProfileActivityCandidate.this, postModelList);
                    postsRecyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileActivityCandidate.this, ""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}