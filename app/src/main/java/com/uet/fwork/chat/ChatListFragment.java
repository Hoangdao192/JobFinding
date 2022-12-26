package com.uet.fwork.chat;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.chat.ChanelModel;
import com.uet.fwork.database.repository.ChatRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListFragment extends Fragment {

    private EditText edtSearch;
    private RecyclerView recChatList;
    private CircleImageView cirImgAvatar;

    private List<UserModel> userResultList;
    private List<String> chanelIdList;

    private FirebaseUser firebaseUser;
    private ChatRepository chatRepository;
    private UserRepository userRepository;

    private ChatListRecyclerViewAdapter adapter;

    private ChatSearchFragment chatSearchFragment = new ChatSearchFragment();

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
                new ChatListRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(ChanelModel chanel) {
                        chanel.getMembers().forEach(userId -> {
                            if (!userId.equals(firebaseUser.getUid())) {
                                userRepository.getUserByUID(userId, new Repository.OnQuerySuccessListener<UserModel>() {
                                    @Override
                                    public void onSuccess(UserModel result) {
                                        startChatActivity(result, chanel.getId());
                                    }
                                });
                            }
                        });
                    }
                }
        );
//        chatRepository.getAllChatIdByUserId(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<List<String>>() {
//            @Override
//            public void onSuccess(List<String> chanelIdList) {
//                adapter.updateChanelList(chanelIdList);
//            }
//        });

//        FirebaseDatabase.getInstance().getReference("chats/userChats").child(firebaseUser.getUid())
//                        .addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                                chatRepository.getAllChatIdByUserId(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<List<String>>() {
//                                    @Override
//                                    public void onSuccess(List<String> chanelIdList) {
//                                        adapter.updateChanelList(chanelIdList);
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                                chatRepository.getAllChatIdByUserId(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<List<String>>() {
//                                    @Override
//                                    public void onSuccess(List<String> chanelIdList) {
//                                        adapter.updateChanelList(chanelIdList);
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                            }
//
//                            @Override
//                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearch);
        recChatList = view.findViewById(R.id.recChatList);
        recChatList.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        cirImgAvatar = view.findViewById(R.id.imgUserAvatar);

        edtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, chatSearchFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        userRepository.getUserByUID(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<UserModel>() {
            @Override
            public void onSuccess(UserModel result) {
                if (result != null) {
                    if (!result.getAvatar().isEmpty()) {
                        Picasso.get().load(result.getAvatar())
                                .placeholder(R.drawable.wlop_33se)
                                .into(cirImgAvatar);
                    }
                }
            }
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
