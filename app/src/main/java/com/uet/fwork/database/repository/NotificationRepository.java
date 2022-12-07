package com.uet.fwork.database.repository;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository extends Repository {
    private static final String REFERENCE_PATH = "notifications";
    private static final String LOG_TAG = "Notification repository";

    public NotificationRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, REFERENCE_PATH);
    }

    public void getAllByUserId(
            String userId, OnQuerySuccessListener<List<NotificationModel>> listener) {
        rootDatabaseReference.child(userId).orderByChild("sentTime").get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, "Get all by user id successful " + userId);
                        List<NotificationModel> notificationList = new ArrayList<>();
                        dataSnapshot.getChildren().forEach(snapshot -> {
                            notificationList.add(snapshot.getValue(NotificationModel.class));
                        });
                        listener.onSuccess(notificationList);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.d(LOG_TAG, "Get all by user id failed " + userId);
                })
                .addOnCanceledListener(() -> {
                    Log.d(LOG_TAG, "Get all by user id cancelled " + userId);
                });
    }
}
