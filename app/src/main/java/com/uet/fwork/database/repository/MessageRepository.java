package com.uet.fwork.database.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.database.model.chat.MessageModel;
import com.uet.fwork.database.model.chat.MessageStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageRepository extends Repository {
    private static final String databaseReferencePath = "chats/messages";

    public MessageRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, databaseReferencePath);
    }

    public MessageRepository(FirebaseDatabase firebaseDatabase, FirebaseFirestore firebaseFirestore) {
        super(firebaseDatabase, firebaseFirestore, databaseReferencePath);
    }

    /**
     * listener.onSuccess(String messageId)
     * @param message
     */
    public void insertMessage(String chanelId, MessageModel message, @Nullable OnQuerySuccessListener<String> listener) {
        DatabaseReference newMessageRef = rootDatabaseReference.child(chanelId).push();
        message.setId(newMessageRef.getKey());
        newMessageRef.setValue(message).addOnSuccessListener(unused -> {
            if (listener != null) {
                listener.onSuccess(message.getId());
            }
        }).addOnFailureListener(System.out::println);
    }

    public void updateMessage(String chanelId, String messageId, Map<String, Object> updateData) {
        rootDatabaseReference.child(chanelId).child(messageId).updateChildren(updateData);
    }

    /**
     * Lấy tin nhắn cuối cùng mà người dùng này đọc
     * @param chanelId
     * @param userId
     * @param listener
     */
    public void getLastSeenMessage(String chanelId, String userId, OnQuerySuccessListener<MessageModel> listener) {
        rootDatabaseReference.child(chanelId).equalTo(MessageStatus.SEEN, "status")
                .orderByChild("sentTime").limitToLast(1)
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            MessageModel message = dataSnapshot.getValue(MessageModel.class);
                            listener.onSuccess(message);
                        } else {
                            listener.onSuccess(null);
                        }
                    }
                });
    }

    public void getAllMessageOrderBySentTimeLimit(String chanelId, Long limit, OnQuerySuccessListener<List<MessageModel>> listener) {
        rootDatabaseReference.child(chanelId).orderByChild("sentTime").limitToLast(limit.intValue()).get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<MessageModel> messageList = new ArrayList<>();
                    dataSnapshot.getChildren().forEach(snapshot -> {
                        MessageModel message = snapshot.getValue(MessageModel.class);
                        messageList.add(message);
                    });
                    listener.onSuccess(messageList);
                })
                .addOnFailureListener(System.out::println);
    }

    public void getLastMessage(String chanelId, OnQuerySuccessListener<MessageModel> listener) {
        rootDatabaseReference.child(chanelId).orderByChild("sentTime").limitToLast(1)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    System.out.println(dataSnapshot);
                    if (dataSnapshot.exists()) {
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            MessageModel message = snapshot.getValue(MessageModel.class);
                            listener.onSuccess(message);
                        });
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(exception -> {
                    exception.printStackTrace();
                    listener.onSuccess(null);
                });
    }
}
