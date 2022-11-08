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

    private ChatRepository chatRepository;
    private FirebaseUser firebaseUser;

    private UserRepository userRepository;

    public ChatListFragment() {
        super(R.layout.fragment_chat_main);
        userResultList = new ArrayList<>();
        userRepository = new UserRepository(FirebaseDatabase.getInstance(), FirebaseFirestore.getInstance());
        chatRepository = new ChatRepository(FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearch);

        recUserList = view.findViewById(R.id.recUserList);
        UserFoundRecyclerViewAdapter adapter = new UserFoundRecyclerViewAdapter(
                getContext(), userResultList,
                new UserFoundRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(UserModel user) {
                        System.out.println("LIST FRAGMENT: " + user.getId());
                        chatRepository.isChatChanelExists(firebaseUser.getUid(), user.getId(), chanelId -> {
                            if (chanelId != null) {
                                System.out.println("LIST FRAGMENT: " + chanelId);
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
                        10, result -> {
                            adapter.setUserList(result);
                        });
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
