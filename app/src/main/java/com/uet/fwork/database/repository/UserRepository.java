package com.uet.fwork.database.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
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

    public void isUserExists(String userUID, OnQuerySuccessListener<Boolean> onQuerySuccessListener) {
        rootDatabaseReference.child(userUID).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    onQuerySuccessListener.onSuccess(true);
                } else {
                    onQuerySuccessListener.onSuccess(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getUserByUID(
            String userUID,
            Repository.OnQuerySuccessListener<UserModel> listener
    ) {
        Log.d("GET USER", userUID);
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
                    } else if (userRole.equals(UserRole.NOT_SET)) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        listener.onSuccess(userModel);
                    }
                } else {
                    listener.onSuccess(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

//    public void insertUser(UserModel userModel) {
//        String userUID = userModel.getId();
//        if (userModel.getRole().equals(UserRole.CANDIDATE)) {
//            rootDatabaseReference.child(userUID).setValue(((CandidateModel) userModel));
//        } else if (userModel.getRole().equals(UserRole.EMPLOYER)) {
//            rootDatabaseReference.child(userUID).setValue(((EmployerModel) userModel));
//        } else {
//            rootDatabaseReference.child(userUID).setValue(userModel);
//        }
//    }

    public void insertUser(UserModel userModel) {
        String userUID = userModel.getId();
        if (userModel instanceof CandidateModel) {
            rootDatabaseReference.child(userUID).setValue(((CandidateModel) userModel));
        } else if (userModel instanceof EmployerModel) {
            rootDatabaseReference.child(userUID).setValue(((EmployerModel) userModel));
        } else {
            rootDatabaseReference.child(userUID).setValue(userModel);
        }
    }

    public void insertUser(
            UserModel userModel,
            Repository.OnQuerySuccessListener<Void> listener
    ) {
        rootDatabaseReference.child(userModel.getId()).setValue(userModel)
                .addOnSuccessListener(unused -> listener.onSuccess(unused));
    }

    public void updateUser(String userUID, Map<String, Object> updateDataMap) {
        rootDatabaseReference.child(userUID).updateChildren(updateDataMap);
    }

    public void updateUser(String userUID, UserModel userModel) {
        if (userModel.getRole().equals(UserRole.CANDIDATE)) {
            rootDatabaseReference.child(userUID).setValue(((CandidateModel) userModel));
        } else if (userModel.getRole().equals(UserRole.EMPLOYER)) {
            rootDatabaseReference.child(userUID).setValue(((EmployerModel) userModel));
        }
    }
}
