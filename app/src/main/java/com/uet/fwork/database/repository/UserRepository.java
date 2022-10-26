package com.uet.fwork.database.repository;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.EmployerModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;

import java.util.HashMap;
import java.util.Map;

public class UserRepository extends Repository {

    private static UserRepository INSTANCE = null;

    //  Path from root node
    public static final String databaseReferencePath = "users/";

    public UserRepository(FirebaseDatabase firebaseDatabase) {
        super(firebaseDatabase, databaseReferencePath);
    }

    public void getUserByUID(
            String userUID,
            Repository.OnQuerySuccessListener<UserModel> listener
    ) {
        rootDatabaseReference.child(userUID).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = (String) dataSnapshot.child("role").getValue();
                    if (userRole.equals(UserRole.CANDIDATE)) {
                        UserModel userModel = dataSnapshot.getValue(CandidateModel.class);
                        listener.onSuccess(userModel);
                    } else if (userRole.equals(UserRole.EMPLOYER)) {
                        UserModel userModel = dataSnapshot.getValue(EmployerModel.class);
                        listener.onSuccess(userModel);
                    }
                } else {
                    listener.onSuccess(null);
                }
            }
        });
    }

    public void insertUser(UserModel userModel) {
        rootDatabaseReference.child(userModel.getId()).setValue(userModel);
    }

    public void insertUser(
            UserModel userModel,
            Repository.OnQuerySuccessListener<Void> listener
    ) {
        rootDatabaseReference.child(userModel.getId()).setValue(userModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        listener.onSuccess(unused);
                    }
                });
    }

    public void updateUser(String userUID, Map<String, Object> updateDataMap) {
        rootDatabaseReference.child(userUID).updateChildren(updateDataMap);
    }
}
