package com.uet.fwork.database.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.database.model.chat.ChanelModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ChatRepository extends Repository {
    private static final String LOG_TAG = "ChatRepository";
    private final static String databaseReferencePath = "chats/list";
    private static ChatRepository INSTANCE = null;

    private ChatRepository() {
        super(databaseReferencePath);
    }

    public static ChatRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new ChatRepository();
        }

        return INSTANCE;
    }

    public void createNewChat(List<String> chatMembersUID, @Nullable OnQuerySuccessListener<ChanelModel> listener) {
        ChanelModel chanelModel = new ChanelModel();
        chanelModel.setMembers(chatMembersUID);
        DatabaseReference newChanelRef = rootDatabaseReference.push();
        chanelModel.setId(newChanelRef.getKey());
        newChanelRef.setValue(chanelModel)
                .addOnSuccessListener(unused -> {
                    chatMembersUID.forEach(userId -> {
                        firebaseDatabase.getReference("chats/userChats").child(userId)
                                .child(chanelModel.getId()).setValue(chanelModel);
                    });
                    if (listener != null) {
                        listener.onSuccess(chanelModel);
                    }
                })
                .addOnFailureListener(System.out::println);


    }

    public void getChatChanelById(String id, OnQuerySuccessListener<ChanelModel> listener) {
        rootDatabaseReference.child(id).get()
                .addOnSuccessListener(dataSnapshot -> {
                    ChanelModel chanel = dataSnapshot.getValue(ChanelModel.class);
                    listener.onSuccess(chanel);
                })
                .addOnFailureListener(System.out::println);
    }

    public void getChatChanelByUser(String firstUserId, String secondUserId, OnQuerySuccessListener<ChanelModel> listener) {

    }

    /**
     * Lấy các chatChanel của firstUser và tìm xem trường member của các chanel này có chứa secondUserId không
     * Nếu có thì chat chanel giữa firstUser và secondUser đã tồn tại
     * Trả và id của chanel đó
     * @param firstUserId
     * @param secondUserId
     * @param listener -> onSuccess(chanelId)
     */
    public void isChatChanelExists(String firstUserId, String secondUserId,
                                   /**
                                    * Trả về null nếu không tìm thấy chanel
                                    * Trả về chanelId nếu chat chanel giữa hai user tồn tại
                                    */
                                   OnQuerySuccessListener<String> listener
    ) {

//        System.out.println("REPOSITORY");
        //  Lấy reference của đường dẫn đến userChats của firstUser
        DatabaseReference databaseReference = firebaseDatabase.getReference("chats/userChats/" + firstUserId);
        databaseReference.get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {
                        //  Lấy tất cả các chat chanel của firstUser
                        Iterator<DataSnapshot> dataSnapshots = snapshot.getChildren().iterator();
                        while (dataSnapshots.hasNext()) {
                            DataSnapshot chanelSnapshot = dataSnapshots.next();
                            ChanelModel chanel = chanelSnapshot.getValue(ChanelModel.class);
                            if (chanel.getMembers().contains(secondUserId)) {
                                listener.onSuccess(chanelSnapshot.getKey());
                                return;
                            }
                        }
                        listener.onSuccess(null);
                    }
                });
    }

    public void getAllChatIdByUserId(String userId, OnQuerySuccessListener<List<String>> listener) {
        DatabaseReference userChatsRef = firebaseDatabase.getReference("chats/userChats/" + userId);
        userChatsRef.orderByChild("lastUpdate").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                List<String> chatChanelIdList = new ArrayList<>();
                List<ChanelModel> chanelModels = new ArrayList<>();
                dataSnapshot.getChildren().forEach((snapshot) -> {
                    chanelModels.add(snapshot.getValue(ChanelModel.class));
                });
                for (int i = 0; i < chanelModels.size() - 1; ++i) {
                    for (int j = i + 1; j < chanelModels.size(); ++j) {
                        if (chanelModels.get(i).getLastUpdate() < chanelModels.get(j).getLastUpdate()) {
                            ChanelModel temp = chanelModels.get(i);
                            chanelModels.set(i, chanelModels.get(j));
                            chanelModels.set(j, temp);
                        }
                    }
                }
                chanelModels.forEach((chanelModel) -> {
                    chatChanelIdList.add(chanelModel.getId());
                });
                listener.onSuccess(chatChanelIdList);
            }
        });
    }

    public void updateChanelLastUpdate(String chanelId, Long lastUpdate) {
        getChatChanelById(chanelId, new OnQuerySuccessListener<ChanelModel>() {
            @Override
            public void onSuccess(ChanelModel chanelModel) {
                chanelModel.getMembers().forEach((memberId) -> {
                    DatabaseReference userChatsRef =
                            firebaseDatabase.getReference("chats/userChats/" + memberId);
                    userChatsRef.child(chanelId).child("lastUpdate").setValue(lastUpdate);
                });
            }
        });
    }


}
