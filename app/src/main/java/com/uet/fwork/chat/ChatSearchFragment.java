package com.uet.fwork.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.ChatRepository;
import com.uet.fwork.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatSearchFragment extends Fragment {
    private EditText edtSearch;
    private CountDownTimer countDownTimer;
    private RecyclerView recUserList;

    private List<UserModel> userResultList;

    private FirebaseUser firebaseUser;
    private ChatRepository chatRepository;
    private UserRepository userRepository;

    public ChatSearchFragment() {
        super(R.layout.fragment_chat_create);
        userResultList = new ArrayList<>();
        userRepository = UserRepository.getInstance();
        chatRepository = ChatRepository.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearch);
        recUserList = view.findViewById(R.id.recUserList);
        edtSearch.requestFocus();

        initSearchUser();
    }

    private void initSearchUser() {
        UserFoundRecyclerViewAdapter adapter = new UserFoundRecyclerViewAdapter(
                getContext(), userResultList,
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
        recUserList.setLayoutManager(new LinearLayoutManager(getContext()));

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
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("partner", partner);
        intent.putExtra("chatChanelId", chanelId);
        startActivity(intent);
    }

    private void unFocusSearch() {
        edtSearch.getText().clear();
    }
}
