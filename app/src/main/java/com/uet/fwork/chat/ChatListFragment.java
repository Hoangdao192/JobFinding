package com.uet.fwork.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.repository.ChatRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private EditText edtSearch;
    private RecyclerView recChatList;
    private ImageView btnEdit;

    private List<UserModel> userResultList;
    private List<String> chanelIdList;

    private FirebaseUser firebaseUser;
    private ChatRepository chatRepository;
    private UserRepository userRepository;

    private ChatListRecyclerViewAdapter adapter;

    public ChatListFragment() {
        super(R.layout.fragment_chat_main);
        userResultList = new ArrayList<>();
        userRepository = UserRepository.getInstance();
        chatRepository = ChatRepository.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chanelIdList = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("CALLL RESUME");
        chatRepository.getAllChatIdByUserId(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<List<String>>() {
            @Override
            public void onSuccess(List<String> chanelIdList) {
                adapter.updateChanelList(chanelIdList);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ChatListRecyclerViewAdapter(
                getContext(), chanelIdList, firebaseUser.getUid(), FirebaseDatabase.getInstance(),
                (chanel) ->
                {
                    for (String userId : chanel.getMembers()) {
                        if (!userId.equals(firebaseUser.getUid())) {
                            userRepository.getUserByUID(userId, result -> startChatActivity(result, chanel.getId()));
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnEdit = view.findViewById(R.id.imgEdit);
        edtSearch = view.findViewById(R.id.edtSearch);
        recChatList = view.findViewById(R.id.recChatList);

        btnEdit.setOnClickListener(imageView -> {
            startActivity(new Intent(getContext(), CreateChatActivity.class));
        });

        loadChatList();
    }

    private void loadChatList() {
        recChatList.setAdapter(adapter);
        recChatList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void startChatActivity(UserModel partner, String chanelId) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("partner", partner);
        intent.putExtra("chatChanelId", chanelId);
        startActivity(intent);
    }
}
