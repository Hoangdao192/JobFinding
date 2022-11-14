package com.uet.fwork.database.repository;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.database.model.UserDeviceModel;

public class UserDeviceRepository extends Repository {
    private final static String databaseReferencePath = "userDevices";

    public UserDeviceRepository(FirebaseDatabase firebaseDatabase, FirebaseFirestore firebaseFirestore) {
        super(firebaseDatabase, firebaseFirestore, databaseReferencePath);
    }

    public UserDeviceRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, databaseReferencePath);
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
