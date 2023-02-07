package com.vnsoftware.jobfinder.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.database.model.UserModel;
import com.vnsoftware.jobfinder.database.repository.ChatRepository;
import com.vnsoftware.jobfinder.database.repository.Repository;
import com.vnsoftware.jobfinder.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class CreateChatActivity extends AppCompatActivity {

    private EditText edtSearch;
    private CountDownTimer countDownTimer;
    private RecyclerView recUserList;
    private RecyclerView recRecommendList;

    private List<UserModel> userResultList;
    private List<UserModel> userRecommendList;

    private FirebaseUser firebaseUser;
    private ChatRepository chatRepository;
    private UserRepository userRepository;

    public CreateChatActivity() {
        super();
        userResultList = new ArrayList<>();
        userRepository = UserRepository.getInstance();
        chatRepository = ChatRepository.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initSearchUser() {
        userRepository.getAllUser(new Repository.OnQuerySuccessListener<List<UserModel>>() {
            @Override
            public void onSuccess(List<UserModel> result) {
                userRecommendList = result;
                UserFoundRecyclerViewAdapter adapter = new UserFoundRecyclerViewAdapter(
                        CreateChatActivity.this, userRecommendList,
                        user -> {
                            chatRepository.isChatChanelExists(firebaseUser.getUid(), user.getId(), chanelId -> {
                                //  Chat chanel giữa hai user tồn tại
                                if (chanelId != null) {
                                    unFocusSearch();
                                    startChatActivity(user, chanelId);
                                }
                                //  Tạo đoạn chat mới
                                else {
                                    List<String> chatMembers = new ArrayList<>();
                                    chatMembers.add(firebaseUser.getUid());
                                    chatMembers.add(user.getId());
                                    chatRepository.createNewChat(chatMembers, chanelModel -> {
                                        unFocusSearch();
                                        startChatActivity(user, chanelModel.getId());
                                    });
                                }
                            });
                        }
                );
                recRecommendList.setAdapter(adapter);
                recRecommendList.setLayoutManager(new LinearLayoutManager(CreateChatActivity.this));
            }
        });

        UserFoundRecyclerViewAdapter adapter = new UserFoundRecyclerViewAdapter(
                this, userResultList,
                user -> {
                    chatRepository.isChatChanelExists(firebaseUser.getUid(), user.getId(), chanelId -> {
                        //  Chat chanel giữa hai user tồn tại
                        if (chanelId != null) {
                            unFocusSearch();
                            startChatActivity(user, chanelId);
                        }
                        //  Tạo đoạn chat mới
                        else {
                            List<String> chatMembers = new ArrayList<>();
                            chatMembers.add(firebaseUser.getUid());
                            chatMembers.add(user.getId());
                            chatRepository.createNewChat(chatMembers, chanelModel -> {
                                unFocusSearch();
                                startChatActivity(user, chanelModel.getId());
                            });
                        }
                    });
                }
        );
        recUserList.setAdapter(adapter);
        recUserList.setLayoutManager(new LinearLayoutManager(this));

        countDownTimer = new CountDownTimer(50, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                userRepository.getAllUserFullNameSimilarTo(edtSearch.getText().toString(),
                        10, adapter::setUserList);
            }
        };

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                countDownTimer.cancel();
                if (!edtSearch.getText().toString().isEmpty()) {
                    recUserList.setVisibility(View.VISIBLE);
                    countDownTimer.start();
                } else {
                    adapter.clearUserList();
                    recUserList.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void startChatActivity(UserModel partner, String chanelId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("partner", partner);
        intent.putExtra("chatChanelId", chanelId);
        startActivity(intent);
    }

    private void unFocusSearch() {
        edtSearch.getText().clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        edtSearch = findViewById(R.id.edtSearch);
        recUserList = findViewById(R.id.recUserList);
        recRecommendList = (RecyclerView) findViewById(R.id.recRecommendList);
        edtSearch.requestFocus();

        initSearchUser();
    }
}