package com.uet.fwork.chat;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.chat.ChanelModel;
import com.uet.fwork.database.repository.ChatRepository;
import com.uet.fwork.database.repository.Repository;
import com.uet.fwork.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private EditText edtSearch;
    private CountDownTimer countDownTimer;
    private RecyclerView recUserList, recChatList;

    private List<UserModel> userResultList;
    private List<String> chanelIdList;

    private FirebaseUser firebaseUser;
    private ChatRepository chatRepository;
    private UserRepository userRepository;

    private ChatListRecyclerViewAdapter adapter;

    public ChatListFragment() {
        super(R.layout.fragment_chat_main);
        userResultList = new ArrayList<>();
        userRepository = new UserRepository(FirebaseDatabase.getInstance(), FirebaseFirestore.getInstance());
        chatRepository = new ChatRepository(FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chanelIdList = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ChatListRecyclerViewAdapter(
                getContext(), chanelIdList, firebaseUser.getUid(), FirebaseDatabase.getInstance(),
                new ChatListRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(ChanelModel chanel) {
                        chanel.getMembers().forEach((key, value) -> {
                            if (!key.equals(firebaseUser.getUid())) {
                                userRepository.getUserByUID(key, new Repository.OnQuerySuccessListener<UserModel>() {
                                    @Override
                                    public void onSuccess(UserModel result) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("chatChanelId", chanel.getId());
                                        bundle.putSerializable("partner", result);
                                        Navigation.findNavController(getActivity(), R.id.navigation_host)
                                                .navigate(R.id.action_chatListFragment_to_chatMainFragment, bundle);
                                    }
                                });
                            }
                        });
                    }
                }
        );
        chatRepository.getAllChatIdByUserId(firebaseUser.getUid(), new Repository.OnQuerySuccessListener<List<String>>() {
            @Override
            public void onSuccess(List<String> chanelIdList) {
                System.out.println("FRAGMENT " + chanelIdList);
                adapter.updateChanelList(chanelIdList);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearch);
        recUserList = view.findViewById(R.id.recUserList);
        recChatList = view.findViewById(R.id.recChatList);

        loadChatList();
        initSearchUser();

        System.out.println("CALLED");
    }

    private void loadChatList() {
        recChatList.setAdapter(adapter);
        recChatList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void initSearchUser() {
        UserFoundRecyclerViewAdapter adapter = new UserFoundRecyclerViewAdapter(
                getContext(), userResultList,
                user -> {
                    chatRepository.isChatChanelExists(firebaseUser.getUid(), user.getId(), chanelId -> {
                        if (chanelId != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("chatChanelId", chanelId);
                            bundle.putSerializable("partner", user);
                            unFocusSearch();
                            Navigation.findNavController(getActivity(), R.id.navigation_host)
                                    .navigate(R.id.action_chatListFragment_to_chatMainFragment, bundle);
                        } else {
                            List<String> chatMembers = new ArrayList<>();
                            chatMembers.add(firebaseUser.getUid());
                            chatMembers.add(user.getId());
                            chatRepository.createNewChat(chatMembers, new Repository.OnQuerySuccessListener<ChanelModel>() {
                                @Override
                                public void onSuccess(ChanelModel result) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("chatChanelId", result.getId());
                                    bundle.putSerializable("partner", user);
                                    unFocusSearch();
                                    Navigation.findNavController(getActivity(), R.id.navigation_host)
                                            .navigate(R.id.action_chatListFragment_to_chatMainFragment, bundle);
                                }
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
                if (s.toString().isEmpty()) {
                    adapter.clearUserList();
                    recUserList.setVisibility(View.GONE);
                }
            }
        });
    }

    private void unFocusSearch() {
        edtSearch.getText().clear();
        recUserList.setVisibility(View.GONE);
    }
}
