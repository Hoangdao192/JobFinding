package com.vnsoftware.jobfinder.database.repository;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.vnsoftware.jobfinder.database.model.UserDeviceModel;

public class UserDeviceRepository extends Repository {
    private final static String databaseReferencePath = "userDevices";
    private static final String LOG_TAG = "UserDeviceRepository";
    private static UserDeviceRepository INSTANCE = null;

    private UserDeviceRepository() {
        super(databaseReferencePath);
    }

    public static UserDeviceRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new UserDeviceRepository();
        }

        return INSTANCE;
    }

    public void insert(UserDeviceModel userDevice) {
        System.out.println("Call insert");
        if (userDevice.getUserId() == null) {
            System.out.println("Not insert");
            return;
        }
        rootDatabaseReference.child(userDevice.getUserId()).setValue(userDevice);
    }

    public void update(UserDeviceModel userDevice) {
        System.out.println("Call update");
        rootDatabaseReference.child(userDevice.getUserId()).child("deviceMessageToken")
                .setValue(userDevice.getDeviceMessageToken());
    }

    public void isUserDeviceExists(String userId, OnQuerySuccessListener<Boolean> listener) {
        rootDatabaseReference.child(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            System.out.println(dataSnapshot);
                        }
                        listener.onSuccess(dataSnapshot.exists());
                    }
                }).addOnFailureListener(System.out::println);
    }
}
