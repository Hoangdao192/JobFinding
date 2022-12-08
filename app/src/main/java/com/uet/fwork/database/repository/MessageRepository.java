package com.uet.fwork.database.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.Constants;
import com.uet.fwork.database.model.chat.MessageModel;
import com.uet.fwork.database.model.chat.MessageStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageRepository extends Repository {
    private static final String databaseReferencePath = "chats/messages";
    private static final String LOG_TAG = "Message repository";
    private static MessageRepository INSTANCE = null;

    private MessageRepository() {
        super(databaseReferencePath);
    }

    public static MessageRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new MessageRepository();
        }

        return INSTANCE;
    }

    /**
     * listener.onSuccess(String messageId)
     * @param message
     */
    public void insertMessage(String chanelId, MessageModel message, @Nullable OnQuerySuccessListener<String> listener) {
        DatabaseReference newMessageRef = rootDatabaseReference.child(chanelId).push();
        message.setId(newMessageRef.getKey());
        newMessageRef.setValue(message).addOnSuccessListener(unused -> {
            notifyMessageSent(chanelId, message.getId());
            if (listener != null) {
                listener.onSuccess(message.getId());
            }
        }).addOnFailureListener(System.out::println);
    }

    private void notifyMessageSent(String chanelId, String messageId) {
        RequestQueue requestQueue = Volley.newRequestQueue(applicationContext);
        String apiUrl = Constants.SERVER_URL + "message/notify";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl, response -> {
            response = new String(
                    response.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
            Log.d(LOG_TAG, "Volley: Request response " + response);
        }, error -> {
            error.printStackTrace();
            Log.d(LOG_TAG, "Volley: Send request failed " + apiUrl);
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("messageId", messageId);
                params.put("chanelId", chanelId);
                return params;
            }
        };
        Log.d(LOG_TAG, "Volley: Add request to queue");
        requestQueue.add(stringRequest);
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
