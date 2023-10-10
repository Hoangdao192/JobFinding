package com.uet.fwork.database.repository;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.uet.fwork.database.model.NotificationModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationRepository extends Repository {
    private static final String REFERENCE_PATH = "notifications";
    private static final String LOG_TAG = "Notification repository";
    private static NotificationRepository INSTANCE = null;

    private NotificationRepository() {
        super(REFERENCE_PATH);
    }

    public static NotificationRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new NotificationRepository();
        }

        return INSTANCE;
    }

    public QueryTask<List<NotificationModel>> findAllByUserId(String userId) {
        return new QueryTask<List<NotificationModel>>() {
            @Override
            public void execute() {
                rootDatabaseReference.child(userId).orderByChild("sentTime").get()
                        .addOnSuccessListener(dataSnapshot -> {
                            Log.d(LOG_TAG, "Get all by user id successful " + userId);
                            List<NotificationModel> notificationList = new ArrayList<>();
                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                            while (iterator.hasNext()) {
                                DataSnapshot snapshot = iterator.next();
                                notificationList.add(snapshot.getValue(NotificationModel.class));
                            }
                            onSuccess(notificationList);
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            Log.d(LOG_TAG, "Get all by user id failed " + userId);
                            onFailed(e);
                        })
                        .addOnCanceledListener(() -> {
                            Log.d(LOG_TAG, "Get all by user id cancelled " + userId);
                            onCancelled();
                        });
            }
        };
    }

    public void getAllByUserId(
            String userId, OnQuerySuccessListener<List<NotificationModel>> listener) {
        rootDatabaseReference.child(userId).orderByChild("sentTime").get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, "Get all by user id successful " + userId);
                        List<NotificationModel> notificationList = new ArrayList<>();
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            DataSnapshot snapshot = iterator.next();
                            notificationList.add(snapshot.getValue(NotificationModel.class));
                        }
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
